# 📸 GUÍA COMPLETA DE TOMA DE EVIDENCIAS (OE1 → OE4)

> Guía única y detallada para capturar **todas** las evidencias de la tesis en una sola
> jornada. Pensada para alguien que no recuerda dónde está cada cosa: se indica **en qué
> proyecto**, **en qué consola**, **la sintaxis exacta** y **qué screenshot tomar**.
>
> Léela de arriba a abajo: el orden está optimizado para no repetir trabajo.

---

## 🧭 0. ANTES DE EMPEZAR (leer 1 vez)

### 0.1 Los DOS proyectos (no los confundas)

| Símbolo | Proyecto | Ruta | Qué corre |
|---|---|---|---|
| 🟥 **SUT** | Broadleaf (el "paciente") | `C:\software\projects\TESIS_QA\WEBSITE\DemoSite` | El módulo **`api`** (clase `ApiApplication`) |
| 🟦 **FRAMEWORK** | Tus pruebas (tu "producto") | `C:\software\projects\TESIS_QA\AutomationFramework\broadleaf-api-test` | La clase **`RunCucumberTest`** |

### 0.2 Las consolas (cuándo usar cuál)

- **IntelliJ IDEA** → para **levantar Broadleaf** y para **correr las pruebas** (NO tienes Maven en el PATH, así que todo lo Java/Maven va por el IDE).
- **Git Bash** (la que ya usas, se ve `MINGW64`) → para los comandos **`git`** (OE4) y **`ngrok`** (OE3). **Recomendada.**
- **PowerShell** / **CMD** → sirven igual para `git` y `ngrok`; solo cambia cómo te mueves de carpeta (ver abajo).

> 💡 Los comandos `git` y `ngrok` son **idénticos** en Git Bash, PowerShell y CMD. Lo único
> que cambia entre consolas es **cómo entras a la carpeta** (`cd`) y los **separadores de ruta**.

**Cómo abrir una consola YA parada en el proyecto del framework:**

| Consola | Comando para entrar a la carpeta |
|---|---|
| Git Bash | `cd /c/software/projects/TESIS_QA/AutomationFramework/broadleaf-api-test` |
| PowerShell | `cd C:\software\projects\TESIS_QA\AutomationFramework\broadleaf-api-test` |
| CMD | `cd /d C:\software\projects\TESIS_QA\AutomationFramework\broadleaf-api-test` |

> Atajo: en el Explorador de Windows, abre la carpeta del framework, haz **clic derecho →
> "Git Bash Here"** y ya quedas dentro.

### 0.3 Carpeta para guardar las evidencias (recomendado)

Crea UNA carpeta fuera de los repos para no ensuciarlos, por ejemplo:
```
C:\software\projects\TESIS_QA\EVIDENCIAS-TESIS\
   ├── OE1\
   ├── OE2\
   ├── OE3\
   └── OE4\
```
**Convención de nombres** para los screenshots (así el anexo queda ordenado):
`OE1_01_swagger.png`, `OE1_02_get-cart.png`, `OE2_01_estructura.png`, etc.

### 0.4 Herramienta de captura
- **Recorte de pantalla de Windows:** tecla `Win + Shift + S` (selecciona área → se copia → pega en Paint/Word y guarda como PNG).
- Para capturar una consola completa también sirve guardar el texto en `.txt`.

### 0.5 Orden recomendado de la jornada
```
OE1 (Swagger/Postman)  →  OE2 (estructura + suite verde + Allure)
   →  OE3 (ngrok + pipeline en la nube)  →  OE4 (experimento de churn)
```
> ⚠️ **Importante:** OE4 **muta** el SUT. Haz OE1, OE2 y OE3 con el SUT **limpio**
> (sin el filtro de mutación), y deja OE4 para el final.

---

## ✅ CHECKLIST MAESTRO (marca al terminar)

- [ ] OE1 — Swagger arriba + 6 endpoints + (opcional Postman) + schemas
- [ ] OE2 — Estructura del proyecto + suite VERDE + reporte Allure local
- [ ] OE3 — ngrok + Secret + pipeline VERDE en Actions + reporte en GitHub Pages
- [ ] OE4 — Mutación A (rojo→verde→diff) + Mutación B (rojo→verde→diff) + comparación

