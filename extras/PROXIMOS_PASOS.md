# Próximos pasos — Robustez de evidencia y resultados (OE4)

Hoja de ruta para fortalecer la evidencia experimental más allá del alcance
actual (diseño pre-experimental, un solo SUT, framework desacoplado medido en
aislamiento). Cada ítem indica **qué limitación ataca** y **qué hipótesis o
figura refuerza**. Ordenado por relación valor/esfuerzo.

Estado actual congelado: línea base con contrato endurecido (commit `a560441`),
denominador de Code Churn = **2 187 líneas**. Evidencia cruda en
`oe4-sut-artifact/`; bloques para captura en `oe4-capturas/`.

---

## 1. Medir la línea base ACOPLADA (con vs sin framework) — ALTA prioridad

**Limitación que ataca:** hoy solo se midió el framework desacoplado; el
contraste "con vs sin arquitectura" es cualitativo (se argumenta vía Ojdanic et
al., 2023, en 7.2), no cuantitativo.

**Qué hacer:**
- Implementar una suite "acoplada" de referencia: validación campo por campo
  (sin JSON Schema, sin DTOs, sin capa cliente reutilizable), replicando los
  mismos casos funcionales.
- Inyectar los **mismos 5 operadores** de mutación con el mismo filtro
  (`Oe4CartContractMutationFilter`).
- Medir Code Churn de adaptación con `git diff --stat` en ambas suites.

**Qué habilita:** un gráfico de barras **con vs sin framework** con datos
REALES (no estimados) — el resultado más contundente para demostrar el valor del
desacople. Refuerza HE1 y HE3.

> Nota de validez: NO graficar "sin framework" con valores estimados. Sin
> medición real, mantener el contraste como argumento cualitativo.

## 2. Aumentar el número de mediciones / operadores — ALTA prioridad

**Limitación que ataca:** el bajo n impide inferencia estadística paramétrica
(declarado en 7.2); hoy la valoración se apoya en umbrales de referencia.

**Qué hacer:**
- Ampliar el catálogo de operadores de mutación: campos anidados, arrays,
  cambios de cardinalidad, `enum`, nullability, y rupturas a nivel de
  `status code` / headers HTTP (no solo body).
- Repetir mediciones para estimar dispersión.

**Qué habilita:** estadística descriptiva más sólida (medias, rangos) y, con
suficiente n, pruebas no paramétricas. Refuerza HE1 y HE2.

## 3. Validez externa: segundo SUT — MEDIA prioridad

**Limitación que ataca:** validez externa acotada a un único sistema (Broadleaf).

**Qué hacer:** replicar el protocolo de evolución de contratos sobre otra API
REST (idealmente de otro dominio). Comparar churn, detección y MI.

**Qué habilita:** generalización de las conclusiones más allá de un SUT.

## 4. Automatizar la medición y la reproducibilidad — MEDIA prioridad

**Limitación que ataca:** hoy la medición por mutación es manual (activar filtro
→ correr → `git diff --stat` → capturar). Riesgo de error y baja
reproducibilidad.

**Qué hacer:**
- Script que, por cada operador: aplica la mutación, corre la suite, captura
  exit code (verde/rojo), corre `git diff --stat`, y emite la fila de la tabla
  + el dato para la figura.
- Registrar metadatos de entorno: versión de Broadleaf, JDK, commit del SUT,
  hash de cada `.txt` de evidencia.

**Qué habilita:** Tablas 4 y 5 regeneradas con un comando; trazabilidad
auditable de cada número reportado.

## 5. Ampliar la detección semántica — MEDIA prioridad

**Limitación que ataca:** la validación por esquema es ciega al valor (el falso
negativo semántico). Hoy se mitiga con UNA aserción funcional puntual.

**Qué hacer:**
- Más aserciones funcionales de valor en escenarios clave.
- Evaluar contract testing dirigido por el consumidor (p. ej. Pact) y/o
  property-based testing para cubrir el espacio de valores.

**Qué habilita:** delimitar con más finura la frontera "forma vs significado".
Refuerza HE2 y la validez de constructo de la resiliencia.

## 6. Estabilidad longitudinal (anti-flakiness) — BAJA prioridad

**Limitación que ataca:** la estabilidad se afirma puntual; no se observó en el
tiempo (contraste con Micco, 2016).

**Qué hacer:** correr la suite en CI de forma repetida y registrar el historial
verde/rojo para evidenciar determinismo (ausencia de transiciones inestables).

## 7. Índice de Mantenibilidad por módulo y por variante — BAJA prioridad

**Qué hacer:** desglosar el MI (hoy 69,15 global) por paquete/capa y reportar
explícitamente la(s) variante(s) de cálculo, en línea con Heričko y Šumak
(2023). Permite ver qué capa concentra el riesgo de mantenibilidad.

---

## Higiene de evidencia (aplicar siempre)

- Versionar los `.txt` de evidencia junto al commit del SUT que los produjo.
- Anotar en cada captura: operador, `MUTATION` activo, exit code, y `git diff
  --stat`.
- Criterio de Code Churn declarado y constante: definir si se cuenta el cambio
  **mínimo** para volver a verde (criterio de la Tabla 4) o el renombrado
  **completo** de todas las apariciones. No mezclar criterios entre operadores.
- Tras medir, revertir siempre a la línea base (`git checkout -- src/test/`,
  diff vacío) antes del siguiente operador.
