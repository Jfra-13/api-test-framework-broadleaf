# OE4 — Resultados del Experimento de Inyección y Code Churn

> Acumulador de resultados. **NO se resetea entre mutaciones.**
> Denominador de churn **CONGELADO = 2187** líneas para TODAS las mutaciones
> (nota baseline: hoy el repo mide 2182; el java cayó 1532→1527 tras TK004 que borró `docs/`.
> Se conserva 2187 por consistencia con A y B ya reportados).
> Fórmula: `CHURN_% = (líneas_modificadas / 2187) * 100`.
> Regla FN: `FN = 0` si la ruptura fue detectada; `1` si pasó inadvertida.
> Baseline de contrato endurecido commiteado en `a560441` (no cuenta como churn).

## Mutaciones ya medidas en sesiones previas (referencia)
| Mut | Cambio | Arch | Líneas | % | Detección | FN | Gherkin |
|---|---|---|---|---|---|---|---|
| A | renombrar `customer`→`cliente` | 1 | 1 | 0,05% | Sí (schema) | 0 | No |
| B | renombrar `id`→`idCarrito` | 4 | 5 | 0,23% | Sí (funcional+cascada) | 0 | No |

---

## Tarea A — Mutación M2: cambio de tipo de dato
**Ruptura:** `itemCount` entero → string (filtro `MUTATION="M2"`, muta GET /cart y POST /cart/{id}/item).
**Adaptación a verde (capa técnica):** `cart-schema.json` → `"itemCount": { "type": "string" }` (1 línea).

| Token | Valor |
|---|---|
| `[CHURN_M2_ARCH]` | 1 |
| `[CHURN_M2_LIN]` | 1 |
| `[CHURN_M2_%]` | 0,05% (1/2187) |
| `[DETECCION_M2]` | Sí |
| `[FN_M2]` | 0 |
| `[M2_GHERKIN]` | No |

**Detector:** ÚNICO = JSON Schema (`cart-schema.json`). El step funcional `itemCount == 1` pasó por
coerción de RestAssured (`"1"`→1); solo el contrato cazó el cambio de tipo.

**Evidencia:**
- Diff: `evidencia/M2_cambio_tipo.diff`
- ROJO: `evidencia/M2_ROJO_suite.txt` (exit -1; `instance type (string) does not match (allowed: ["integer"])` @ `/properties/itemCount`)
- VERDE: `evidencia/M2_VERDE_suite.txt` (exit 0; `Validacion de contrato exitosa: cart-schema.json`)

---

## Tarea B — Mutación M3: eliminación de llave requerida
**Ruptura:** filtro `MUTATION="M3"` elimina la clave `status` del JSON de /cart (GET /cart y POST /cart/{id}/item).
**Adaptación a verde (capa técnica):** `cart-schema.json` → quitar `status` de `required` y borrar su entrada en `properties` (2 líneas).

| Token | Valor |
|---|---|
| `[CHURN_M3_ARCH]` | 1 |
| `[CHURN_M3_LIN]` | 2 |
| `[CHURN_M3_%]` | 0,09% (2/2187) |
| `[DETECCION_M3]` | Sí |
| `[FN_M3]` | 0 |
| `[M3_GHERKIN]` | No |

**Detector:** ÚNICO = JSON Schema (`cart-schema.json`), `keyword: "required"`, `missing: ["status"]`.
Sin confusor: el filtro muta sólo /cart, por lo que el step de checkout final
`el campo "status" debe ser "SUBMITTED"` (orden no mutada) pasó tanto en rojo como en verde.
DTO (`OrderWrapper.status`) NO se tocó: es compartido con la orden final, que sí lo asercia.

**Evidencia:**
- Diff: `evidencia/M3_quitar_status.diff` (1 archivo, 1 insertion + 2 deletions)
- ROJO: `evidencia/M3_ROJO_suite.txt` (exit -1; `object has missing required properties (["status"])` en 3 escenarios: `cart.feature:11`, `cart.feature:20`, `checkout.feature:17`)
- VERDE: `evidencia/M3_VERDE_suite.txt` (exit 0; `Validacion de contrato exitosa: cart-schema.json` ×3; checkout `status=="SUBMITTED"` intacto)

## Tarea C — Control negativo: rename de variable interna del controller
**Cambio:** renombre de una variable **interna** en el controller del SUT (`CustomCartEndpoint`), sin tocar el JSON de respuesta ni la capa de test. Filtro de inyección DESACTIVADO (`ACTIVE=false`).
**Esperado:** suite VERDE → especificidad (0 falsos positivos).

| Token | Valor |
|---|---|
| `[CTRL_RESULTADO]` | VERDE |
| `[CTRL_FALSOS_POSITIVOS]` | 0 |
| `[CTRL_CHURN_ARCH]` | 0 |
| `[CTRL_CHURN_LIN]` | 0 |
| `[CTRL_CHURN_%]` | 0,00% (0/2187) |

**Interpretación:** un cambio interno que NO altera el contrato observable no dispara ningún detector. La batería no genera falsos positivos → especificidad confirmada. Complementa la sensibilidad medida en A/B/M2/M3.