---

# 🟢 OE1 — Línea Base y Análisis de Endpoints

**Meta:** evidenciar que exploraste y documentaste los contratos de la API.

### Paso 1.1 — Levantar el SUT (Broadleaf) 🟥 (IntelliJ)
1. Abre IntelliJ con el proyecto **`DemoSite`** (`C:\software\projects\TESIS_QA\WEBSITE\DemoSite`).
2. Levanta el módulo **`api`** como siempre lo haces (Run de la app / `ApiApplication`).
3. Espera a que termine de arrancar (en la consola de IntelliJ verás "Started ... in X seconds").

### Paso 1.2 — Swagger UI (el mapa de la API) 📸
1. En el navegador abre:
   ```
   https://localhost:7445/api/v1/swagger-ui.html
   ```
   > ⚠️ Es **HTTPS con certificado propio**: el navegador mostrará "Tu conexión no es
   > privada". Haz clic en **Configuración avanzada → Continuar a localhost (no seguro)**.
   > *(Alternativa sin warning, por HTTP:* `http://localhost:7082/api/v1/swagger-ui.html` *)*
2. 📸 **`OE1_01_swagger.png`** — la lista de controladores/endpoints.
3. Despliega los endpoints clave y captura cada uno (request/response model):
   - `GET /cart` → 📸 `OE1_02_swagger-cart.png`
   - `GET /catalog/search` → 📸 `OE1_03_swagger-catalog.png`
   - `POST /cart/{cartId}/item` → 📸 `OE1_04_swagger-additem.png`

### Paso 1.3 — Respuestas reales (navegador o Postman) 📸
**Opción rápida (navegador), GET directos:**
- `https://localhost:7445/api/v1/cart` → 📸 `OE1_05_get-cart-json.png` (el JSON del carrito)
- `https://localhost:7445/api/v1/catalog/search?q=hot` → 📸 `OE1_06_get-catalog-json.png`

**Opción completa (Postman)** — para mostrar el flujo POST/PUT con cuerpos:
1. Abre Postman, crea la colección **"Broadleaf Tesis - Baseline"**.
2. Replica estas llamadas y captura request + response de cada una:
   | # | Método | URL |
   |---|---|---|
   | 1 | GET | `https://localhost:7445/api/v1/cart` |
   | 2 | GET | `https://localhost:7445/api/v1/catalog/search?q=hot` |
   | 3 | POST | `https://localhost:7445/api/v1/cart/1/item`  (body: `{"quantity":1,"skuId":13}`) |
   | 4 | PUT | `https://localhost:7445/api/v1/shipping/1/1/address` (body de dirección) |
   | 5 | POST | `https://localhost:7445/api/v1/cart/checkout/payment?cartId=1` (body de pago) |
   | 6 | POST | `https://localhost:7445/api/v1/cart/checkout?cartId=1` (body vacío `{}`) |
   - 📸 `OE1_07_postman_*.png` (una por llamada).
   > En Postman, para el certificado propio: **Settings → General → SSL certificate
   > verification = OFF**.

### Paso 1.4 — Los contratos documentados 📸 (en el FRAMEWORK 🟦)
Abre y captura (son tus "contratos base", anexos de OE1):
- `docs/API-BASELINE.md` → 📸 `OE1_08_baseline-doc.png`
- `src/test/resources/schemas/cart-schema.json` → 📸 `OE1_09_cart-schema.png`
- `src/test/resources/schemas/checkout-schema.json` → 📸 `OE1_10_checkout-schema.png`

✅ **OE1 listo.** Deja Broadleaf **encendido** para OE2.

---

# 🟢 OE2 — Arquitectura del Framework (BDD multicapa)

**Meta:** evidenciar la arquitectura en capas y que la suite corre verde con contract testing.

### Paso 2.1 — La estructura del proyecto 📸 (IntelliJ, FRAMEWORK 🟦)
1. Abre IntelliJ con el proyecto **`broadleaf-api-test`**.
2. En el panel **Project** (izquierda), expande:
   `src/test/java/com/tesis/automation/` y `src/test/resources/`.
3. 📸 **`OE2_01_estructura.png`** — que se vean las carpetas `clients/`, `dto/`, `steps/`,
   `hooks/`, `runners/`, `utils/`, `features/`, `schemas/`.

