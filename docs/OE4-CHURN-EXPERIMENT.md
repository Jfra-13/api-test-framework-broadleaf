# 🧪 OE4 — Experimento de Inyección y Code Churn

> **Objetivo Específico 4:** demostrar de forma cuantitativa que la arquitectura
> multicapa del framework **absorbe los cambios de contrato del SUT** y **minimiza
> la deuda técnica** (Code Churn).

**Estado:** ⏳ Fase 0 preparada — pendiente de ejecución (ver `oe4/sut-mutations/README-INYECCION.md`).

---

## 1. Hipótesis y métrica

**Hipótesis (H1):** ante una ruptura de contrato inyectada en el SUT, la cantidad de
líneas que hay que modificar en el framework (*Code Churn*) es **mínima y se localiza
en las capas inferiores** (contrato/cliente), sin propagarse a la especificación de
negocio (Gherkin) cuando el campo no está acoplado funcionalmente.

**Métrica — Code Churn:**

```
Code Churn = líneas modificadas en el framework para reabsorber la ruptura (git diff)
Ratio      = Code Churn / Tamaño total del framework
```

**Denominador (tamaño real del "producto" = framework):**

| Capa | Archivos | Líneas |
|---|---|---|
| Java (clients, dto, steps, hooks, runners, utils) | — | 1.532 |
| Features (Gherkin) | 3 | 105 |
| Schemas (JSON contracts) | 5 | 387 |
| `pom.xml` | 1 | 163 |
| **TOTAL** | | **2.187** |

> *(Medido con `git ls-files ... | xargs wc -l` el 2026-06-03.)*

---

## 2. Marco del experimento

Se separan rigurosamente los dos sistemas:

- **SUT (Sistema Bajo Prueba):** Broadleaf Commerce DemoSite (`/cart` endpoint).
- **Producto (Framework):** la suite BDD de automatización de APIs.

La **inyección** (mutación) se realiza en el SUT; la **medición** del churn se hace
exclusivamente en el repositorio del Framework. La mutación del SUT **no** cuenta como churn.

### Mecanismo de inyección
El bean `OrderWrapper` de Broadleaf (que serializa la respuesta de `/cart`) está
declarado en XML de la librería (`bl-api-applicationContext-wrapper.xml`) con
`@XmlAccessorType(FIELD)`. Para una inyección **controlada, quirúrgica y reversible**,
se interceptan las dos respuestas validadas contra `cart-schema.json`
(`GET /cart` y `POST /cart/{id}/item`) con un filtro de servlet
(`Oe4CartContractMutationFilter.java`) que renombra **una** llave de primer nivel del
JSON. Es el equivalente observable a renombrar un campo en el controller/DTO del backend.

---

## 3. Preparación previa (Fase 0) — *hardening* del contrato

Para que el *contract testing* detecte una ruptura por **renombre de llave**, el contrato
debe **exigir** la presencia del campo. Como mejora legítima de la línea base, se endureció
`cart-schema.json` agregando `customer` a `required`:

```diff
- "required": ["id", "status", "itemCount"],
+ "required": ["id", "status", "itemCount", "customer"],
```

> El campo `customer` siempre está presente en la respuesta del carrito (el wrapper lo
> asigna incondicionalmente), por lo que el endurecimiento **no afecta** la línea base verde.
> Este cambio se commitea **antes** del experimento y **no** se contabiliza como churn.

---

## 4. Protocolo de medición (rigor)

Cada mutación es un **ciclo independiente sobre árbol limpio**:

```
1. Árbol del framework limpio  (git status limpio)
2. Inyectar mutación en el SUT  (filtro ON, reiniciar Broadleaf)
3. Ejecutar la suite -> capturar ROJO (falla)         [evidencia "antes"]
4. Aplicar el arreglo mínimo en el framework
5. Ejecutar la suite -> capturar VERDE                [evidencia "después"]
6. git diff --stat / git diff  -> registrar el Code Churn
7. git checkout -- .  (resetear framework)  +  revertir mutación SUT
```

---

## 5. Las dos mutaciones

