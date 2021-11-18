package com.manavtamboli

import com.google.firebase.FirebaseApp
import com.manavtamboli.FirebaseAuthentication.AuthorizationType
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "127.0.0.1") {
        configureApplication()
    }.start(wait = true)
}

fun Application.configureApplication(){

    // Initialize FirebaseApp.
    initializeFirebase()

    // Configure FirebaseAuthentication
    install(FirebaseAuthentication) {

        // Sets the FirebaseApp instance to verify tokens.
        // If not specified, default FirebaseApp instance will be used.
        firebaseApp = FirebaseApp.getInstance()

        // Sets the authorization type used in the requests.
        // Default is AuthorizationType.Bearer
        authorizationType = AuthorizationType.Bearer

        // Sets whether to check if the tokens were revoked or if the user is disabled.
        // Defaults to false.
        checkRevoked = true
    }

    // Configure Routing
    routing {
        get("/test"){
            val decodedToken = call.getDecodedToken()
            if (decodedToken == null){
                val status = when (call.getFailureReason()){
                    FirebaseAuthentication.FailureReason.InvalidFirebaseApp -> HttpStatusCode.InternalServerError
                    FirebaseAuthentication.FailureReason.TokenInvalid -> HttpStatusCode.Unauthorized
                    FirebaseAuthentication.FailureReason.TokenNullOrBlank -> HttpStatusCode.BadRequest
                    null -> throw RuntimeException() // This can never occur, as decodedToken is null.
                }
                call.respond(status)
            } else {
                call.respond(HttpStatusCode.OK, decodedToken.uid)
            }
        }
    }
}