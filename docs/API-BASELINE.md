# Objetivo Específico 1 (OE1): Línea Base - Análisis de Endpoints

## 📊 Contratos Base (Baselines)

Este documento establece los "contratos esperados" para cada endpoint de la API de Broadleaf Commerce.
La validación contra estos esquemas es la base del **Contract Testing** implementado en OE2.

---

## 1. Endpoint: GET /cart

### Propósito
Obtiene o crea un carrito de compras para la sesión HTTP actual.

### Solicitud
```
GET /api/v1/cart
Headers:
  - Accept: application/json
  - Cookie: JSESSIONID={sessionId}
```

### Respuesta 200 OK
```json
{
  "id": 1,
  "status": "IN_PROCESS",
  "itemCount": 0,
  "subTotal": { "amount": 0.00, "currency": "USD" },
  "total": { "amount": 0.00, "currency": "USD" },
  "fulfillmentGroups": [
    {
      "id": 1,
      "orderId": 1,
      "total": { "amount": 0.00, "currency": "USD" },
      "address": null
    }
  ],
  "customer": { "id": 101 }
}
```

### Validación de Contrato
✅ Ver: `cart-schema.json`

---

## 2. Endpoint: GET /catalog/search?q={query}

### Propósito
Busca productos en el catálogo por término de búsqueda.

### Solicitud
```
GET /api/v1/catalog/search?q=hot
Headers:
  - Accept: application/json
```

### Respuesta 200 OK
```json
{
  "products": [
    {
      "id": 13,
      "name": "Bull Snort Smokin' Toncils Hot Sauce",
      "defaultSku": {
        "id": 13,
        "activeStartDate": "2023-01-01T00:00:00.000+00:00"
      },
      "price": { "amount": 3.99, "currency": "USD" }
    }
  ]
}
```

### Campos Obligatorios
- `products []` - Array de productos
- `products[].id` - ID único del producto
- `products[].name` - Nombre del producto
- `products[].defaultSku` - SKU por defecto
- `products[].defaultSku.id` - ID del SKU

---

## 3. Endpoint: POST /cart/{cartId}/item

### Propósito
Agrega un artículo al carrito.

### Solicitud
```
POST /api/v1/cart/1/item
Headers:
  - Content-Type: application/json
  - Cookie: JSESSIONID={sessionId}

Body:
{
  "quantity": 1,
  "skuId": 13
}
```

### Respuesta 200 OK
```json
{
  "id": 1,
  "status": "IN_PROCESS",
  "itemCount": 1,
  "subTotal": { "amount": 3.99, "currency": "USD" },
  "total": { "amount": 3.99, "currency": "USD" },
  "orderItems": [
    {
      "id": 1,
      "name": "Bull Snort Smokin' Toncils Hot Sauce",
      "quantity": 1,
      "skuId": 13,
      "retailPrice": { "amount": 3.99, "currency": "USD" }
    }
  ],
  "fulfillmentGroups": [
    {
      "id": 1,
      "total": { "amount": 3.99, "currency": "USD" },
      "fulfillmentGroupItems": [{ "id": 1, "quantity": 1 }]
    }
  ]
}
```

### Validación de Contrato
✅ Ver: `cart-schema.json`

---

## 4. Endpoint: PUT /shipping/{cartId}/{fulfillmentGroupId}/address

### Propósito
Configura la dirección de envío para un grupo de cumplimiento.

### Solicitud
```
PUT /api/v1/shipping/1/1/address
Headers:
  - Content-Type: application/json
  - Cookie: JSESSIONID={sessionId}

Body:
{
  "firstName": "Juan",
  "lastName": "Perez",
  "addressLine1": "Calle Falsa 123",
  "city": "Tech City",
  "stateProvinceRegion": "TX",
  "postalCode": "75001",
  "isoCountryAlpha2": { "alpha2": "US" }
}
```

### Respuesta 200 OK
```json
{
  "id": 1,
  "fulfillmentGroups": [
    {
      "id": 1,
      "address": {
        "id": 1,
        "firstName": "Juan",
        "lastName": "Perez",
        "addressLine1": "Calle Falsa 123",
        "city": "Tech City",
        "stateProvinceRegion": "TX",
        "postalCode": "75001",
        "isoCountryAlpha2": { "alpha2": "US" }
      }
    }
  ]
}
```

### Campos Requeridos
- `firstName` (string)
- `lastName` (string)
- `addressLine1` (string)
- `city` (string)
- `postalCode` (string)
- `isoCountryAlpha2.alpha2` (string) - Código ISO de país

