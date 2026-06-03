Feature: Gestión del Carrito de Compras

  Background:
    Given el servidor API está disponible en "https://localhost:7445/api/v1"
    And la sesión HTTP está inicializada con JSESSIONID

  Scenario: Creación de un carrito nuevo
    Given el usuario obtiene un nuevo carrito de compras
    Then la respuesta debe tener código de estado 200
    And la respuesta contiene un campo "id" de tipo número
    And la respuesta cumple con el contrato "cart-schema.json"
    And se captura el ID del carrito para usar en próximas solicitudes

  Scenario: Agregar un producto al carrito
    Given el usuario obtiene un nuevo carrito de compras
    And el usuario busca un producto con término "hot"
    When el usuario agrega 1 unidad del SKU al carrito
    Then la respuesta debe tener código de estado 200
    And el campo "itemCount" debe ser igual a 1
    And la respuesta cumple con el contrato "cart-schema.json"

  Scenario: Error al agregar un producto con SKU inválido
    Given el usuario obtiene un nuevo carrito de compras
    And el usuario intenta agregar un producto inválido
    When realiza una solicitud POST a "/cart/{cartId}/item" con skuId inválido
    Then la respuesta debe tener código de estado 400 o 404
    And la respuesta contiene un messageKey describiendo el error
