package examples.singleAllOf.controllers

import examples.singleAllOf.models.Result
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.`get`
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import kotlin.Any
import kotlin.String
import kotlin.Suppress

public interface TestController {
    /**
     * Route is expected to respond with [examples.singleAllOf.models.Result].
     * Use [examples.singleAllOf.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun test(call: TypedApplicationCall<Result>)

    public companion object {
        /**
         * Mounts all routes for the Test resource
         *
         * - GET /test
         */
        public fun Route.testRoutes(controller: TestController) {
            `get`("/test") {
                controller.test(TypedApplicationCall.from(call))
            }
        }

        /**
         * Gets parameter value associated with this name or null if the name is not present.
         * Converting to type R using DefaultConversionService.
         *
         * Throws:
         *   ParameterConversionException - when conversion from String to R fails
         */
        private inline fun <reified R : Any> Parameters.getTyped(name: String): R? {
            val values = getAll(name) ?: return null
            val typeInfo = typeInfo<R>()
            return try {
                @Suppress("UNCHECKED_CAST")
                DefaultConversionService.fromValues(values, typeInfo) as R
            } catch (cause: Exception) {
                throw ParameterConversionException(
                    name,
                    typeInfo.type.simpleName
                        ?: typeInfo.type.toString(),
                    cause,
                )
            }
        }

        /**
         * Gets first value from the list of values associated with a name.
         *
         * Throws:
         *   BadRequestException - when the name is not present
         */
        private fun Headers.getOrFail(name: String): String = this[name] ?: throw
            BadRequestException("Header " + name + " is required")
    }
}

/**
 * Decorator for Ktor's ApplicationCall that provides type safe variants of the [respond] functions.
 *
 * It can be used as a drop-in replacement for [io.ktor.server.application.ApplicationCall].
 *
 * @param R The type of the response body
 */
public class TypedApplicationCall<R : Any> private constructor(
    private val applicationCall: ApplicationCall,
) : ApplicationCall by applicationCall {
    @Suppress("unused")
    public suspend inline fun <reified T : R> respondTyped(message: T) {
        respond(message)
    }

    @Suppress("unused")
    public suspend inline fun <reified T : R> respondTyped(status: HttpStatusCode, message: T) {
        respond(status, message)
    }

    public companion object {
        public fun <R : Any> from(applicationCall: ApplicationCall): TypedApplicationCall<R> =
            TypedApplicationCall<R>(applicationCall)
    }
}