### Paso 2.2 — Una muestra de cada capa 📸
Abre y captura un archivo representativo de cada capa (evidencia del desacople):
- Feature (Gherkin): `src/test/resources/features/checkout.feature` → 📸 `OE2_02_feature.png`
- Step: `src/test/java/com/tesis/automation/steps/CheckoutSteps.java` → 📸 `OE2_03_steps.png`
- Client (RestAssured): `src/test/java/com/tesis/automation/clients/BaseApiClient.java` → 📸 `OE2_04_client.png`
- DTO (Lombok): `src/test/java/com/tesis/automation/dto/OrderPayment.java` → 📸 `OE2_05_dto.png`

### Paso 2.3 — Correr la suite y verla VERDE 📸 (IntelliJ)
1. En el Project, navega a:
   `src/test/java/com/tesis/automation/runners/RunCucumberTest.java`
2. **Clic derecho sobre el archivo → `Run 'RunCucumberTest'`.**
   *(Atajo: con el archivo abierto, `Ctrl+Shift+F10`.)*
3. Espera a que termine. En la ventana **Run** (abajo) verás el árbol de escenarios en **verde**
   y "Process finished with exit code 0".
4. 📸 **`OE2_06_suite-verde.png`** — el árbol de escenarios verde + la consola.
   > Si quieres el texto: clic en la consola → `Ctrl+A`, `Ctrl+C` → pégalo en
   > `OE2_06_suite-verde.txt`.

### Paso 2.4 — Reporte Allure local (sin Maven en PATH) 📸
El run anterior generó resultados crudos en la carpeta `allure-results/` del framework.
Para ver el reporte **usa el Maven que trae IntelliJ** (no necesitas `mvn` instalado):

1. Abre la herramienta Maven: **View → Tool Windows → Maven** (o el ícono **`m`** en el
   borde derecho de IntelliJ).
2. En el panel Maven, haz clic en el ícono **"Execute Maven Goal"** (un `m` con una flecha)
   y escribe exactamente:
   ```
   allure:serve
   ```
   y Enter. *(También puedes expandir `broadleaf-api-test → Plugins → allure →` doble clic
   en `allure:serve`.)*
3. IntelliJ descarga Allure y **abre el reporte en el navegador** automáticamente.
4. Captura:
   - Pestaña **Overview** (gráfico de pass/fail) → 📸 `OE2_07_allure-overview.png`
   - Pestaña **Suites / Behaviors** (los escenarios) → 📸 `OE2_08_allure-suites.png`
   - Un escenario abierto con sus pasos → 📸 `OE2_09_allure-detalle.png`

> 🔧 **Si `allure:serve` falla** (p. ej. sin internet para descargar Allure): usa como
> evidencia de reporte el publicado en la nube de OE3 (más abajo, Paso 3.7). Es el mismo
> framework.

✅ **OE2 listo.**

---

# 🟢 OE3 — Integración Continua (CI/CD en la nube)

**Meta:** evidenciar que la suite corre **desatendida en GitHub Actions** y publica el
reporte Allure en **GitHub Pages**.

> Necesitas **a la vez**: tu PC encendida, **Broadleaf arriba** 🟥 y **ngrok** corriendo,
> porque la nube ejecuta las pruebas contra TU Broadleaf local a través del túnel.

### Datos de tu repositorio (confírmalos)
- Repo: `https://github.com/Jfra-13/api-test-framework-broadleaf`
- Reporte publicado (Pages): `https://jfra-13.github.io/api-test-framework-broadleaf/`
- Workflow: **"CI - Pruebas de API Broadleaf"** (dispara con push a `main`/`master` y con botón manual).

### Paso 3.1 — Broadleaf arriba 🟥
Asegúrate de que el módulo `api` esté corriendo en `https://localhost:7445` (como en OE1).

