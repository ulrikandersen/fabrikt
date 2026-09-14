package examples.deprecatedOperations.client

import examples.deprecatedOperations.models.Subject
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.Deprecated
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface SubjectsClient {
    /**
     *
     *
     * @param id
     */
    @Deprecated(message = "This API operation is deprecated.")
    @HttpExchange(
        url = "/subjects/{id}",
        method = "GET",
        accept = ["application/json"],
    )
    public fun findSubject(
        @PathVariable("id") id: String,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): Subject

    /**
     *
     *
     * @param subject
     * @param id
     */
    @HttpExchange(
        url = "/subjects/{id}",
        method = "POST",
    )
    public fun replaceSubject(
        @RequestBody subject: Subject,
        @PathVariable("id") id: String,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}