---

## 5. Endpoint: POST /cart/checkout/payment?cartId={cartId}

### Propósito
Agrega información de pago a la orden antes del checkout final.

### Solicitud
```
POST /api/v1/cart/checkout/payment?cartId=1
Headers:
  - Content-Type: application/json
  - Cookie: JSESSIONID={sessionId}

Body:
{
  "orderId": 1,
  "type": "CREDIT_CARD",
  "gatewayType": "NULL_GATEWAY",
  "amount": 3.99,
  "billingAddress": { ... },
  "transactions": [
    {
      "type": "AUTHORIZE_AND_CAPTURE",
      "success": true,
      "amount": 3.99,
      "additionalFields": [
        { "key": "number", "value": "1111222233334444" },
        { "key": "expMonth", "value": "12" },
        { "key": "expYear", "value": "2028" },
        { "key": "cvv", "value": "123" }
      ]
    }
  ]
}
```

### Respuesta 200 OK
```json
{
  "id": 1,
  "orderId": 1,
  "type": "CREDIT_CARD",
  "amount": 3.99,
  "currency": "USD",
  "gatewayType": "NULL_GATEWAY",
  "transactions": [
    {
      "id": 1,
      "type": "AUTHORIZE_AND_CAPTURE",
      "success": true,
      "amount": 3.99,
      "currency": "USD"
    }
  ]
}
```

### ⚠️ CAMPOS CRÍTICOS
- `transactions[0].success` MUST be `true` (Boolean, ¡NO String!)
- `transactions[0].amount` MUST match the payment amount
- `gatewayType` para tests SHOULD be "NULL_GATEWAY"

---

## 6. Endpoint: POST /cart/checkout?cartId={cartId}

### Propósito
Realiza el checkout final y confirma la orden.

### Solicitud
```
POST /api/v1/cart/checkout?cartId=1
Headers:
  - Content-Type: application/json
  - Cookie: JSESSIONID={sessionId}

Body: {} (vacío)
```

### Respuesta 200/201
```json
{
  "id": 1,
  "orderNumber": "202512121532001",
  "status": "SUBMITTED",
  "submitDate": "2025-12-12T15:32:00.123+00:00",
  "total": { "amount": 3.99, "currency": "USD" },
  "itemCount": 1,
  "payments": [
    {
      "id": 1,
      "type": "CREDIT_CARD",
      "transactions": [
        { "id": 1, "type": "AUTHORIZE_AND_CAPTURE", "success": true }
      ]
    }
  ]
}
```

### ✅ Criterios de Éxito
1. Status Code: 200 o 201
2. `status` = "SUBMITTED"
3. `orderNumber` ≠ null
4. `payments.transactions[].success` = true
5. Valida contra `checkout-schema.json`

---

## Errores Documentados

### Error 404
```json
{
  "timestamp": "2025-12-12T15:32:00.123+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/v1/endpoint-invalido"
}
```

### Error 400 - Validación 
```json
{
  "httpStatusCode": 400,
  "messages": [
    {
      "messageKey": "invalidEnumerationValueException",
      "message": "Invalid PaymentGatewayType"
    }
  ]
}
```

### Error 500 - Checkout Processing
```json
{
  "httpStatusCode": 500,
  "messages": [
    {
      "messageKey": "checkoutProcessingError",
      "message": "Error procesando el checkout"
    }
  ]
}
```

---

## Resumen de Hallazgos (OE1)

| Endpoint | Estado | Contrato | Notas |
|----------|--------|----------|-------|
| GET /cart | ✅ OK | `cart-schema.json` | Crea carrito si no existe |
| GET /catalog/search | ✅ OK | JSON inline | Array de productos |
| POST /cart/{id}/item | ✅ OK | `cart-schema.json` | Retorna carrito actualizado |
| PUT /shipping/{cartId}/{fgId}/address | ✅ OK | `cart-schema.json` | Requiere campos obligatorios |
| POST /cart/checkout/payment | ⚠️ CRÍTICO | `order-payment-schema.json` | **success MUST be Boolean** |
| POST /cart/checkout | ✅ OK | `checkout-schema.json` | Status final SUBMITTED |

---

## Próximos Pasos (OE2)

✅ Refactorizar código según arquitectura BDD
✅ Implementar Steps Definitions con Cucumber
✅ Crear API Clients para desacoplamiento
✅ Aplicar Contract Testing con JSON Schemas
✅ Generar reportes Allure

