package com.cjbooms.fabrikt.clients

import io.ktor.client.plugins.contentnegotiation.ContentNegotiationConfig
import io.ktor.serialization.jackson.jackson

class KtorClientJacksonTest : KtorClientTestBase() {
    override fun ContentNegotiationConfig.configureSerializer() = jackson()
}
