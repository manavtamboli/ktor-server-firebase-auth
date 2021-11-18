package com.manavtamboli

import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "127.0.0.1") {
        routing {
            get("/"){

            }
        }
    }.start(wait = true)
}