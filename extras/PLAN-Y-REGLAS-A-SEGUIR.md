# OE4 — Plan y Reglas a Seguir

Plan de ejecución del experimento de inyección y Code Churn (OE4 de la tesis QA) y las
reglas críticas que rigen cada mutación. Documento de referencia para toda la sesión.

---

## 1. Hallazgos previos (estado del repo)

1. **El filtro ya no está en el repo.** El commit `TK004` borró `docs/` completo, incluido
   `Oe4CartContractMutationFilter.java`. Se recuperó de `683a9d1`/`bf2632c`, se amplió y
   vive en `oe4-sut-artifact/Oe4CartContractMutationFilter.java` (fuera de `src/`, no afecta el
   denominador). La copia operativa la corre el usuario dentro del SUT (`DemoSite/api/...`).
2. **Hardening commiteado como baseline.** `a560441 test(oe4): harden cart contract baseline
   (require customer key)` agrega `customer` a `required` de `cart-schema.json`. Es contrato,
   **no cuenta como churn**.
3. **Denominador 2187 CONGELADO.** Medido hoy: java 1527 + features 105 + schemas 387 + pom 163
   = 2182 (el java cayó 1532→1527 por TK004). Se conserva **2187** para TODAS las mutaciones por
   consistencia con A y B ya publicados. No se recalculan A ni B.

---

## 2. Decisiones del usuario (FINAL)

- **Denominador:** 2187 fijo para M2, M3, control y semántica. Documentar la nota baseline.
- **Semántica:** SÍ incluida. Reportar detección en dos niveles: sintáctico 4/4, semántico 0/1
  (FN esperado). Aserción de valor como mitigación → 5/5 post-mitigación, midiendo su churn.
- **Preferencia semántica:** `itemCount` que no coincide con los ítems reales (alt: `total` != suma).

---

## 3. Plan por tarea

| Tarea | Mutación | Diseño (sin tocar `.feature`) | Detector esperado |
|---|---|---|---|
| **A** | **M2 — cambio de tipo** | `itemCount` entero→string vía filtro. Verde: `cart-schema` type integer→string (+ DTO si hiciera falta). | `cart-schema` (`type`) |
| **B** | **M3 — quitar llave requerida** | Eliminar `status` del JSON. Ningún step/feature de cart lo lee (los `status` de CheckoutSteps son de otro endpoint no mutado) → schema único detector. Verde: sacar `status` de `required`+`properties` (+ DTO). | `cart-schema` (`missing`) |
| **C** | **Control negativo** | Renombrar una variable **interna** del controller del SUT (no cambia el JSON). Corre la suite → debe quedar VERDE sin tocar nada. | — (especificidad: 0 falsos positivos) |
| **D** | **Índice de Mantenibilidad** | Manual en IntelliJ (Analyze → Calculate Metrics). Declarar variante de fórmula del plugin. | — |
| **E** | **Agregados** | Se calculan al final. Base 4 rupturas (A, B, M2, M3). `TASA = detectadas/4`; `FN_TOTAL = FN_M2+FN_M3`. Con semántica: 5 rupturas, schema-only 4/5 = 80%. | — |
| **SEM** | **Semántica** | `itemCount` mentiroso SOLO en GET /cart (escenario "carrito nuevo", que no asercia su valor) → schema pasa = **FN real**. Mitigación: aserción `itemCount == nº ítems reales` (mide churn). | Schema pasa (FN); mitigación lo caza |

### Fórmulas
- `CHURN_% = (líneas_modificadas / 2187) * 100`
- `FN = 0` si la ruptura fue detectada; `1` si pasó inadvertida.

---

## 4. Reglas Críticas (R1–R5) — NO ROMPER

- **R1.** Cada mutación parte de repo **limpio** (`git status` limpio) y al terminar se **resetea**
  (`git checkout -- src/`). Las mediciones quedan aisladas entre sí.
- **R2.** Al volver a VERDE, modificar **SOLO capas técnicas**: `schemas/`, `dto/`, `clients/` y, como
  máximo, **tokens literales del nombre del campo** en las aserciones de `steps/`. **NUNCA** el flujo
  de negocio de los `.feature` (Dado/Cuando). Central para HE3.
- **R3.** Como se mide churn, **cambio MÍNIMO** rojo→verde. Sin refactors oportunistas ni formateos.
- **R4.** Verificar que los features quedaron intactos: `git diff -- '*.feature'` (esperado: sin cambios).
- **R5.** La inyección se hace en el SUT vía `Oe4CartContractMutationFilter` (toggle `ACTIVE` +
  `MUTATION`), una mutación a la vez. No alterar la lógica de negocio real del SUT salvo el control
  negativo (Tarea C).

---

## 5. Protocolo por mutación (ciclo)

1. `git status` — confirmar repo limpio.
2. Activar la mutación en el filtro (`MUTATION="..."`) y reiniciar Broadleaf (módulo `api`).
3. Ejecutar la suite (`RunCucumberTest`). Capturar **ROJO** (debe detectar la ruptura).
4. Adaptar SOLO la capa técnica hasta volver a **VERDE** (R2/R3).
5. Medir churn: `git --no-pager diff --stat -- src/` y `git --no-pager diff -- src/`.
6. **Guardar evidencia ANTES de resetear** (sobrevive al checkout):
   - Diff → `evidencia/<MUT>_*.diff`
   - Logs ROJO y VERDE → `evidencia/<MUT>_ROJO_suite.txt`, `evidencia/<MUT>_VERDE_suite.txt`
   - Bloque de tokens → `RESULTADOS_EXPERIMENTO.md` (acumulador, NO se resetea).
7. Resetear: `git checkout -- src/`.

> **Pasos que el usuario ejecuta** (no autónomos para la CLI): copiar el filtro al SUT, reiniciar
> Broadleaf, correr `RunCucumberTest`, pegar ROJO/VERDE, control negativo (editar variable interna),
> y MI en IntelliJ.
> **Lo que hace la CLI:** ampliar el filtro, editar capa técnica para ir a verde, medir churn,
> guardar evidencia y armar las tablas de resultados.

---

## 6. Stop-and-ask (no adivinar) si:
- (a) volver a verde exigiría modificar un `.feature`;
- (b) el denominador 2187 no coincide con lo medido (ya resuelto: se congela 2187);
- (c) la variante de MI es ambigua;
- (d) hay que decidir si incluir la mutación semántica (ya resuelto: SÍ).

---

## 7. Entregable final
Bloque de RESULTADOS que mapee cada token a su valor + ruta de evidencia, para pegar de vuelta en
el chat académico. Vive en `RESULTADOS_EXPERIMENTO.md`.
Tokens: `[CHURN_M2_*]`, `[CHURN_M3_*]`, `[CTRL_*]`, `[MI_VALOR]`, `[TASA_DETECCION_GLOBAL]`,
`[FN_TOTAL]`, `[DETECCION_SEM]`, `[FN_SEM]`. Notas: variante de MI; denominador verificado;
semántica incluida = Sí.
