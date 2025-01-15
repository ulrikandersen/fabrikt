package examples.parameterNameClash.client

import io.ktor.resources.Resource
import kotlin.String

/**
 * A successful request returns an HTTP 204 response with an empty body.
 *
 * @param pathB
 * @param queryB
 */
@Resource("/example/{b}")
public class ExampleGetByPathB(
    public val pathB: String,
    public val queryB: String,
)

/**
 * A successful request returns an HTTP 204 response with an empty body.
 *
 * @param bodySomeObject example
 * @param querySomeObject
 */
@Resource("/example")
public class ExamplePost(
    public val querySomeObject: String,
)
