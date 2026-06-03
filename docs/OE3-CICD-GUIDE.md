# 🚀 OE3 — Integración Continua (CI/CD)

> **Objetivo:** sacar el framework de tu computadora y lograr su **ejecución
> desatendida en la nube** (GitHub Actions), publicando el reporte Allure en una
> **URL pública navegable** (GitHub Pages).

**Estrategia elegida:** runner en la nube de GitHub + túnel **ngrok** para que ese
runner alcance tu Broadleaf local + publicación del reporte en **GitHub Pages**.

---

## 🧠 El problema central del OE3 (y cómo lo resolvemos)

Tu SUT (Broadleaf) corre en **tu PC** (`https://localhost:7445`). Un servidor en la
nube de GitHub **no puede ver tu `localhost`**. Solución:

```
   PC (tú)                         Nube (GitHub Actions)
┌────────────────────┐            ┌───────────────────────────┐
│  Broadleaf :7445    │            │  Ubuntu + JDK 17          │
│        ▲            │            │  mvn clean test           │
│        │            │            │        │                  │
│   ngrok (túnel)─────┼──INTERNET──┼──► BASE_URL (Secret)      │
│   URL pública       │            │        │                  │
└────────────────────┘            │   mvn allure:report       │
                                   │        │                  │
                                   │   GitHub Pages (reporte)  │
                                   └───────────────────────────┘
```

El framework lee la URL del SUT de la variable de entorno **`BASE_URL`**; ngrok te
da una URL pública que apunta a tu Broadleaf local; esa URL se guarda como **Secret**
del repo y el pipeline la inyecta.

---

## ✅ PARTE 0 — Lo que YA dejé hecho en el proyecto (no toques nada aquí)

| Archivo | Cambio | Por qué |
|---|---|---|
| `src/.../clients/BaseApiClient.java` | La URL del SUT ya **NO está hardcodeada**. Ahora se resuelve así: `-Dbase.url` → variable de entorno `BASE_URL` → `localhost:7445` por defecto. | El mismo código corre en local y en la nube sin cambios. |
| `pom.xml` | Propiedad `base.url` + `systemPropertyVariables` en Surefire. | Permite también `mvn test -Dbase.url=...` en local. |
| `.gitignore` | Ignora `allure-results/`, `allure-report/`, `.allure/`. | No subir basura generada al repo. |
| `.github/workflows/ci-pipeline.yml` | **El pipeline completo.** | Es el entregable del OE3. |

> 🔎 Resultado clave: en local sigues corriendo `mvn clean test` igual que siempre
> (usa `localhost:7445`). En la nube, el pipeline pasa `BASE_URL` con la URL de ngrok.

---

## 👤 PARTE 1 — Validar en local que todo compila (antes de subir)

Abre una terminal **en la carpeta del proyecto** (`broadleaf-api-test`) o usa la
terminal de IntelliJ, con Broadleaf **levantado**, y corre:

```powershell
mvn clean test
mvn allure:report
```

- `mvn clean test` debe ejecutar la suite contra `localhost:7445` (como siempre).
- `mvn allure:report` debe generar la carpeta `target/site/allure-maven-plugin/`
  con un `index.html`. Ábrelo en el navegador para confirmar que el reporte sale bien.

✅ Si ambos comandos funcionan, estás listo para subir a GitHub.

---

## 👤 PARTE 2 — Crear el repositorio en GitHub y subir el código

### 2.1 Inicializar Git en el proyecto (solo la primera vez)

> ⚠️ Ejecuta esto **dentro de** `C:\software\projects\TESIS_QA\AutomationFramework\broadleaf-api-test`
> (la carpeta donde está `pom.xml` y `.github/`). No en una carpeta padre.

```powershell
cd C:\software\projects\TESIS_QA\AutomationFramework\broadleaf-api-test
git init
git add .
git commit -m "OE3: framework + pipeline CI/CD (GitHub Actions + Allure + Pages)"
git branch -M main
```

