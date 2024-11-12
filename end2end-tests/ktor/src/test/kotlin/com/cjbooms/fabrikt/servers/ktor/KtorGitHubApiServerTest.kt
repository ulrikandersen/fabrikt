package com.cjbooms.fabrikt.servers.ktor

import com.example.controllers.ContributorsController.Companion.contributorsRoutes
import com.example.controllers.InternalEventsController.Companion.internalEventsRoutes
import com.example.models.BulkEntityDetails
import com.example.models.EntityDetails
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.slot
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.serializersModule
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KtorGitHubApiServerTest {

    @Test
    fun `request body is parsed correctly`() {
        val bodyCapturingSlot = slot<BulkEntityDetails>()

        testApplication {
            configure()

            routing {
                internalEventsRoutes(InternalEventsControllerImpl(bodyCapturingSlot))
            }

            client.post("/internal/events") {
                header("Authorization", "Basic dGVzdDp0ZXN0") // just anything to trigger auth
                header("Content-Type", "application/json")

                setBody(
                    """
                    {
                        "entities": [{
                            "id": "1"
                        }]
                    }
                    """.trimIndent()
                )
            }

            assertEquals(
                BulkEntityDetails(
                    entities = listOf(
                        EntityDetails(id = "1",
//                            properties = mutableMapOf("entity1PropKey" to "entity1PropValue")
                        )
                    ),
//                    properties = mutableMapOf("propKey" to "propValue")
                ),
                bodyCapturingSlot.captured
            )
        }
    }

    @Test
    fun `response body has the expected JSON structure`() {
        testApplication {
            configure()

            routing {
                internalEventsRoutes(InternalEventsControllerImpl(slot()))
            }

            val response = client.post("/internal/events") {
                header("Authorization", "Basic dGVzdDp0ZXN0") // just anything to trigger auth
                header("Content-Type", "application/json")

                setBody(
                    """
                    {
                        "entities": []
                    }
                    """.trimIndent()
                )
            }

            assertEquals(
                "{\"change_events\":[{\"entity_id\":\"entityId\",\"data\":{\"dataKey\":1,\"otherDataKey\":\"value\"}}]}",
                response.bodyAsText()
            )
        }
    }

    @Test
    fun `headers and query parameter are retrieved and converted correctly`() {
        val xFlowIdCapturingSlot = slot<String?>()
        val xFlowIdValue = "testValue"

        val limitCapturingSlot = slot<Int?>()
        val limitValue = 10

        testApplication {
            configure()

            routing {
                contributorsRoutes(ContributorsControllerImpl(xFlowIdCapturingSlot, limitCapturingSlot))
            }

            client.get("/contributors?limit=$limitValue") {
                header("X-Flow-Id", xFlowIdValue)
            }

            assertEquals(xFlowIdValue, xFlowIdCapturingSlot.captured)
            assertEquals(limitValue, limitCapturingSlot.captured)
        }
    }

    @Test
    fun `method with no return value returns expected HTTP code and empty body`() {
        val xFlowIdCapturingSlot = slot<String?>()
        val limitCapturingSlot = slot<Int?>()

        testApplication {
            configure()

            routing {
                contributorsRoutes(ContributorsControllerImpl(xFlowIdCapturingSlot, limitCapturingSlot))
            }

            val response = client.post("/contributors") {
                header("Content-Type", "application/json")

                setBody(
                    """
                    {
                        "username": "test",
                        "status": "active"
                    }
                """.trimIndent()
                )
            }

            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals("", response.bodyAsText())
        }
    }

    @Test
    fun `body parsing exception results in 400 Bad Request with expected message`() {
        testApplication {
            configure()

            routing {
                contributorsRoutes(ContributorsControllerImpl(slot(), slot()))
            }

            val response = client.post("/contributors") {
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
            val expectedMessage = "Failed to convert request body to class com.example.models.Contributor"

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(expectedMessage, response.bodyAsText())
        }
    }

    @Test
    fun `query parameter with wrong format results in 400 Bad Request with expected message`() {
        testApplication {
            configure()

            routing {
                contributorsRoutes(ContributorsControllerImpl(slot(), slot()))
            }

            val response = client.get("/contributors?limit=invalid")

            // message from Status Pages plugin configured in `configure` function
            val expectedMessage = "Request parameter limit couldn't be parsed/converted to Int"

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(expectedMessage, response.bodyAsText())
        }
    }

    @Test
    fun `a function which does not respond results in 404 Not Found`() {
        testApplication {
            configure()

            routing {
                contributorsRoutes(ContributorsControllerImpl(slot(), slot()))
            }

            val response = client.get("/contributors/1")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    object AnySerializer : KSerializer<Any> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any")

        override fun serialize(encoder: Encoder, value: Any) {
            when (value) {
                is Int -> encoder.encodeInt(value)
                is String -> encoder.encodeString(value)
                is Boolean -> encoder.encodeBoolean(value)
                is Double -> encoder.encodeDouble(value)
//                is DataTypeA -> encoder.encodeSerializableValue(DataTypeA.serializer(), value)
                else -> throw SerializationException("Unsupported type: ${value::class}")
            }
        }

        override fun deserialize(decoder: Decoder): Any {
            val input = decoder as? JsonDecoder ?: throw SerializationException("This class can be loaded only by JSON")
            val jsonElement = input.decodeJsonElement()
            return when (jsonElement) {
                is JsonPrimitive -> when {
                    jsonElement.isString -> jsonElement.content
                    jsonElement.booleanOrNull != null -> jsonElement.boolean
                    jsonElement.intOrNull != null -> jsonElement.int
                    jsonElement.doubleOrNull != null -> jsonElement.double
                    else -> throw SerializationException("Unsupported primitive type: $jsonElement")
                }
                is JsonObject -> jsonElement
                else -> throw SerializationException("Unsupported JSON element: $jsonElement")
            }
        }
    }

    private fun ApplicationTestBuilder.configure() {
        install(ContentNegotiation) {
            json(Json {
                serializersModule = SerializersModule { contextual(Any::class, AnySerializer) }
            })
        }

        install(StatusPages) {
            exception<BadRequestException> { call, cause ->
                println("Caught exception: ${cause.cause?.cause}")
                call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad Request")
            }
        }
    }
}
