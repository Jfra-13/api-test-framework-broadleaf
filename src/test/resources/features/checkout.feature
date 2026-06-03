Feature: Flujo Completo de Checkout con Tarjeta de Crédito

  Descripcion: Valida el flujo happy path de una compra exitosa desde
  la creacion del carrito hasta la confirmacion de la orden (OE1 + OE2).

  Background:
    Given el servidor API está disponible en "https://localhost:7445/api/v1"
    And la sesión HTTP está inicializada con JSESSIONID

  Scenario: Cliente realiza una compra exitosa (Happy Path)

    # PASO 1: Crear Carrito
    Given el usuario obtiene un nuevo carrito de compras
    When realiza una solicitud GET a "/cart"
    Then la respuesta debe tener código de estado 200
    And la respuesta contiene un campo "id" de tipo número
    And la respuesta cumple con el contrato "cart-schema.json"
    And se captura el ID del carrito para usar en próximas solicitudes

    # PASO 2: Buscar Producto
    When el usuario busca un producto con término "hot"
    And realiza una solicitud GET a "/catalog/search?q=hot"
    Then la respuesta debe tener código de estado 200
    And la respuesta contiene al menos un producto
    And se captura el primer SKU ID encontrado

    # PASO 3: Agregar Item al Carrito
    When el usuario agrega 1 unidad del SKU al carrito
    And realiza una solicitud POST a "/cart/{cartId}/item"
    Then la respuesta debe tener código de estado 200
    And el campo "itemCount" debe ser igual a 1
    And se captura el "fulfillmentGroups[0].id"
    And la respuesta cumple con el contrato "cart-schema.json"

    # PASO 4: Configurar Dirección de Envío
    When el usuario configura la dirección de envío
    And realiza una solicitud PUT a "/shipping/{cartId}/{fulfillmentGroupId}/address"
    Then la respuesta debe tener código de estado 200
    And la dirección quedó registrada correctamente

    # PASO 5: Agregar Información de Pago
    When el usuario agrega información de pago con tarjeta de crédito
    And incluye billingAddress
    And incluye transacción con tipo "AUTHORIZE_AND_CAPTURE"
    And realiza una solicitud POST a "/cart/checkout/payment?cartId={cartId}"
    Then la respuesta debe tener código de estado 200
    And el campo "transactions[0].success" debe ser true
    And el campo "transactions[0].amount" debe ser 3.99
    And la respuesta cumple con el contrato "order-payment-schema.json"

    # PASO 6: Realizar Checkout
    When el usuario realiza el checkout final
    And realiza una solicitud POST a "/cart/checkout?cartId={cartId}"
    Then la respuesta debe tener código de estado 200 o 201
    And el campo "status" debe ser "SUBMITTED"
    And el campo "orderNumber" no debe ser nulo
    And la orden ha sido procesada exitosamente
    And la respuesta cumple con el contrato "checkout-schema.json"