### 2.2 Crear el repositorio remoto en GitHub

1. Entra a <https://github.com/new>.
2. **Repository name:** `broadleaf-api-test-framework` (o el nombre que prefieras).
3. **Visibilidad:** marca **Public**.
   - 💡 GitHub Pages gratis necesita repo **público** (en repos privados Pages requiere
     plan de pago). Para una tesis, público es además ideal porque lo puedes compartir.
4. **NO** marques “Add a README / .gitignore / license” (ya los tienes en local).
5. Click **Create repository**.

### 2.3 Conectar y subir

Copia los comandos que GitHub te muestra (sección *“…or push an existing repository”*),
o usa estos (reemplazando `TU-USUARIO` y el nombre del repo):

```powershell
git remote add origin https://github.com/TU-USUARIO/broadleaf-api-test-framework.git
git push -u origin main
```

> En el primer `push` GitHub te pedirá autenticarte (navegador o token). Sigue el flujo.

---

## 👤 PARTE 3 — Activar GitHub Pages (fuente = GitHub Actions)

1. En tu repo: **Settings** → menú lateral **Pages**.
2. En **Build and deployment** → **Source**: selecciona **GitHub Actions**.
3. Listo. No elijas rama; el propio pipeline publica el reporte.

---

## 👤 PARTE 4 — Exponer Broadleaf con ngrok

### 4.1 Instalar y autenticar ngrok (una sola vez)

1. Crea cuenta gratis en <https://dashboard.ngrok.com/signup>.
2. Descarga ngrok para Windows o instálalo:
   ```powershell
   choco install ngrok        # si usas Chocolatey
   # o descarga el .exe desde https://ngrok.com/download
   ```
3. Copia tu authtoken del dashboard y regístralo:
   ```powershell
   ngrok config add-authtoken TU_AUTHTOKEN
   ```

### 4.2 Levantar el túnel (cada vez que vayas a correr el pipeline)

Con Broadleaf corriendo en `https://localhost:7445`, abre **otra** terminal:

```powershell
ngrok http https://localhost:7445
```

ngrok te mostrará una línea **Forwarding** como:

```
Forwarding   https://a1b2-201-x-x-x.ngrok-free.app  ->  https://localhost:7445
```

➡️ Esa URL (`https://a1b2-...ngrok-free.app`) es tu URL pública. Déjala corriendo.

> 💡 **Dominio fijo (muy recomendado para no repetir trabajo):** en el plan free ngrok
> te regala **1 dominio estático**. Reclámalo en el dashboard (*Domains*) y úsalo así:
> ```powershell
> ngrok http https://localhost:7445 --url=https://TU-DOMINIO.ngrok-free.app
> ```
> *(en versiones viejas de ngrok el flag es `--domain=` en lugar de `--url=`).*
> Con dominio fijo, el Secret `BASE_URL` lo configuras **una sola vez** y no cambia.

---

## 👤 PARTE 5 — Crear el Secret `BASE_URL` en GitHub

1. En tu repo: **Settings** → **Secrets and variables** → **Actions**.
2. Botón **New repository secret**.
3. **Name:** `BASE_URL`
4. **Secret (value):** la URL de ngrok **+ `/api/v1`**, por ejemplo:
   ```
   https://a1b2-201-x-x-x.ngrok-free.app/api/v1
   ```
   > ⚠️ El `/api/v1` al final es obligatorio (es el prefijo de las APIs de Broadleaf).
5. **Add secret**.

> 🔁 Si **NO** usas dominio fijo, la URL de ngrok cambia cada vez que lo reinicias →
> tendrás que **actualizar este Secret** antes de cada corrida. Por eso recomiendo el
> dominio estático del paso 4.2.

---

## 👤 PARTE 6 — Ejecutar el pipeline (ejecución desatendida)