| | **Mutación A — "solo-contrato"** | **Mutación B — "funcional"** |
|---|---|---|
| Llave mutada en SUT | `customer` → `cliente` | `id` → `idCarrito` |
| ¿Quién la referencia en el framework? | **Nadie** en steps/features. Solo el contrato. | `cart-schema` + `CartSteps` (x2) + `cart.feature` + `checkout.feature` |
| Falla esperada | Validación de contrato `cart-schema.json` | Contrato **+** flujo funcional (cartId = `null` → `/cart/null/item` falla) |
| Capas tocadas al arreglar | Contrato | Contrato + Steps + Especificación |
| Churn esperado | **~1 línea / 1 archivo** | **~5 líneas / 4 archivos** |

---

## 6. Resultados — Mutación A (`customer` → `cliente`)

- 📸 Evidencia ROJO: _(opcional — re-inyectar sin el fix para capturar la falla de contrato)_
- 📸 Evidencia VERDE: ✅ suite completa en verde (exit code 0, 6/6 escenarios).
  - Corroboración objetiva: `Body Length` del carrito pasó de **651 → 650** bytes
    (`"customer"` 8 chars → `"cliente"` 7 chars), confirmando que la mutación se aplicó.

**`git diff --stat`:**
```
 src/test/resources/schemas/cart-schema.json | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

**`git diff`:**
```diff
@@ -2,7 +2,7 @@
   "$schema": "http://json-schema.org/draft-07/schema#",
   "title": "Cart Schema - Broadleaf Commerce",
   "type": "object",
