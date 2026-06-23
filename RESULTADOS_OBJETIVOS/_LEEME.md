# oe4-capturas — Bloques listos para captura (Anexo C)

Cada `.txt` es un bloque enfocado (≥30 líneas) con un solo estado de la suite,
pensado para tomarle UNA captura. Abrí el archivo, encuadrá ~30 líneas, capturá.

## Listos para capturar (10 de 10) — COMPLETO

| # | Archivo | Operador | Estado | Qué se ve |
|---|---|---|---|---|
| 1 | `01_M2_rojo.txt` | M2 cambio de tipo | ROJO | `instance type (string) does not match (allowed: ["integer"])` |
| 2 | `02_M2_verde.txt` | M2 (schema adaptado) | VERDE | `Validacion de contrato exitosa` + exit 0 |
| 3 | `03_M3_rojo.txt` | M3 quitar `status` | ROJO | `object has missing required properties (["status"])` ×3 |
| 4 | `04_M3_verde.txt` | M3 (schema adaptado) | VERDE | `Validacion de contrato exitosa` + exit 0 |
| 5 | `05_SEM_FN_verde.txt` | SEM valor mentido (99) | VERDE = FN | `itemCount=99` llega y el contrato igual PASA |
| 6 | `06_SEM_mitigacion_rojo.txt` | SEM + aserción de valor | ROJO | `Expected: is <0> but: was <99>` |
| 7 | `07_SEM_mitigacion_verde.txt` | SEM off + aserción | VERDE | `itemCount=0`, sin falso positivo |
| 8 | `08_control_verde.txt` | control negativo | VERDE | suite verde + `git diff --stat` vacío = churn 0 |
| 9 | `09_A_rojo.txt` | A `customer`→`cliente` | ROJO | `missing required properties (["customer"])` ×3 |
| 10 | `10_A_verde.txt` | A (schema adaptado) | VERDE | suite verde + churn 2 líneas / 1 archivo |
| 11 | `11_B_rojo.txt` | B `id`→`idCarrito` | ROJO | Gherkin + schema + cascada: Failures 5, Errors 3 |
| 12 | `12_B_verde.txt` | B (suite adaptada) | VERDE | suite verde + churn 7 líneas / 5 archivos |

## ESTADO: COMPLETO (10/10) — 2026-06-22

A y B capturados en vivo contra Broadleaf (mutación activada por el usuario,
suite corrida desde acá). Resultados finales:

- **A (`customer → cliente`)**: ROJO por schema (`missing ["customer"]` ×3) →
  VERDE adaptando solo `cart-schema.json`. Churn = **2 líneas / 1 archivo**.
  A_GHERKIN = No (la feature no nombra `customer`).
- **B (`id → idCarrito`)**: ROJO por Gherkin + schema + cascada (Failures 5,
  Errors 3: el id se consume como pathParam aguas abajo) → VERDE adaptando
  schema + 2 features + CartSteps + CheckoutHappyPathTest. Churn = **7 líneas /
  5 archivos**. B_GHERKIN = Sí.

Contraste clave de la tesis: renombrar `id` (campo acoplado a varias capas)
cuesta ~3,5× más churn que renombrar `customer` (solo en el contrato).

Baseline revertido tras medir (`git checkout -- src/test/`, diff vacío).

## Code Churn (git diff --stat)

- M2 y M3: el diff del schema está en la sección `----- DIFF -----` de
  `oe4-sut-artifact/result_M2.txt` y `result_M3.txt`.
- Control: `git diff --stat` vacío = 0 (ya verificado, ver archivo 08).
- A y B: se capturan en el paso 4 de cada uno.

## Nota de tipografía

Los archivos 1–4 y 6–7 vienen de la consola de IntelliJ (UTF-8 limpio). El 5 y 8
fueron compuestos con los valores reales de la corrida; si querés acentos
perfectos en A/B, capturá directo desde la consola de IntelliJ.
