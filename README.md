# ktor-server-firebase-auth
Firebase Authentication is a Ktor plugin which verifies requests authorized by a Firebase Auth Id Token.

### Usage
```kotlin
// Configure FirebaseAuthentication
install(FirebaseAuthentication) {

    // Sets the FirebaseApp instance to verify tokens.
    // If not specified, default FirebaseApp instance will be used.
    firebaseApp = FirebaseApp.getInstance()

    // Sets the authorization type used in the requests.
    // Default is AuthorizationType.Bearer
    authorizationType = AuthorizationType.Parameter("token")

    // Sets whether to check if the tokens were revoked or if the user is disabled.
    // Defaults to false.
    checkRevoked = true
}
```

### Handle request
```kotlin
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
```
```ApplicationCall.getDecodedToken()``` - Extension function for [ApplicationCall] which gets the decoded token (if a valid token was present, or null) from the attributes.
```ApplicationCall.getFailureReason()``` - Extension function for [ApplicationCall] which gets the failure reason if no valid token was found.

### Configuration
**firebaseApp : FirebaseApp** - The ```FirebaseApp``` to use to verify id tokens with Firebase servers. If set to null, default instance of FirebaseApp will be used.
**authorizationType : AutorizationType** -  The type of authorization used in the requests. Currently supports ```AuthorizationType.Bearer```, ```AuthorizationType.Parameter```, ```AuthorizationType.Custom```. Defaults to AuthorizationType.Bearer.
**checkRevoked : Boolean** - A boolean denoting whether to check if the tokens were revoked or if the user is disabled. Defaults to ```false```.

### Contribution
*Any contributions and feature requests are welcome.*
