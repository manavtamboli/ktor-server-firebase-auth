package com.manavtamboli

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ configureApplication() }) {

            // Test Unauthorized
            handleRequest(HttpMethod.Get, "/test").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }
}