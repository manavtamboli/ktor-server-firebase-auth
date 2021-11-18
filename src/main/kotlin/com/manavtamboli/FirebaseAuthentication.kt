package com.manavtamboli

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import com.manavtamboli.FirebaseAuthentication.Companion.DecodedTokenKey
import com.manavtamboli.FirebaseAuthentication.Companion.FailureReasonKey
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

class FirebaseAuthentication(configuration: Configuration) {

    private val auth = FirebaseAuth.getInstance(configuration.firebaseApp ?: FirebaseApp.getInstance())
    private val authorizationType = configuration.authorizationType
    private val checkRevoked = configuration.checkRevoked

    /**
     * Configuration class for [FirebaseAuthentication].
     * */
    class Configuration {
        /**
         * The [FirebaseApp] to use to verify id tokens with Firebase servers. If set to null, [FirebaseApp.getInstance] will be used to get a [FirebaseApp].
         * */
        var firebaseApp : FirebaseApp? = null

        /**
         * The type of authorization used in the requests. Defaults to [AuthorizationType.Bearer].
         * */
        var authorizationType : AuthorizationType = AuthorizationType.Bearer

        /**
         * A boolean denoting whether to check if the tokens were revoked or if the user is disabled. Defaults to false.
         * */
        var checkRevoked : Boolean = false
    }

    /**
     * Represents type of authorization.
     * */
    sealed class AuthorizationType {

        /**
         * Takes [ApplicationCall] and returns the IdToken, if present, or null.
         * */
        abstract val getToken : (ApplicationCall) -> String?

        /**
         * IdToken sent as Bearer scheme.
         * */
        object Bearer : AuthorizationType() {
            override val getToken: (ApplicationCall) -> String? = {
                it.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer")?.trim()
            }
        }

        /**
         * IdToken sent as a parameter.
         * @param parameterName Specifies the parameter name of the IdToken.
         * */
        class Parameter(val parameterName : String) : AuthorizationType() {
            override val getToken: (ApplicationCall) -> String? = {
                it.parameters[parameterName]
            }
        }

        /**
         * Handle custom authorization scheme.
         * */
        class Custom(override val getToken: (ApplicationCall) -> String?) : AuthorizationType()
    }

    private fun intercept(context : PipelineContext<Unit, ApplicationCall>){
        context.call.run {
            val idToken = authorizationType.getToken(this)
            if (!idToken.isNullOrBlank()){
                try {
                    val decodedToken = auth.verifyIdToken(idToken, checkRevoked)
                    attributes.put(DecodedTokenKey, decodedToken)
                } catch (ex : IllegalArgumentException){
                    attributes.put(FailureReasonKey, FailureReason.InvalidFirebaseApp)
                } catch (ex : FirebaseAuthException){
                    attributes.put(FailureReasonKey, FailureReason.TokenInvalid)
                } catch (ex : Exception) {
                    throw ex
                }
            } else {
                attributes.put(FailureReasonKey, FailureReason.TokenNullOrBlank)
            }
        }
    }

    /**
     * Represents failure reason for failed verification.
     * */
    sealed class FailureReason {

        /**
         * IdToken is invalid
         * */
        object TokenInvalid : FailureReason()

        /**
         * IdToken is null or blank.
         * */
        object TokenNullOrBlank : FailureReason()

        /**
         * Firebase app is invalid.
         * */
        object InvalidFirebaseApp : FailureReason()
    }

    companion object : ApplicationFeature<ApplicationCallPipeline, Configuration, FirebaseAuthentication> {

        internal val FailureReasonKey : AttributeKey<FailureReason> = AttributeKey("FirebaseAuthenticationFailureReasonKey")
        internal val DecodedTokenKey : AttributeKey<FirebaseToken> = AttributeKey("FirebaseAuthenticationDecodedTokenKey")

        override val key: AttributeKey<FirebaseAuthentication> = AttributeKey("FirebaseAuthenticationPluginKey")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): FirebaseAuthentication {
            val config = Configuration().apply(configure)
            return FirebaseAuthentication(config).apply {
                pipeline.intercept(ApplicationCallPipeline.Features){
                    intercept(this)
                }
            }
        }
    }
}

/**
 * Extension function for [ApplicationCall] which gets the decoded token (if a valid token was present, or null) from the attributes.
 * */
fun ApplicationCall.getDecodedToken() : FirebaseToken? = attributes.getOrNull(DecodedTokenKey)

/**
 * Extension function for [ApplicationCall] which gets the failure reason if no valid token was found.
 * */
fun ApplicationCall.getFailureReason() : FirebaseAuthentication.FailureReason? = attributes.getOrNull(FailureReasonKey)