-  "required": ["id", "status", "itemCount", "customer"],
+  "required": ["id", "status", "itemCount", "cliente"],
   "properties": {
```

| Métrica | Valor |
|---|---|
| Archivos modificados | **1** (`cart-schema.json`) |
| Líneas modificadas (Code Churn) | **1** |
| Archivos `.feature` tocados | **0** ✅ |
| Archivos `steps/` tocados | **0** ✅ |
| Ratio churn / 2.187 | **≈ 0,05 %** |

---

## 7. Resultados — Mutación B (`id` → `idCarrito`)

- 📸 Evidencia ROJO: _(captura del usuario — fallo del contrato `cart-schema.json` + cartId nulo)_
- 📸 Evidencia VERDE: ✅ suite completa en verde (exit code 0, 6/6 escenarios).
  - Corroboración objetiva: `Body Length` del carrito pasó de **651 → 658** bytes
    (`"id"` → `"idCarrito"`, +7 chars), y la respuesta de add-item de **3538 → 3545**,
    confirmando que la mutación se aplicó en las dos respuestas validadas por `cart-schema`.

> ⚠️ **Nota metodológica:** el `git diff` crudo incluía además `docs/OE4-CHURN-EXPERIMENT.md`
> (cambio de documentación, sin commitear), que **NO** se contabiliza como churn — la
> documentación no forma parte del producto (denominador = 2.187 = java+features+schemas+pom).
> El churn real son los **4 archivos del framework** listados abajo.

**`git diff --stat` (solo archivos del producto):**
```
 src/test/resources/schemas/cart-schema.json        | 2 +-
 src/test/java/com/tesis/automation/steps/CartSteps.java | 4 ++--
 src/test/resources/features/cart.feature           | 2 +-
 src/test/resources/features/checkout.feature       | 2 +-
 4 files changed, 5 insertions(+), 5 deletions(-)
```

**`git diff`:**
```diff
--- a/src/test/resources/schemas/cart-schema.json
+++ b/src/test/resources/schemas/cart-schema.json
-  "required": ["id", "status", "itemCount", "customer"],
+  "required": ["idCarrito", "status", "itemCount", "customer"],

--- a/src/test/java/com/tesis/automation/steps/CartSteps.java
+++ b/src/test/java/com/tesis/automation/steps/CartSteps.java
@@ userGetsNewCart()
-        String cartId = response.jsonPath().getString("id");
+        String cartId = response.jsonPath().getString("idCarrito");
@@ captureCartId()
-        String cartId = response.jsonPath().getString("id");
+        String cartId = response.jsonPath().getString("idCarrito");

--- a/src/test/resources/features/cart.feature
+++ b/src/test/resources/features/cart.feature
-    And la respuesta contiene un campo "id" de tipo número
+    And la respuesta contiene un campo "idCarrito" de tipo número

--- a/src/test/resources/features/checkout.feature
+++ b/src/test/resources/features/checkout.feature
-    And la respuesta contiene un campo "id" de tipo número
+    And la respuesta contiene un campo "idCarrito" de tipo número
```

| Métrica | Valor |
|---|---|
| Archivos modificados | **4** (cart-schema, CartSteps, cart.feature, checkout.feature) |
| Líneas modificadas (Code Churn) | **5** |
| Desglose por capa | Contrato **1** + Steps **2** + Especificación **2** |
| Líneas del **flujo de negocio** del escenario modificadas | **0** (solo cambió el literal del nombre del campo, no la secuencia Given/When/Then) |
| Ratio churn / 2.187 | **≈ 0,23 %** |

---

## 8. Análisis comparativo

| | Mutación A (contrato) | Mutación B (funcional) |
|---|---|---|
| Code Churn (líneas) | **1** | **5** |
| Archivos | **1** | **4** |
| Capas afectadas | Contrato | Contrato (1) + Steps (2) + Spec (2) |
| ¿Tocó la lógica de negocio (flujo Gherkin)? | **No** | **No** (solo literales de nombre de campo) |
| Ratio sobre 2.187 líneas | **≈ 0,05 %** | **≈ 0,23 %** |

**Lectura:**
- La **Mutación A** confirma el caso ideal de la hipótesis: el *contract testing*
  centralizado absorbe una ruptura pura en **~1 línea**, sin tocar features ni steps.
- La **Mutación B** muestra el matiz honesto: un campo **acoplado funcionalmente**
  cuesta más, pero el *blast radius* queda **acotado y cuantificado** en las capas
  técnicas; el **flujo de negocio** descrito en Gherkin permanece intacto.

**Hallazgo de diseño (aporte de la tesis):** la Mutación B revela que los nombres de
campo **no deberían filtrarse al Gherkin**. Si la validación se delega íntegramente al
contrato (schema) y la captura del identificador se centraliza, el churn de B se acercaría
al de A. Esto refuerza el principio de **diseño para el cambio**.

---

## 9. Conclusión

El experimento **valida la hipótesis de mantenibilidad (H1)** con datos concretos:

- **Mutación A (ruptura pura de contrato):** Code Churn = **1 línea / 1 archivo**
  (≈ 0,05 % de las 2.187 líneas del producto), **sin tocar features ni steps**. El
  *contract testing* centralizado en JSON Schema absorbe el cambio en un único punto.
- **Mutación B (campo con acoplamiento funcional):** Code Churn = **5 líneas / 4 archivos**
  (≈ 0,23 %), repartido en Contrato (1) + Steps (2) + Especificación (2). Mayor que A,
  pero igualmente **mínimo y acotado a las capas técnicas**; el **flujo de negocio**
  descrito en Gherkin **no cambió** (solo el literal del nombre del campo).

En ambos casos el churn es **inferior al 0,25 %** del tamaño del framework y **ningún
cambio alteró la lógica de negocio** de los escenarios. Esto demuestra cuantitativamente
que la arquitectura multicapa propuesta **minimiza la deuda técnica** ante la evolución
del contrato del SUT.

**Aporte adicional (hallazgo de diseño):** el contraste A-vs-B evidencia que el costo de
mantenimiento crece cuando el nombre de un campo se **filtra** a capas superiores
(steps y, sobre todo, al Gherkin). La recomendación de diseño derivada es **delegar la
validación estructural íntegramente al contrato (schema)** y **centralizar la captura de
identificadores**, lo que acercaría el churn de un cambio funcional (B) al caso ideal (A).

---

## 10. Anexos
- `oe4/sut-mutations/Oe4CartContractMutationFilter.java` — artefacto de inyección.
- `oe4/sut-mutations/README-INYECCION.md` — protocolo de ejecución paso a paso.
- Capturas ROJO/VERDE y salidas de `git diff` por mutación.