### Paso 3.2 — Levantar el túnel ngrok (consola: Git Bash / PowerShell / CMD)
> Si es la primera vez, instala y autentica ngrok una sola vez:
> ```bash
> ngrok config add-authtoken TU_AUTHTOKEN     # el token está en tu dashboard de ngrok
> ```
Abre una consola (cualquiera) y ejecuta — **mismo comando en las tres consolas**:
```bash
ngrok http https://localhost:7445
```
ngrok te mostrará una línea **Forwarding** como:
```
Forwarding   https://a1b2-xxxx.ngrok-free.app  ->  https://localhost:7445
```
1. 📸 **`OE3_01_ngrok.png`** — la ventana de ngrok con la URL pública.
2. **Copia esa URL pública** (la `https://....ngrok-free.app`). **Déjala corriendo** (no cierres la consola).

### Paso 3.3 — Configurar el Secret `BASE_URL` en GitHub 📸
1. En el navegador, ve a tu repo → **Settings → Secrets and variables → Actions**.
2. Si ya existe `BASE_URL`, clic en **Update**; si no, **New repository secret**.
3. **Name:** `BASE_URL`
4. **Value:** la URL de ngrok **+ `/api/v1`**, por ejemplo:
   ```
   https://a1b2-xxxx.ngrok-free.app/api/v1
   ```
   > ⚠️ El `/api/v1` final es **obligatorio**.
5. **Add/Update secret.** 📸 `OE3_02_secret.png` (sin mostrar el valor, solo que existe).

### Paso 3.4 — Disparar el pipeline (botón manual) 📸
1. En tu repo → pestaña **Actions**.
2. En la izquierda elige el workflow **"CI - Pruebas de API Broadleaf"**.
3. Botón **Run workflow** → (rama `master`) → **Run workflow**.
4. 📸 `OE3_03_run-workflow.png`

### Paso 3.5 — Ver la corrida y los 2 jobs 📸
1. Entra a la corrida que acaba de iniciar.
2. Verás 2 jobs encadenados:
   - **Ejecutar pruebas y generar reporte Allure**
   - **Publicar reporte en GitHub Pages**
3. Espera a que ambos queden en **verde** (✓). (~2–5 min.)
4. 📸 `OE3_04_pipeline-verde.png` — los dos jobs en verde.
5. Abre el job 1 → expande el paso **"Ejecutar la suite (mvn clean test)"** →
   📸 `OE3_05_logs-tests.png` (que se vean los escenarios ejecutándose en la nube).

### Paso 3.6 — Mostrar el YAML del pipeline 📸 (FRAMEWORK 🟦)
Abre `.github/workflows/ci-pipeline.yml` en IntelliJ → 📸 `OE3_06_yaml.png`.

### Paso 3.7 — El reporte Allure publicado (GitHub Pages) 📸
1. Abre en el navegador:
   ```
   https://jfra-13.github.io/api-test-framework-broadleaf/
   ```
   *(o la URL que aparece en el job "Publicar reporte en GitHub Pages").*
2. 📸 `OE3_07_allure-pages.png` — el reporte Allure abierto en su URL pública (que se vea
   la barra de direcciones con `github.io`).

> 🧰 **Troubleshooting OE3:**
> - *Job falla con timeouts / connection refused* → Broadleaf o ngrok caídos, o el Secret
>   `BASE_URL` desactualizado (la URL de ngrok cambia cada vez que lo reinicias, salvo dominio fijo).
> - *Respuestas con HTML de aviso de ngrok* → ya está mitigado en el código (header `ngrok-skip-browser-warning`).
> - *Pages da 404* → **Settings → Pages → Source = GitHub Actions**.

✅ **OE3 listo.** Ya puedes **cerrar ngrok** (Ctrl+C en su consola) si no harás más corridas en la nube.

---

# 🟢 OE4 — Experimento de Inyección y Code Churn

**Meta:** evidenciar que una ruptura de contrato del SUT se reabsorbe con **mínimo Code Churn**.
Harás **2 mutaciones**; cada una es un ciclo: **inyectar → ROJO → arreglar → VERDE → medir**.

> 📁 Todo lo de OE4 ya está preparado en el framework, en `docs/oe4/sut-mutations/`.
> El detalle exhaustivo está en `docs/oe4/sut-mutations/README-INYECCION.md`; aquí va la
> versión de "toma de evidencias".