**Evidencia:**
- VERDE: `oe4-sut-artifact/result_C.txt` (exit 0; `Validacion de contrato exitosa: cart-schema.json` ×3; cart body length 651 = `status` presente, sin mutación).
- Nota de incidente: el primer run salió ROJO con fingerprint M3 (`missing: ["status"]`, body 629) porque el filtro `Oe4CartContractMutationFilter` seguía `ACTIVE=true` en el SUT (build viejo de Tarea B). Tras `ACTIVE=false` + rebuild del módulo `api` + restart → VERDE.

## Tarea D — Índice de Mantenibilidad (MI)

**Naturaleza:** métrica **estática** (no toca SUT, filtro ni suite). Medida en IntelliJ con el plugin **MetricsReloaded** sobre `src` (paquete `com.tesis.automation`, incluyendo test sources). Perfil `Mi-tesis` con Halstead Volume (`V`), Cyclomatic Complexity (`v(G)`) y Lines of Code (`LOC`) a nivel método.

**Fórmula declarada** — MI clásico de 3 factores, normalizado Visual Studio (sin factor de comentarios):

```
MI = max(0, (171 − 5.2·ln(V) − 0.23·v(G) − 16.2·ln(LOC)) · 100 / 171)
```

Agregación = **promedio por método** (91 métodos). Clamp `V=max(V,1)`, `LOC=max(LOC,1)` por 1 constructor vacío (`V=0`).

| Token | Valor |
|---|---|
| `[MI_VALOR]` | 69,15 |
| `[MI_METODOS]` | 91 |
| `[MI_MIN]` | 42,63 |
| `[MI_MAX]` | 99,87 |
| `[MI_RAW_SEI]` | 118,25 |
| `[MI_BANDAS]` | verde 91 / amarillo 0 / rojo 0 |
| `[MI_VG_PROM]` | 1,29 |

**Interpretación:** umbral Microsoft = verde ≥20, amarillo 10–19, rojo <10. Los **91 métodos en verde**, promedio **69,15 = mantenibilidad ALTA**. Complejidad ciclomática promedio 1,29 (casi todo flujo lineal). Refuerza OE4: la arquitectura multicapa mantiene baja complejidad y alta mantenibilidad, consistente con el churn mínimo de A/B/M2/M3.

**Peores 5 (igual en verde):** `test_05_AddPayment` (42,6), `test_04b_SelectShippingOption` (43,3), `DataGenerator.generateCreditCardPayment` (51,6), `test_04_ConfigureShipping` (52,5), `test_03_AddItemToCart` (52,6) — los E2E largos de `CheckoutHappyPathTest`, esperable.

**Evidencia:**
- CSV crudo: `oe4-sut-artifact/metrics_method.csv` (perfil `Mi-tesis`, bloques Method/Class/Package/Module/FileType/Project).

---

## Tarea SEM — Mutación semántica: valor mentiroso (FN real + mitigación)

**Naturaleza:** mutación **semántica**. A diferencia de A/B/M2/M3 (rupturas sintácticas que rompen el
contrato observable), SEM produce un JSON **estructuralmente válido con un valor falso**. Prueba el
límite del contrato JSON Schema: el schema valida *forma*, no *verdad*.

**Ruptura:** filtro `MUTATION="SEM"` reemplaza `itemCount` por `99` **SOLO en GET /cart**
(`Oe4CartContractMutationFilter.java`, rama `case "SEM"`). El carrito recién creado tiene
`itemCount` real = `0`; SEM lo miente a `99`. El JSON sigue siendo un entero válido → el schema PASA.

**Por qué es un FN:** ningún detector asercia el *valor* de `itemCount` en GET /cart.
- `cart-schema.json` solo valida `"itemCount": { "type": "integer" }` → 99 es entero → pasa.
- Las aserciones `el campo "itemCount" debe ser igual a 1` (cart.feature, checkout.feature) operan
  sobre la respuesta del **POST `/cart/{id}/item`**, que SEM **no** muta (solo GET /cart).
- El step `realiza una solicitud GET a "/cart"` (checkout.feature:14) es decorativo no-op
  (`CommonSteps.decorativeGet`), no re-dispara nada.

### Fase 1 — Falso Negativo (schema solo)

| Token | Valor |
|---|---|
| `[DETECCION_SEM]` | No (schema-only) |
| `[FN_SEM]` | 1 |
| `[SEM_GHERKIN]` | No |

**Detector:** ninguno a nivel contrato. Suite **VERDE** (exit 0) con la mentira adentro.
Prueba dura: línea diagnóstica `[OE4-SEM] itemCount GET /cart = 99` en los 4 escenarios →
el valor `99` llegó al test y aun así pasó. El schema no distingue valor verdadero de falso.

**Evidencia:** `oe4-sut-artifact/result_SEM.txt` (exit 0; `itemCount=99` impreso ×4;
`Validacion de contrato exitosa: cart-schema.json`; body length 653 = 99 mutado).

### Fase 2 — Mitigación: aserción de valor

