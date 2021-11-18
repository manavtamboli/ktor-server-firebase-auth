package com.manavtamboli

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/* region Constants */
private const val FirebaseCredentialsPath = "/firebase_key.json"
private const val NoFirebaseCredentialMessage = "No firebase credentials found. Add firebase credentials to resources folder, and specify path accordingly."
/* endregion */

/**
 * Initializes firebase admin sdk on the server.
 * */
fun initializeFirebase(){
    val stream = Unit::class.java.getResource(FirebaseCredentialsPath)?.openStream() ?: throw RuntimeException(NoFirebaseCredentialMessage)
    val credentials = GoogleCredentials.fromStream(stream)
    val options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build()
    FirebaseApp.initializeApp(options)
}