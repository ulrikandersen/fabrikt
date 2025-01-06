package com.cjbooms.fabrikt.servers.ktor

import com.example.controllers.TransportationDevicesController.Companion.transportationDevicesRoutes
import com.example.models.Error
import com.example.models.TransportationDevice
import com.example.models.TransportationDeviceDeviceType
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.slot
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KtorKotlinxServerTest {

    @Test
    fun `request body is parsed correctly`() {
        val bodyCapturingSlot = slot<TransportationDevice>()

        testApplication {
            configure()

            routing {
                transportationDevicesRoutes(TransportationDevicesControllerImpl(bodyCapturingSlot))
            }

            val result = client.post("/transportation-devices") {
                header("Content-Type", "application/json")

                setBody(
                    """
                    {
                        "deviceType": "rollerskates",
                        "make": "Roller Master",
                        "model": "Pro"
                    }
                    """.trimIndent()
                )
            }

            assertEquals(HttpStatusCode.OK, result.status)
            assertEquals(
                TransportationDevice(
                    deviceType = TransportationDeviceDeviceType.ROLLERSKATES,
                    make = "Roller Master",
                    model = "Pro",
                ),
                bodyCapturingSlot.captured
            )
        }
    }

    @Test
    fun `body parsing exception results in 400 Bad Request with expected message`() {
        testApplication {
            configure()

            routing {
                transportationDevicesRoutes(TransportationDevicesControllerImpl(slot()))
            }

            val response = client.post("/transportation-devices") {
                header("Content-Type", "application/json")

                setBody(
                    """
                    {
                        "invalid": "body"
                    }
                """.trimIndent()
                )
            }

            // message from Status Pages plugin configured in `configure` function
            val expectedMessage = """
                {
                    "message": "Failed to convert request body to class com.example.models.TransportationDevice",
                    "details": "Illegal input: Fields [deviceType, make, model] are required for type with serial name 'com.example.models.TransportationDevice', but they were missing at path: ${'$'}"
                }
            """.trimIndent()

            assertEquals(BadRequest, response.status)
            assertEquals(expectedMessage, response.bodyAsText())
        }
    }


    @Test
    fun `response body has the expected JSON structure`() {
        testApplication {
            configure()

            routing {
                transportationDevicesRoutes(TransportationDevicesControllerImpl(slot()))
            }

            val response = client.get("/transportation-devices")

            assertEquals(
                """
                    [
                        {
                            "deviceType": "rollerskates",
                            "make": "Roller Master",
                            "model": "Pro"
                        },
                        {
                            "deviceType": "bike",
                            "make": "Bike Co",
                            "model": "Mountain Goat"
                        }
                    ]
                """.trimIndent(),
                response.bodyAsText()
            )
        }
    }

    private fun ApplicationTestBuilder.configure() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        install(StatusPages) {
            exception<BadRequestException> { call, cause ->
                val details = when (cause.cause?.cause) {
                    is JsonConvertException -> cause.cause?.cause?.message // kotlinx.serialization error message
                    else -> null
                }
                call.respond(BadRequest, Error(cause.message ?: "Bad Request", details = details))
            }
        }
    }
}
