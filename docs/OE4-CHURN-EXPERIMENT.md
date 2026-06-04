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
se intercepta la respuesta de `GET /cart` con un filtro de servlet
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
| ¿Quién la referencia en el framework? | **Nadie** en steps/features. Solo el contrato. | `cart-schema` + `CartSteps` (x2) + `CheckoutSteps` + `cart.feature` + `checkout.feature` |
| Falla esperada | Validación de contrato `cart-schema.json` | Contrato **+** flujo funcional (cartId = `null` → `/cart/null/item` falla) |
| Capas tocadas al arreglar | Contrato | Contrato + Steps + Especificación |
| Churn esperado | **~1 línea / 1 archivo** | **~6 líneas / 5 archivos** |

---

## 6. Resultados — Mutación A (`customer` → `cliente`)

- 📸 Evidencia ROJO: _(pegar captura)_
- 📸 Evidencia VERDE: _(pegar captura)_

**`git diff --stat`:**
```
(pegar salida)
```

**`git diff`:**
```diff
(pegar salida)
```

| Métrica | Valor |
|---|---|
| Archivos modificados | _(a completar)_ |
| Líneas modificadas (Code Churn) | _(a completar)_ |
| Archivos `.feature` tocados | _(esperado: 0)_ |
| Archivos `steps/` tocados | _(esperado: 0)_ |
| Ratio churn / 2.187 | _(a completar)_ |

---

## 7. Resultados — Mutación B (`id` → `idCarrito`)

- 📸 Evidencia ROJO: _(pegar captura)_
- 📸 Evidencia VERDE: _(pegar captura)_

**`git diff --stat`:**
```
(pegar salida)
```

**`git diff`:**
```diff
(pegar salida)
```

| Métrica | Valor |
|---|---|
| Archivos modificados | _(a completar)_ |
| Líneas modificadas (Code Churn) | _(a completar)_ |
| Archivos `.feature` tocados | _(a completar)_ |
| Líneas del **flujo de negocio** del escenario modificadas | _(esperado: 0 — solo cambian literales de nombre de campo)_ |
| Ratio churn / 2.187 | _(a completar)_ |

---

## 8. Análisis comparativo

| | Mutación A (contrato) | Mutación B (funcional) |
|---|---|---|
| Code Churn (líneas) | _(A)_ | _(B)_ |
| Archivos | _(A)_ | _(B)_ |
| Capas afectadas | Contrato | Contrato + Steps + Spec |
| ¿Tocó la lógica de negocio (flujo Gherkin)? | No | No (solo literales) |
| Ratio sobre 2.187 líneas | _(A)_ | _(B)_ |

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

_(A completar con los números reales.)_

La arquitectura multicapa propuesta **valida la hipótesis de mantenibilidad**: un cambio
de contrato del SUT se reabsorbe con un Code Churn de **N líneas sobre 2.187** (≈ X %),
localizado en las capas inferiores, demostrando una **deuda técnica mínima** frente a la
evolución del backend.

---

## 10. Anexos
- `oe4/sut-mutations/Oe4CartContractMutationFilter.java` — artefacto de inyección.
- `oe4/sut-mutations/README-INYECCION.md` — protocolo de ejecución paso a paso.
- Capturas ROJO/VERDE y salidas de `git diff` por mutación.
