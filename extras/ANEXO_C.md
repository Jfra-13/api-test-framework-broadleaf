# Anexo C — Evidencias de ejecución por operador de mutación

Cada operador de mutación se midió sobre la suite de contrato (JSON Schema) +
escenarios funcionales, ejecutada contra el SUT Broadleaf (`localhost:7445`).
La inyección se realiza con el filtro `Oe4CartContractMutationFilter`
(`MUTATION` selecciona el operador; `ACTIVE=false` desactiva). El Code Churn se
mide con `git diff --stat` sobre un denominador **congelado = 2187 líneas**
(`CHURN_% = líneas / 2187 · 100`).

## Tabla maestra — resultados por operador

| Operador | Ruptura | Estado suite | Churn (arch / lín / %) | Detección | FN | Detector |
|---|---|---|---|---|---|---|
| **A** | `customer` → `cliente` | ROJO → VERDE | 1 / 1 / 0,05% | Sí | 0 | JSON Schema |
| **B** | `id` → `idCarrito` | ROJO → VERDE | 4 / 5 / 0,23% | Sí | 0 | Funcional + cascada |
| **M2** | `itemCount` entero → string | ROJO → VERDE | 1 / 1 / 0,05% | Sí | 0 | JSON Schema |
| **M3** | eliminar llave `status` | ROJO → VERDE | 1 / 2 / 0,09% | Sí | 0 | JSON Schema (`required`) |
| **SEM** (Fase 1) | `itemCount` = 99 (valor falso) | VERDE | — | **No** | **1** | ninguno (FN real) |
| **SEM** (Fase 2, mitigación) | aserción de valor `itemCount == 0` | ROJO → VERDE | 1 / 1 / 0,05% | Sí | 0 | aserción funcional |
| **Control C** | rename de variable interna del controller | VERDE | 0 / 0 / 0,00% | — (0 FP) | — | especificidad |

**Estado suite** = comportamiento esperado: ROJO con la mutación activa (la
batería caza la ruptura), VERDE tras la adaptación de la capa correspondiente.
El control negativo se espera VERDE estable (no debe disparar falsos positivos).

## Mapa de archivos de evidencia (`oe4-sut-artifact/` y `evidencia/`)

| Operador | Archivo de consola | Contenido clave |
|---|---|---|
| **M2** | `oe4-sut-artifact/result_M2.txt` | diff + ROJO (`instance type (string) does not match (allowed: ["integer"])` @ `/properties/itemCount`) + VERDE (`Validacion de contrato exitosa`) |
| **M3** | `oe4-sut-artifact/result_M3.txt` | diff + ROJO (`object has missing required properties (["status"])` ×3) + VERDE |
| **SEM** | `oe4-sut-artifact/result_SEM.txt` | VERDE con `itemCount=99` impreso ×4 → FN confirmado |
| **SEM mitigación (rojo)** | `oe4-sut-artifact/result_SEM_mitigacion_Rojo.txt` | `AssertionError: Expected: is <0> but: was <99>` @ `cart.feature:11` |
| **SEM mitigación (verde)** | `oe4-sut-artifact/result_SEM_mitigacion_Verde.txt` | VERDE con `itemCount=0` ×4 |

## Operadores sin captura de consola propia (A, B, control C)

A, B y el control negativo **no tienen archivo `.txt` de consola individual**. Sus
valores están medidos y documentados (`RESULTADOS_EXPERIMENTO.md`), y usan el
**mismo mecanismo de inyección por filtro** que M2/M3/SEM. Como evidencia visual
del par rojo/verde se reusan las capturas de M2 y M3 (operadores sintácticos
equivalentes: rename de campo y cambio estructural), que muestran idéntico patrón
de detección por contrato.

> Nota de integridad: el texto del Anexo que lista `result_A`, `result_B` y
> `result_C` como archivos en `oe4-sut-artifact/` debe ajustarse — esos archivos
> no se generaron. La evidencia de A/B/control vive en la tabla maestra (valores
> medidos) y se ilustra con las capturas de M2/M3.

## Agregados de resiliencia (Tarea E)

| Métrica | Valor |
|---|---|
| Tasa de detección global (schema + funcional) | **100%** (5/5) |
| Tasa de detección schema-only | **80%** (4/5) |
| Falsos negativos schema-only | **1** (único = SEM) |
| Falsos negativos mitigados | **0** |
| Falsos positivos totales | **0** (control C + mitigación SEM) |