### Paso 4.0 — Preparar (una sola vez)
1. 🟥 **Copiar el filtro al SUT** (si no está ya). Copia el archivo:
   ```
   DESDE: ...\broadleaf-api-test\docs\oe4\sut-mutations\Oe4CartContractMutationFilter.java
   HACIA: ...\WEBSITE\DemoSite\api\src\main\java\com\community\api\endpoint\cart\Oe4CartContractMutationFilter.java
   ```
2. 🟦 **Framework limpio.** En una consola dentro del framework:
   ```bash
   git status            # debe estar limpio (sin archivos en rojo modificados)
   ```
   📸 `OE4_00_git-status-limpio.png`

---

### 🧬 Paso 4.1 — MUTACIÓN A (`customer` → `cliente`) — churn ideal de 1 línea

**A.1 — Inyectar** 🟥
1. Abre `Oe4CartContractMutationFilter.java` (en el SUT) y deja:
   ```java
   private static final boolean ACTIVE   = true;
   private static final String  MUTATION = "A";
   ```
2. **Reinicia Broadleaf** (Stop ⏹ y Run ▶ del módulo `api`).
3. *(Opcional, buena evidencia)* navegador → `https://localhost:7445/api/v1/cart` →
   debe verse `"cliente": {...}` en vez de `"customer": {...}`. 📸 `OE4_A1_json-mutado.png`

**A.2 — Capturar el ROJO** 🟦 (IntelliJ)
4. Corre **`RunCucumberTest`** (clic derecho → Run).
5. Fallará el contrato `cart-schema.json` (falta `customer`).
6. 📸 **`OE4_A2_rojo.png`** — el escenario en rojo + el mensaje del validador de esquema.

**A.3 — Arreglar (1 línea)** 🟦
7. Abre `src/test/resources/schemas/cart-schema.json`. En la línea `"required"`, cambia
   **`"customer"` por `"cliente"`**:
   ```json
   "required": ["id", "status", "itemCount", "cliente"],
   ```
8. Corre `RunCucumberTest` → **VERDE**. 📸 `OE4_A3_verde.png`

**A.4 — Medir el churn** 🟦 (consola dentro del framework)
```bash
git --no-pager diff --stat
git --no-pager diff
```
9. 📸 **`OE4_A4_diff.png`** — debe decir **`1 file changed, 1 insertion(+), 1 deletion(-)`**.
   *(También puedes pegar el texto en `OE4_A4_diff.txt`.)*

**A.5 — Resetear para la B** 🟦🟥
```bash
git checkout -- src/test/resources/schemas/cart-schema.json
git status            # limpio otra vez
```
- 🟥 En el filtro, NO lo apagues aún: solo cambia `MUTATION = "B"` (siguiente paso).

---

### 🧬 Paso 4.2 — MUTACIÓN B (`id` → `idCarrito`) — churn funcional acotado

**B.1 — Cambiar a B** 🟥
1. En `Oe4CartContractMutationFilter.java`: `private static final String MUTATION = "B";`
2. **Reinicia Broadleaf.**
3. *(Opcional)* navegador → `https://localhost:7445/api/v1/cart` → debe verse
   `"idCarrito": <num>` en vez de `"id"`. 📸 `OE4_B1_json-mutado.png`

**B.2 — Capturar el ROJO** 🟦
4. `git status` debe estar limpio. Corre `RunCucumberTest`.
5. Falla en cascada (contrato sin `id` + el `cartId` se captura `null`).
6. 📸 **`OE4_B2_rojo.png`** — el rojo + el error.

**B.3 — Arreglar (renombrar en cada capa)** 🟦
Haz estos 5 cambios (4 archivos):

| Archivo | Dónde | Cambio |
|---|---|---|
| `src/test/resources/schemas/cart-schema.json` | línea `"required"` | `"id"` → `"idCarrito"` |
| `src/test/java/com/tesis/automation/steps/CartSteps.java` | ~línea 24 | `getString("id")` → `getString("idCarrito")` |
| `src/test/java/com/tesis/automation/steps/CartSteps.java` | ~línea 63 | `getString("id")` → `getString("idCarrito")` |
| `src/test/resources/features/cart.feature` | ~línea 10 | `campo "id" de tipo número` → `campo "idCarrito" de tipo número` |
| `src/test/resources/features/checkout.feature` | ~línea 16 | `campo "id" de tipo número` → `campo "idCarrito" de tipo número` |

