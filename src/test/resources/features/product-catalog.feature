Feature: Validación del Catálogo de Productos

  Background:
    Given el servidor API está disponible en "https://localhost:7445/api/v1"
    And la sesión HTTP está inicializada con JSESSIONID

  Scenario: Búsqueda de productos devuelve resultados
    When el usuario busca un producto con término "hot"
    Then la respuesta debe tener código de estado 200
    And la respuesta contiene al menos un producto
    And se captura el primer SKU ID encontrado

  Scenario: Contrato del catálogo de productos es válido
    Given el usuario realiza una solicitud GET a "/catalog/search?q=hot"
    Then la respuesta debe tener código de estado 200
    And la respuesta debe ser válida según el esquema "product-catalog-schema.json"
    And cada producto en la lista debe tener los campos obligatorios:
      | id         |
      | name       |
      | defaultSku |
