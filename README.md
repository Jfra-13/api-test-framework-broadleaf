# broadleaf-api-test — Framework BDD para Tesis QA

Proyecto de automatización de pruebas de API REST para Broadleaf Commerce.
Demuestra **OE1** (Línea Base de Contratos) y **OE2** (Arquitectura BDD Multicapa).

---

## Estructura del Proyecto

```
broadleaf-api-test/
├── pom.xml                                      # Dependencias Maven (Java 17)
├── docs/                                        # Documentación de tesis
│   ├── API-BASELINE.md                          # OE1: Contratos de endpoints
│   ├── ARCHITECTURE-GUIDE.md                    # OE2: Arquitectura multicapa
│   ├── REFACTORING-GUIDE.md                     # Guía de refactorización
│   ├── FRAMEWORK-STRUCTURE.md                   # Descripción de carpetas
│   ├── STATUS-CHECKLIST.md                      # Checklist de progreso
│   └── DIAGNOSTICO_Y_SOLUCION.md               # Problemas encontrados
│
└── src/test/
    ├── java/com/tesis/automation/
    │   ├── clients/          # API Client Pattern — encapsulan RestAssured
    │   │   ├── BaseApiClient.java       # Clase padre: GET, POST, PUT comunes
    │   │   ├── CartApiClient.java       # Operaciones del carrito
    │   │   ├── CatalogApiClient.java    # Búsqueda de productos
    │   │   └── CheckoutApiClient.java   # Pago y checkout
    │   │
    │   ├── dto/              # Data Transfer Objects con Lombok
    │   │   ├── Address.java             # Dirección de envío/facturación
    │   │   ├── IsoCountry.java          # Código de país ISO
    │   │   ├── AdditionalField.java     # Campo clave-valor (datos de tarjeta)
    │   │   ├── PaymentTransaction.java  # Transacción de pago
    │   │   ├── OrderPayment.java        # Información completa de pago
    │   │   └── OrderWrapper.java        # Respuesta de orden completa
    │   │
    │   ├── utils/            # Utilidades compartidas
    │   │   ├── DataGenerator.java       # Generación de datos con JavaFaker
    │   │   ├── ScenarioContext.java     # Contexto ThreadLocal entre steps
    │   │   └── JsonSchemaValidator.java # Wrapper para validación de contratos
    │   │
    │   ├── steps/            # Step Definitions Cucumber (Gherkin → Java)
    │   │   ├── CommonSteps.java         # Validaciones genéricas y steps decorativos
    │   │   ├── CartSteps.java           # Steps del carrito
    │   │   ├── CatalogSteps.java        # Steps del catálogo de productos
    │   │   ├── CheckoutSteps.java       # Steps de envío y checkout
    │   │   └── PaymentSteps.java        # Steps de pago con tarjeta
    │   │
    │   ├── hooks/            # Cucumber Hooks — setup y teardown
    │   │   └── CommonHooks.java         # @Before: limpia contexto; @After: logs
    │   │
    │   ├── runners/          # Test Runners
    │   │   └── RunCucumberTest.java     # Ejecuta todos los .feature con Allure
    │   │
    │   └── tests/            # Tests JUnit directos (referencia / monolítico)
    │       ├── checkout/
    │       │   └── CheckoutHappyPathTest.java   # Happy path completo (7 pasos)
    │       └── catalog/
    │           └── CatalogSmokeTest.java         # Smoke test del catálogo
    │
    └── resources/
        ├── features/         # Especificaciones Gherkin
        │   ├── checkout.feature         # Flujo completo de compra (Happy Path)
        │   ├── product-catalog.feature  # Validación del catálogo
        │   └── cart.feature             # Gestión del carrito y errores
        │
        ├── schemas/          # JSON Schemas para Contract Testing (OE1)
        │   ├── cart-schema.json
        │   ├── checkout-schema.json
        │   ├── order-payment-schema.json
        │   ├── product-catalog-schema.json
        │   └── contrato_detalle_producto.json
        │
        ├── junit-platform.properties    # Configuración Cucumber/JUnit 5
        └── simplelogger.properties      # Niveles de log (slf4j-simple)
```

---

## Arquitectura (OE2)

```
Features (Gherkin) → Steps → API Clients → DTOs → HTTP (RestAssured)
```

Cada capa tiene una sola responsabilidad. Ver `docs/ARCHITECTURE-GUIDE.md`.

---

## Contratos de API (OE1)

6 endpoints documentados con JSON Schemas para validación automática.
Ver `docs/API-BASELINE.md` y `src/test/resources/schemas/`.

---

## Ejecución de Pruebas

**Prerrequisito:** DemoSite corriendo en `https://localhost:7445`

### 1. Limpiar historial y ejecutar todas las pruebas

Elimina resultados anteriores de Allure y ejecuta la suite completa:

```powershell
Remove-Item -Recurse -Force ./allure-results -ErrorAction SilentlyContinue; C:\Users\Public\.maven\maven-3.9.16\bin\mvn.cmd test
```

### 2. Ver reporte Allure en el navegador

Compila el reporte y lo abre automáticamente:

```powershell
C:\Users\Public\.maven\maven-3.9.16\bin\mvn.cmd allure:serve
```

> Los resultados de Allure se generan en `./allure-results/` y el reporte HTML en `./target/cucumber-report.html`.

---

## Integración Continua (OE3)

Pipeline de GitHub Actions: `.github/workflows/ci-pipeline.yml`.

### Disparadores

- `push` a las ramas `main` / `master`.
- Ejecución manual (`workflow_dispatch`) desde la pestaña **Actions** del repo.

### Qué hace

| Job | Acción |
|-----|--------|
| `api-tests` | Levanta Ubuntu + JDK 17 (Temurin) con caché Maven, ejecuta `mvn -B clean test`, genera el reporte Allure (`mvn -B allure:report`) y sube los resultados crudos como artifact descargable (retención 30 días). |
| `deploy-report` | Publica el reporte Allure en **GitHub Pages** (URL navegable). |

### Conexión con el SUT (Broadleaf)

El SUT corre en tu PC. Para que el runner de GitHub lo alcance, se expone con un túnel **ngrok** y su URL pública se inyecta vía variable de entorno.

1. Levantar el DemoSite local (`https://localhost:7445`).
2. Abrir el túnel: `ngrok http https://localhost:7445`.
3. Guardar la URL pública de ngrok (incluyendo `/api/v1`) como **Secret** del repositorio:
   - Repo → **Settings → Secrets and variables → Actions → New repository secret**
   - Nombre: `BASE_URL`
4. Lanzar el pipeline (push o ejecución manual).

`BaseApiClient` lee la URL con `System.getenv("BASE_URL")`. Si el Secret está vacío, cae a `localhost:7445`.

### Resultado

- **Reporte Allure** publicado en GitHub Pages (ver URL en el job `deploy-report`).
- **Artifact** `allure-results` descargable desde la corrida en la pestaña Actions.