> 💡 Para encontrar rápido cada línea en IntelliJ: `Ctrl+Shift+F` (Find in Files) y busca `"id"`.

**B.4 — Verificar y medir** 🟦
7. Corre `RunCucumberTest` → **VERDE**. 📸 `OE4_B3_verde.png`
8. Mide:
   ```bash
   git --no-pager diff --stat
   git --no-pager diff
   ```
9. 📸 **`OE4_B4_diff.png`** — debe listar **4 archivos** (`cart-schema.json`, `CartSteps.java`,
   `cart.feature`, `checkout.feature`) = **`4 files changed, 5 insertions(+), 5 deletions(-)`**.
   *(Texto opcional en `OE4_B4_diff.txt`.)*

**B.5 — Cerrar el experimento** 🟦🟥
```bash
git checkout -- .          # descarta TODOS los arreglos de B (framework vuelve a baseline)
git status                 # limpio
```
- 🟥 En el filtro pon `ACTIVE = false` (o **borra** `Oe4CartContractMutationFilter.java`) y
  **reinicia Broadleaf**.
- *(Opcional)* corre `RunCucumberTest` una vez más → **VERDE** = SUT y framework restaurados.
  📸 `OE4_B5_restaurado-verde.png`

### Paso 4.3 — La comparación (anexo) 📸 (FRAMEWORK 🟦)
Abre `docs/OE4-CHURN-EXPERIMENT.md` (secciones 6, 7, 8) → 📸 `OE4_10_comparacion.png`.
Resumen que demuestra tu hipótesis:

| | Mutación A | Mutación B |
|---|---|---|
| Code Churn | **1 línea / 1 archivo** | **5 líneas / 4 archivos** |
| ¿Tocó la lógica de negocio? | No | No |
| Ratio sobre 2.187 líneas | ≈ 0,05 % | ≈ 0,23 % |

✅ **OE4 listo.**

---

## 📋 RESUMEN DE COMANDOS (chuleta rápida)

> Todos los `git` se ejecutan **dentro del FRAMEWORK** 🟦, en cualquier consola.

```bash
# Entrar a la carpeta del framework (Git Bash)
cd /c/software/projects/TESIS_QA/AutomationFramework/broadleaf-api-test

# Ver estado / medir churn
git status
git --no-pager diff --stat
git --no-pager diff

# Descartar cambios (resetear a baseline)
git checkout -- src/test/resources/schemas/cart-schema.json   # un archivo
git checkout -- .                                             # todo

# Túnel para OE3 (cualquier consola)
ngrok http https://localhost:7445
```

| Acción | Dónde | Cómo |
|---|---|---|
| Levantar Broadleaf | 🟥 IntelliJ (DemoSite) | Run del módulo `api` (`ApiApplication`) |
| Correr la suite | 🟦 IntelliJ (framework) | Clic derecho `RunCucumberTest` → Run (o `Ctrl+Shift+F10`) |
| Reporte Allure local | 🟦 IntelliJ → Maven | goal `allure:serve` |
| Swagger | navegador | `https://localhost:7445/api/v1/swagger-ui.html` |
| Pipeline en la nube | navegador | repo → Actions → Run workflow |
| Reporte publicado | navegador | `https://jfra-13.github.io/api-test-framework-broadleaf/` |

---

## ❗ ERRORES COMUNES

- **"No tengo `mvn`"** → correcto: usa **IntelliJ** (Run de clases y el panel **Maven** para `allure:serve`). Nunca necesitas `mvn` en la terminal.
- **El navegador bloquea `https://localhost:7445`** → es certificado propio: *Avanzado → Continuar*. O usa el puerto HTTP `http://localhost:7082/...`.
- **`git diff` muestra un archivo de `docs/`** → ese cambio es de documentación, **no es churn**. Antes de medir, confirma `git status` limpio salvo el archivo que estás midiendo.
- **OE4 no falla (sale verde cuando esperabas rojo)** → no reiniciaste Broadleaf tras tocar el filtro, o `ACTIVE=false`. Revisa el filtro y reinicia el SUT.
- **OE3 falla en la nube** → Broadleaf/ngrok caídos o `BASE_URL` viejo. Reabre ngrok, actualiza el Secret y vuelve a "Run workflow".
```
