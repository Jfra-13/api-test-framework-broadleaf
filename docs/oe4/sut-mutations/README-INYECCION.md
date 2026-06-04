# 🧪 OE4 — Guía de inyección y medición (paso a paso para TI)

> Esta guía es **lo que te toca hacer a ti** desde IntelliJ (sin Maven).
> Yo (el asistente) ya dejé listo: el hardening del `cart-schema.json`, el filtro
> de inyección (`Oe4CartContractMutationFilter.java`) y este instructivo.
> Tú ejecutas, capturas pantallas y me pasas los resultados; yo armo las tablas.

**Dos "mundos" (no los mezcles):**
- 🟥 **SUT** = Broadleaf → `C:\software\projects\TESIS_QA\WEBSITE\DemoSite` (corres el módulo `api`).
- 🟦 **Framework** = tus pruebas → `C:\software\projects\TESIS_QA\AutomationFramework\broadleaf-api-test`.

El *Code Churn* se mide **solo en el Framework** (🟦). El filtro vive en el SUT (🟥) y **no cuenta**.

---

## ✅ PASO 0 — Línea base en verde (validar el instrumento)

> Objetivo: confirmar que, **sin ninguna mutación**, la suite pasa en verde con el
> contrato ya endurecido (`customer` ahora es obligatorio en `cart-schema.json`).

1. 🟥 Levanta Broadleaf (módulo `api`) desde IntelliJ → debe responder en `https://localhost:7445`.
2. 🟦 En el proyecto del Framework, corre la suite Cucumber (clase `RunCucumberTest`) desde el IDE.
3. **Resultado esperado:** TODO verde (15/15).
   - 🟢 Si pasa → el instrumento está bien. Sigue al PASO 1.
   - 🔴 Si falla **solo** el contrato del carrito por `customer` → avísame: cambio el
     campo endurecido a otro garantizado y reintenta (es un ajuste de 1 línea de mi lado).

📸 *Captura:* la suite en verde (evidencia de "antes").

---

## 🧬 MUTACIÓN A — campo SOLO-CONTRATO (`customer` → `cliente`)

> Demuestra el churn **ideal**: la ruptura la atrapa el contrato y se arregla en **1 línea**,
> **sin tocar** features ni steps.

### A.1 — Inyectar la mutación en el SUT 🟥
1. Copia el archivo `Oe4CartContractMutationFilter.java` (está en esta misma carpeta) a:
   ```
   DemoSite\api\src\main\java\com\community\api\endpoint\cart\Oe4CartContractMutationFilter.java
   ```
2. Ábrelo y confirma que arriba diga:
   ```java
   private static final boolean ACTIVE   = true;
   private static final String  MUTATION = "A";
   ```
3. 🟥 **Reinicia** Broadleaf (Stop ▶ Run) para que tome el filtro.
4. *(Opcional, evidencia)* abre en el navegador `https://localhost:7445/api/v1/cart` y verifica
   que el JSON ahora trae `"cliente": {...}` en vez de `"customer": {...}`.
   📸 *Captura:* el JSON mutado.

### A.2 — Capturar la FALLA (rojo) 🟦
1. Corre `RunCucumberTest` en el Framework.
2. **Resultado esperado:** falla el escenario de checkout en la validación de contrato
   `cart-schema.json` (falta el campo obligatorio `customer`).
   📸 *Captura:* el test en rojo + el mensaje del validador de esquema.
3. **Importante:** NO arregles nada todavía. Asegúrate que el árbol del Framework esté limpio:
   ```powershell
   # 🟦 en la carpeta del Framework
   git status        # debe estar limpio (solo el commit de setup de OE4)
   ```

### A.3 — Arreglar con el churn mínimo 🟦
1. Abre `src/test/resources/schemas/cart-schema.json`.
2. En el arreglo `"required"`, cambia **`"customer"` por `"cliente"`** (esa sola línea):
   ```jsonc
   "required": ["id", "status", "itemCount", "cliente"],
   ```
   *(Opcional, por prolijidad: en `properties` renombra también `"customer"` → `"cliente"`.)*
3. Corre `RunCucumberTest` → **debe volver a verde**.
   📸 *Captura:* el test en verde (evidencia de "después").