**Requisitos vivos durante la corrida (≈2–5 min):** tu PC encendida, Broadleaf
levantado y ngrok corriendo.

Tienes dos formas de disparar el pipeline:

**Opción A — Manual (recomendada para demostrar / con ngrok arriba):**
1. Ve a la pestaña **Actions** de tu repo.
2. Selecciona el workflow **“CI - Pruebas de API Broadleaf”**.
3. Botón **Run workflow** → **Run workflow**.

**Opción B — Automática por push:**
```powershell
git add .
git commit -m "trigger CI"
git push
```
Cualquier push a `main` dispara el pipeline solo.

Verás 2 jobs encadenados:
1. **Ejecutar pruebas y generar reporte Allure**
2. **Publicar reporte en GitHub Pages**

---

## 👤 PARTE 7 — Ver el reporte publicado

- Al terminar el job **Publicar reporte en GitHub Pages**, su URL aparece en el propio
  job (entorno *github-pages*).
- También la encuentras en **Settings → Pages**:
  ```
  https://TU-USUARIO.github.io/broadleaf-api-test-framework/
  ```
- Además, en cada corrida se guarda un artifact descargable **`allure-results`**
  (pestaña Actions → la corrida → sección *Artifacts*).

---

## 🎓 Cómo presentarlo en la tesis (evidencias a capturar)

El pipeline cumple los **3 pasos** del OE3:

| Paso OE3 | Dónde está | Screenshot sugerido |
|---|---|---|
| Paso 1 — Configurar GitHub Actions | `.github/workflows/ci-pipeline.yml` | El YAML + pestaña Actions con corridas verdes |
| Paso 2 — Pipeline (Ubuntu, JDK 17, `mvn clean test`, SUT vía túnel) | Jobs del workflow | Log del job mostrando los pasos 1→6 |
| Paso 3 — Reporte desatendido (Allure en GitHub Pages) | Job *Publicar reporte* | La URL `*.github.io` con el reporte Allure abierto |

**Texto de apoyo:** explica que la URL del SUT está externalizada (env var `BASE_URL`),
lo que **desacopla el framework del entorno** y habilita ejecutar el mismo código en
local y en la nube — esto conecta directo con la mantenibilidad que demostrarás en OE4.

---

## 🩹 Troubleshooting

| Síntoma | Causa probable | Solución |
|---|---|---|
| Job falla en *Run tests* con timeouts / connection refused | ngrok o Broadleaf caídos, o `BASE_URL` desactualizado | Asegura PC + Broadleaf + ngrok arriba; actualiza el Secret `BASE_URL` con la URL nueva de ngrok |
| Respuestas con HTML de aviso de ngrok | Página interstitial del plan free | Ya está mitigado: el framework envía el header `ngrok-skip-browser-warning` cuando la URL es de ngrok |
| ngrok da error TLS contra `localhost:7445` | Certificado self-signed de Broadleaf | Prueba `ngrok http https://localhost:7445 --host-header=rewrite`; si persiste, expón Broadleaf por HTTP y usa esa URL en `BASE_URL` |
| Pages no publica / 404 | Source de Pages mal configurado | **Settings → Pages → Source = GitHub Actions** (Parte 3) |
| El pipeline no aparece en Actions | El archivo no llegó al repo | Confirma que subiste `.github/workflows/ci-pipeline.yml` (`git status` / revisa en GitHub) |

---

## ⚡ Checklist rápido

- [ ] `mvn clean test` y `mvn allure:report` funcionan en local
- [ ] `git init` + commit + repo público en GitHub + `git push`
- [ ] Settings → Pages → Source = **GitHub Actions**
- [ ] ngrok autenticado y corriendo (`ngrok http https://localhost:7445`)
- [ ] Secret **`BASE_URL`** = URL de ngrok + `/api/v1`
- [ ] Broadleaf + ngrok arriba → **Run workflow**
- [ ] Reporte visible en `https://TU-USUARIO.github.io/<repo>/`
```
