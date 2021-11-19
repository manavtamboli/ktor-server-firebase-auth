package com.manavtamboli

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ValidTestIdToken = "PLACE-A-VALID-ID-TOKEN-HERE"

class ApplicationTest {

    @Test
    fun testBearer() {
        withTestApplication({ configureApplication() }) {

            // Test Null token
            handleRequest(HttpMethod.Get, "/test").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }

            // Test Blank token
            handleRequest(HttpMethod.Get, "/test"){
                addHeader(HttpHeaders.Authorization, "Bearer ")
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }

            // Test Invalid token
            handleRequest(HttpMethod.Get, "/test"){
                addHeader(HttpHeaders.Authorization, "Bearer ${generateRandomString()}")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            // Test Valid Token
            handleRequest(HttpMethod.Get, "/test"){
                addHeader(HttpHeaders.Authorization, "Bearer $ValidTestIdToken")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testParameter(){
        withTestApplication({ configureApplication() }) {

            // Test Null token
            handleRequest(HttpMethod.Get, "/test").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }

            // Test Blank token
            handleRequest(HttpMethod.Get, "/test?token=").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }

            // Test Invalid token
            handleRequest(HttpMethod.Get, "/test?token=${generateRandomString()}").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            // Test Valid Token
            handleRequest(HttpMethod.Get, "/test?token=$ValidTestIdToken").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    private fun generateRandomString(length : Int = 64) : String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}