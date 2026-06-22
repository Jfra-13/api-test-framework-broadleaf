# Carpeta `usar/` — Material para el documento final de tesis (OE4)

Esta carpeta concentra **todo lo necesario** para redactar el OE4 (Objetivo Específico 4):
inyección de rupturas de contrato, Code Churn, Índice de Mantenibilidad y resiliencia
(detección / falsos negativos). Cada valor (token entre corchetes) está respaldado por
su evidencia (salida real de la corrida, diff o CSV).

> El experimento NO mide "tiempo manual vs automático". Mide **valor arquitectónico**:
> mantenibilidad (churn + MI) y resiliencia (detección + FN) de un framework de pruebas
> multicapa (features → steps → clients → dto → schemas) sobre Broadleaf Commerce.

---

## 1. Cómo usar esta carpeta

1. Empezá por **`RESULTADOS_EXPERIMENTO.md`**: es el documento maestro con cada mutación,
   su tabla de tokens, el detector y la interpretación. Las rutas que cita resuelven
   dentro de esta misma carpeta (`evidencia/...`, `oe4-sut-artifact/...`).
2. Para cada token que pegues en la tesis, abrí el archivo de evidencia indicado en la
   tabla de abajo y citalo (es la prueba reproducible que el jurado puede pedir).

---

## 2. Estructura

| Ruta | Contenido |
|---|---|
| `RESULTADOS_EXPERIMENTO.md` | Documento maestro: todas las mutaciones, tokens, detectores e interpretación |
| `evidencia/` | Salidas ROJO/VERDE y diffs de las mutaciones sintácticas M2 y M3 |
| `oe4-sut-artifact/` | Artefacto de inyección (filtro Spring), evidencia SEM, y CSV del Índice de Mantenibilidad |
| `sut-source/` | Volcados de código del SUT (Broadleaf) usados como referencia del contrato real |
| `img.png`, `img_1.png` | Capturas de apoyo |

---

## 3. Mapa de tokens → valor → evidencia

### Mantenibilidad — Code Churn (denominador congelado = 2187 líneas)

| Token | Valor | Evidencia |
|---|---|---|
| `[CHURN_M2_ARCH]` / `[CHURN_M2_LIN]` / `[CHURN_M2_%]` | 1 / 1 / 0,05% | `evidencia/M2_cambio_tipo.diff` |
| `[CHURN_M3_ARCH]` / `[CHURN_M3_LIN]` / `[CHURN_M3_%]` | 1 / 2 / 0,09% | `evidencia/M3_quitar_status.diff` |
| `[CHURN_SEM_MIT_LIN]` / `[CHURN_SEM_MIT_%]` | 1 / 0,05% | `RESULTADOS_EXPERIMENTO.md` (Tarea SEM, Fase 2) |
| `[CTRL_CHURN_LIN]` / `[CTRL_CHURN_%]` | 0 / 0,00% | `oe4-sut-artifact/` (Tarea C, sin diff: 0 cambios) |

> Referencia (medidas en sesiones previas): mutación A `customer`→`cliente` = 1 archivo / 1 línea;
> mutación B `id`→`idCarrito` = 4 archivos / 5 líneas.

### Mantenibilidad — Índice de Mantenibilidad (MI)

| Token | Valor | Evidencia |
|---|---|---|
| `[MI_VALOR]` | 69,15 (promedio 91 métodos, MI clásico VS normalizado) | `oe4-sut-artifact/metrics_method.csv` |
| `[MI_BANDAS]` | verde 91 / amarillo 0 / rojo 0 | `oe4-sut-artifact/metrics_method.csv` |
| `[MI_VG_PROM]` | 1,29 | `oe4-sut-artifact/metrics_method.csv` |

### Resiliencia — Detección y Falsos Negativos

| Token | Valor | Evidencia |
|---|---|---|
| `[DETECCION_M2]` / `[FN_M2]` | Sí / 0 | `evidencia/M2_ROJO_suite.txt` (rojo) + `evidencia/M2_VERDE_suite.txt` (verde) |
| `[DETECCION_M3]` / `[FN_M3]` | Sí / 0 | `evidencia/M3_ROJO_suite.txt` + `evidencia/M3_VERDE_suite.txt` |
| `[DETECCION_SEM]` / `[FN_SEM]` | No / 1 | `oe4-sut-artifact/result_SEM.txt` (verde con valor mentido = FN real) |
| `[DETECCION_SEM_MIT]` | Sí | `oe4-sut-artifact/result_SEM_mitigacion_Rojo.txt` (caza 99≠0) + `..._Verde.txt` (sin FP) |
| `[CTRL_RESULTADO]` / `[CTRL_FALSOS_POSITIVOS]` | VERDE / 0 | Tarea C en `RESULTADOS_EXPERIMENTO.md` |

### Resiliencia — Agregados (Tarea E)

| Token | Valor |
|---|---|
| `[TASA_DETECCION_GLOBAL]` (contrato + funcional) | 100% (5/5) |
| `[TASA_DETECCION_SCHEMA_ONLY]` | 80% (4/5) |
| `[FN_TOTAL]` (schema-only) | 1 (único FN = SEM) |
| `[FN_TOTAL_MITIGADO]` | 0 |
| `[FALSOS_POSITIVOS_TOTAL]` | 0 |

Universo de rupturas = 5 (A, B, M2, M3, SEM). El control negativo C **no** cuenta como ruptura.

---

## 4. Verificaciones metodológicas (por si el jurado pregunta)

- **Denominador 2187:** congelado para todas las mutaciones por consistencia con A y B ya
  reportadas. Nota: el repo hoy mide 2182 tras TK004 (se borró `docs/`); se conserva 2187.
  Detalle en el encabezado de `RESULTADOS_EXPERIMENTO.md`.
- **Variante de MI:** MI clásico de 3 factores, normalizado Visual Studio (sin factor de
  comentarios): `MI = max(0, (171 − 5.2·ln(V) − 0.23·v(G) − 16.2·ln(LOC)) · 100/171)`.
  Agregación = promedio por método. Plugin: MetricsReloaded (IntelliJ).
- **Inyección de rupturas:** filtro Spring `Oe4CartContractMutationFilter` (en
  `oe4-sut-artifact/`), una mutación a la vez, activable por configuración. Estado de reposo:
  `ACTIVE=false`. La lógica de negocio real del SUT no se altera (salvo el control negativo C).
- **Regla de oro del churn:** cada adaptación a verde toca SOLO capas técnicas
  (schemas/dto/clients y, como máximo, tokens literales en aserciones de steps). Los `.feature`
  de negocio quedan intactos → sustenta la hipótesis HE3.
