package data

val sampleOpenApiSpec = """
    openapi: "3.0.0"
    info:
      title: Ice Cream Shop API
      version: "1.0.0"
      description: >
        Order ice cream from the comfort of your browser. Demonstrates oneOf discriminator,
        enums, nullable fields, uuid/date-time formats, and more.

    paths:
      /orders:
        post:
          summary: Place an ice cream order
          operationId: placeOrder
          requestBody:
            required: true
            content:
              application/json:
                schema:
                  ${'$'}ref: '#/components/schemas/IceCreamOrder'
          responses:
            '201':
              description: Order placed successfully
              content:
                application/json:
                  schema:
                    ${'$'}ref: '#/components/schemas/OrderReceipt'
            '400':
              description: Invalid order
              content:
                application/json:
                  schema:
                    ${'$'}ref: '#/components/schemas/ApiError'
            '503':
              description: Shop is closed
              content:
                application/json:
                  schema:
                    ${'$'}ref: '#/components/schemas/ApiError'

    components:
      schemas:

        IceCreamOrder:
          oneOf:
            - ${'$'}ref: '#/components/schemas/ConeOrder'
            - ${'$'}ref: '#/components/schemas/CupOrder'
          discriminator:
            propertyName: orderType
            mapping:
              cone: '#/components/schemas/ConeOrder'
              cup: '#/components/schemas/CupOrder'

        ConeOrder:
          type: object
          required:
            - orderType
            - scoops
            - coneType
            - takeaway
          properties:
            orderType:
              type: string
            takeaway:
              type: boolean
            scoops:
              type: array
              minItems: 1
              maxItems: 3
              items:
                ${'$'}ref: '#/components/schemas/Scoop'
            coneType:
              ${'$'}ref: '#/components/schemas/ConeType'
            drizzle:
              ${'$'}ref: '#/components/schemas/Drizzle'
              nullable: true

        CupOrder:
          type: object
          required:
            - orderType
            - scoops
            - size
            - takeaway
          properties:
            orderType:
              type: string
            takeaway:
              type: boolean
            scoops:
              type: array
              minItems: 1
              maxItems: 5
              items:
                ${'$'}ref: '#/components/schemas/Scoop'
            size:
              ${'$'}ref: '#/components/schemas/CupSize'
            toppings:
              type: array
              items:
                ${'$'}ref: '#/components/schemas/Topping'
              nullable: true

        Scoop:
          type: object
          required:
            - flavour
          properties:
            flavour:
              ${'$'}ref: '#/components/schemas/Flavour'
            note:
              type: string
              description: Special requests for this scoop
              nullable: true

        Flavour:
          type: string
          enum:
            - vanilla
            - chocolate
            - strawberry
            - mint_chip
            - cookie_dough
            - pistachio
            - mango_sorbet

        ConeType:
          type: string
          enum:
            - waffle
            - sugar
            - cake

        CupSize:
          type: string
          enum:
            - small
            - medium
            - large

        Drizzle:
          type: string
          enum:
            - hot_fudge
            - caramel
            - strawberry_syrup

        Topping:
          type: string
          enum:
            - whipped_cream
            - hot_fudge
            - caramel
            - rainbow_sprinkles
            - crushed_nuts

        OrderReceipt:
          type: object
          required:
            - orderId
            - order
            - placedAt
            - totalCents
          properties:
            orderId:
              type: string
              format: uuid
            order:
              ${'$'}ref: '#/components/schemas/IceCreamOrder'
            placedAt:
              type: string
              format: date-time
            readyAt:
              type: string
              format: date-time
              nullable: true
            totalCents:
              type: integer
              description: Total price in cents
            extras:
              type: object
              additionalProperties:
                type: string
              nullable: true

        ApiError:
          type: object
          required:
            - code
            - message
          properties:
            code:
              type: string
            message:
              type: string
            details:
              type: array
              items:
                type: string
              nullable: true
    """.trimIndent()
