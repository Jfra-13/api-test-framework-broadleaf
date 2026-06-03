# Broadleaf Commerce - API Testing Framework (BDD)

## Estructura del Proyecto

```
framework-automatizacion-api/
├── pom.xml                                    # Dependencias Maven (ya creado)
├── src/
│   ├── test/
│   │   ├── java/
│   │   │   └── com/tesis/automation/
│   │   │       ├── clients/                   # API Client Pattern
│   │   │       │   ├── BaseApiClient.java
│   │   │       │   ├── CartApiClient.java
│   │   │       │   ├── CatalogApiClient.java
│   │   │       │   └── CheckoutApiClient.java
│   │   │       ├── dto/                       # Data Transfer Objects (Lombok)
│   │   │       │   ├── Address.java
│   │   │       │   ├── BillingAddress.java
│   │   │       │   ├── PaymentTransaction.java
│   │   │       │   ├── OrderPayment.java
│   │   │       │   └── OrderWrapper.java
│   │   │       ├── steps/                     # Step Definitions (Cucumber)
│   │   │       │   ├── CartSteps.java
│   │   │       │   ├── CatalogSteps.java
│   │   │       │   ├── CheckoutSteps.java
│   │   │       │   └── PaymentSteps.java
│   │   │       ├── utils/                     # Utilidades
│   │   │       │   ├── DataGenerator.java
│   │   │       │   ├── JsonSchemaValidator.java
│   │   │       │   └── ScenarioContext.java
│   │   │       └── hooks/                     # Cucumber Hooks
│   │   │           └── CommonHooks.java
│   │   ├── resources/
│   │   │   ├── features/                      # Gherkin Scenarios
│   │   │   │   ├── checkout.feature
│   │   │   │   ├── product-catalog.feature
│   │   │   │   └── cart.feature
│   │   │   ├── schemas/                       # JSON Schemas (Contratos)
│   │   │   │   ├── cart-schema.json
│   │   │   │   ├── checkout-schema.json
│   │   │   │   ├── product-schema.json
│   │   │   │   └── order-schema.json
│   │   │   └── application.properties         # Config
│   │   └── java/runners/
│   │       └── RunCucumberTest.java           # Test Runner
│   └── docs/
│       ├── API-BASELINE.md                    # Línea Base (OE1)
│       └── ARCHITECTURE.md                    # Arquitectura (OE2)
```

## Objetivo Específico 1 (OE1): Línea Base

✅ **Contratos API Documentados**
- JSON Schemas para cada endpoint
- Mapeo de Request/Response
- Definición de campos obligatorios/opcionales

## Objetivo Específico 2 (OE2): Framework BDD

✅ **Arquitectura Multicapa**
- Features: Lenguaje de negocio (Gherkin)
- Steps: Automatización
- Clients: API Pattern (RestAssured)
- DTOs: Modelos de datos (Lombok)
- Utils: Utilidades compartidas
- Contract Testing: Validación de JSON Schemas

