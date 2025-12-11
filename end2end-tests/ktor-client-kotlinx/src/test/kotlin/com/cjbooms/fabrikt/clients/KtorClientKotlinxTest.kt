package com.cjbooms.fabrikt.clients

import io.ktor.client.plugins.contentnegotiation.ContentNegotiationConfig
import io.ktor.serialization.kotlinx.json.json

class KtorClientKotlinxTest : KtorClientTestBase() {
    override fun ContentNegotiationConfig.configureSerializer() = json()
}