**Adaptación (capa de test):** agregar al escenario "Creación de un carrito nuevo"
(`cart.feature:11`) la aserción del valor real:
`And el campo "itemCount" debe ser igual a 0` (reusa el step `fieldEqualsInt` ya existente).

| Token | Valor |
|---|---|
| `[DETECCION_SEM_MIT]` | Sí (aserción de valor) |
| `[CHURN_SEM_MIT_ARCH]` | 1 |
| `[CHURN_SEM_MIT_LIN]` | 1 |
| `[CHURN_SEM_MIT_%]` | 0,05% (1/2187) |
| `[CTRL_SEM_FALSOS_POSITIVOS]` | 0 |

**Comportamiento:**
- **SEM activo + mitigación → ROJO:** la aserción caza `99 != 0`. La mitigación detecta el FN que el
  schema dejó pasar.
  Evidencia: `oe4-sut-artifact/result_SEM_mitigacion_Rojo.txt`
  (`java.lang.AssertionError: Expected: is <0> but: was <99>` @ `cart.feature:11`).
- **SEM off (`ACTIVE=false`) + mitigación → VERDE:** carrito nuevo real = `itemCount 0` → `0 == 0`
  pasa. Confirma que la aserción NO genera falso positivo (especificidad).
  Evidencia: `oe4-sut-artifact/result_SEM_mitigacion_Verde.txt`
  (exit 0; `itemCount=0` ×4; body length 651 = sin mutar).

**Interpretación:** el contrato JSON Schema es un detector de *estructura/tipo*, ciego al *valor
semántico*. Una mutación que mantiene la forma (entero válido) pero miente el dato pasa inadvertida =
FN real. La defensa es una **aserción funcional de valor** en la capa de test, cuyo costo es mínimo
(1 línea, 0,05% de churn, reusando step existente) y sin falsos positivos. Esto delimita el alcance
del contrato y justifica la combinación contrato + aserciones funcionales de la arquitectura.

### Notas de incidente (para reproducir)
- **BUG corregido en el filtro:** el `case "SEM"` original usaba `replaceFirst(..., "${1}99")`.
  `${1}` es sintaxis inválida en el reemplazo de Java (`Matcher`: un grupo nombrado debe empezar con
  letra) → lanza `IllegalArgumentException` → GET /cart devolvería 500, no 99. Corregido a
  `replaceAll("\"itemCount\"\\s*:\\s*\\d+", "\"itemCount\": 99")`.
- **Verde ambiguo:** suite verde con SEM por sí sola NO prueba el FN (podría no estar mutando). Se
  agregó la línea diagnóstica `[OE4-SEM]` en `CartSteps.userGetsNewCart` para imprimir el valor real
  y demostrar que `99` llegó al test. Esta línea es **diagnóstica, no parte de la mitigación** →
  revertir antes del cierre final (no cuenta como churn).

### Notas de cierre
- Línea diagnóstica `[OE4-SEM]` en `CartSteps.userGetsNewCart` **revertida** (no era churn; andamio de verificación).
- Estado final del filtro del SUT: `ACTIVE=false` (reposo seguro, sin mutación inyectada).
- Línea de mitigación `cart.feature:11` (`itemCount == 0`) **queda** como estado final (churn ya reportado en Fase 2).

---

## Tarea E — Agregados (resiliencia global)

**Naturaleza:** cálculo derivado, no medición. Se obtiene de A, B, M2, M3 y SEM.

**Universo de rupturas que rompen el contrato = 5:** renombrado `customer`→`cliente` (A),
renombrado `id`→`idCarrito` (B), cambio de tipo `itemCount` (M2), eliminación de `status` (M3),
y la mutación semántica `itemCount` mentido (SEM). El **control negativo C NO cuenta** como ruptura
(no altera el contrato observable; sirve para especificidad, no para detección).

| Token | Valor | Cálculo |
|---|---|---|
| `[TASA_DETECCION_GLOBAL]` (schema + funcional) | **100%** (5/5) | A+B+M2+M3 por contrato, SEM por aserción de valor |
| `[TASA_DETECCION_SCHEMA_ONLY]` | **80%** (4/5) | solo contrato JSON Schema; SEM es ciego al valor |
| `[FN_TOTAL]` (schema-only) | **1** | único FN = SEM (Fase 1) |
| `[FN_TOTAL_MITIGADO]` | **0** | tras aserción de valor de la mitigación SEM |
| `[FALSOS_POSITIVOS_TOTAL]` | **0** | control C verde + mitigación SEM sin FP |

**Interpretación:** la batería de contrato (JSON Schema) atrapa el 100% de las rupturas **sintácticas**
(tipo, llave faltante, rename de campo) → 4/5 = 80% schema-only. El 20% restante (SEM) es una mentira
de **valor** estructuralmente válida, invisible al contrato → 1 falso negativo. La defensa es una
**aserción funcional de valor** en la capa de test (costo: 1 línea, 0,05% churn), que lleva la detección
global a **5/5 = 100%** y `[FN_TOTAL_MITIGADO]`=0, sin introducir falsos positivos. Esto delimita
honestamente el alcance del contrato y justifica la arquitectura combinada **contrato + aserciones
funcionales**.