### A.4 — Medir el Code Churn 🟦
En una terminal en la carpeta del Framework:
```powershell
git --no-pager diff --stat        # archivos y líneas tocadas
git --no-pager diff               # el cambio exacto (para anexo)
```
➡️ **Anota:** nº de archivos tocados y nº de líneas. (Esperado: **1 archivo, ~1 línea**, 0 en features/steps.)
📋 *Pásame esa salida* y la pego en la tabla del capítulo OE4.

### A.5 — Resetear para la siguiente mutación 🟦🟥
```powershell
# 🟦 Framework: descarta el arreglo para dejar el árbol limpio otra vez
git checkout -- src/test/resources/schemas/cart-schema.json
```
- 🟥 SUT: deja el filtro puesto, **solo cambia** `MUTATION = "B"` (siguiente sección).

---

## 🧬 MUTACIÓN B — campo FUNCIONAL (`id` → `idCarrito`)

> Demuestra el matiz: un campo con acoplamiento funcional cuesta **varias líneas**,
> pero el cambio queda **contenido en las capas técnicas** (schema + steps + spec);
> el **flujo de negocio** del escenario no cambia.

### B.1 — Cambiar a la mutación B en el SUT 🟥
1. En `Oe4CartContractMutationFilter.java` cambia:
   ```java
   private static final String MUTATION = "B";
   ```
2. 🟥 **Reinicia** Broadleaf.
3. *(Opcional)* `https://localhost:7445/api/v1/cart` debe mostrar `"idCarrito": <num>` en vez de `"id"`.
   📸 *Captura.*

### B.2 — Capturar la FALLA (rojo) 🟦
1. Asegura árbol limpio del Framework (`git status`).
2. Corre `RunCucumberTest`.
3. **Resultado esperado:** falla en cascada —
   (a) el contrato `cart-schema.json` (falta `id`), y
   (b) el flujo funcional (el `cartId` se captura como `null` → el `POST /cart/null/item` falla).
   📸 *Captura:* el rojo + el stack/mensaje.

### B.3 — Arreglar con el churn real (honesto) 🟦
Renombra la referencia al campo en **todas** las capas que lo usan (yo te paso los valores exactos,
pero aquí está el mapa):

| Archivo | Línea aprox. | Cambio |
|---|---|---|
| `schemas/cart-schema.json` | `required` (y opcional `properties`) | `id` → `idCarrito` |
| `steps/CartSteps.java` | ~24 | `getString("id")` → `getString("idCarrito")` |
| `steps/CartSteps.java` | ~63 | `getString("id")` → `getString("idCarrito")` |
| `features/cart.feature` | ~10 | `campo "id"` → `campo "idCarrito"` |
| `features/checkout.feature` | ~16 | `campo "id"` → `campo "idCarrito"` |

> Nota: `CheckoutSteps` lee `"id"` de la respuesta de `PUT /shipping` (que el filtro **no**
> muta), así que **no** se toca. El filtro solo muta `GET /cart` y `POST /cart/{id}/item`.

Corre `RunCucumberTest` → **debe volver a verde**.
📸 *Captura:* verde.

### B.4 — Medir el Code Churn 🟦
```powershell
git --no-pager diff --stat
git --no-pager diff
```
➡️ **Anota** archivos y líneas (esperado: ~4 archivos, ~5 líneas) y *pásame la salida*.

### B.5 — Cerrar el experimento 🟦🟥
```powershell
# 🟦 Framework: descarta los arreglos (para dejar el repo como estaba)
git checkout -- .
```
- 🟥 SUT: pon `ACTIVE = false` (o borra `Oe4CartContractMutationFilter.java`) y reinicia Broadleaf.
- Verifica que `GET /cart` vuelva a traer `"id"` y `"customer"` normales.

---

## 📦 Qué me tienes que pasar (para que yo arme el capítulo)
Por cada mutación (A y B):
1. 📸 Captura del **rojo** (falla) y del **verde** (arreglado).
2. 📋 La salida de `git diff --stat` y `git diff`.
3. *(Opcional)* 📸 El JSON mutado del navegador.

Con eso lleno las tablas de `docs/OE4-CHURN-EXPERIMENT.md` y cierro el OE4. 🚀
