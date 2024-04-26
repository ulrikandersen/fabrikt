package com.cjbooms.fabrikt.servers.ktor

import com.example.controllers.DefaultController
import com.example.controllers.DefaultController.Companion.defaultRoutes
import com.example.controllers.NoneController
import com.example.controllers.NoneController.Companion.noneRoutes
import com.example.controllers.OptionalController
import com.example.controllers.OptionalController.Companion.optionalRoutes
import com.example.controllers.RequiredController
import com.example.controllers.RequiredController.Companion.requiredRoutes
import com.example.controllers.TypedApplicationCall
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KtorAuthenticationTest {

    @Nested
    inner class OptionalAuth {
        @Test
        fun `returns principal with auth provided`() {
            val principalCaptureSlot = slot<UserIdPrincipal?>()

            testApplication {
                configure()

                routing {
                    optionalRoutes(object : OptionalController {
                        override suspend fun testPath(
                            testString: String,
                            principal: Principal?,
                            call: ApplicationCall
                        ) {
                            principalCaptureSlot.captured = principal as? UserIdPrincipal
                            call.respond(HttpStatusCode.OK)
                        }
                    })
                }

                val response = client.get("/optional?testString=test") {
                    header("Authorization", "Basic dGVzdDp0ZXN0") // just anything to trigger auth
                }

                assertEquals("", response.bodyAsText())

                assertTrue(principalCaptureSlot.captured is UserIdPrincipal)
                assertEquals("routeAuth", principalCaptureSlot.captured?.name)
            }
        }

        @Test
        fun `returns null principal without auth provided`() {
            val principalCaptureSlot = slot<UserIdPrincipal?>()

            testApplication {
                configure()

                routing {
                    optionalRoutes(object : OptionalController {
                        override suspend fun testPath(
                            testString: String,
                            principal: Principal?,
                            call: ApplicationCall
                        ) {
                            principalCaptureSlot.captured = principal as? UserIdPrincipal
                            call.respond(HttpStatusCode.OK)
                        }
                    })
                }

                client.get("/optional?testString=test")

                assertEquals(null, principalCaptureSlot.captured)
            }
        }
    }

    @Nested
    inner class RequiredAuth {
        @Test
        fun `returns principal with auth provided`() {
            val principalCaptureSlot = slot<UserIdPrincipal?>()

            testApplication {
                configure()

                routing {
                    requiredRoutes(object : RequiredController {
                        override suspend fun testPath(testString: String, principal: Principal, call: ApplicationCall) {
                            principalCaptureSlot.captured = principal as UserIdPrincipal
                            call.respond(HttpStatusCode.OK)
                        }
                    })
                }

                client.get("/required?testString=test") {
                    header("Authorization", "Basic dGVzdDp0ZXN0") // just anything to trigger auth
                }

                assertTrue(principalCaptureSlot.captured is UserIdPrincipal)
                assertEquals("routeAuth", principalCaptureSlot.captured?.name)
            }
        }

        @Test
        fun `returns 401 without auth provided`() {
            val principalCaptureSlot = slot<UserIdPrincipal?>()

            testApplication {
                configure()

                routing {
                    requiredRoutes(object : RequiredController {
                        override suspend fun testPath(testString: String, principal: Principal, call: ApplicationCall) {
                            principalCaptureSlot.captured = principal as UserIdPrincipal // should not get called
                            call.respond(HttpStatusCode.OK)
                        }
                    })
                }

                val response = client.get("/required?testString=test")

                assertEquals(HttpStatusCode.Unauthorized, response.status)
                assertFalse(principalCaptureSlot.isCaptured)
            }
        }
    }

    @Nested
    inner class NoAuth {
        @Test
        fun `does not require auth`() {
            testApplication {
                configure()

                routing {
                    noneRoutes(object : NoneController {
                        override suspend fun testPath(testString: String, call: ApplicationCall) {
                            call.respond(HttpStatusCode.OK)
                        }
                    })
                }

                val response = client.get("/none?testString=test")

                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
    }

    @Nested
    inner class DefaultAuth {
        @Test
        fun `uses auth from default`() {
            val principalCaptureSlot = slot<UserIdPrincipal?>()

            testApplication {
                configure()

                routing {
                    defaultRoutes(object : DefaultController {
                        override suspend fun testPath(testString: String, principal: Principal, call: ApplicationCall) {
                            principalCaptureSlot.captured = principal as UserIdPrincipal
                            call.respond(HttpStatusCode.OK)
                        }
                    })
                }

                client.get("/default?testString=test") {
                    header("Authorization", "Basic dGVzdDp0ZXN0") // just anything to trigger auth
                }

                assertTrue(principalCaptureSlot.captured is UserIdPrincipal)
                assertEquals("defaultAuth", principalCaptureSlot.captured?.name)
            }
        }
    }
}

private fun ApplicationTestBuilder.configure() {
    install(Authentication) {
        // used for explicit auth
        basic("BasicAuth") {
            validate {
                UserIdPrincipal("routeAuth") // always authenticate
            }
        }

        // used for default auth
        basic("basicAuth") {
            validate {
                UserIdPrincipal("defaultAuth") // always authenticate
            }
        }
    }

    install(StatusPages)

    install(ContentNegotiation) {
        jackson {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            registerKotlinModule()
        }
    }
}