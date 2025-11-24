package com.socialmedia.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "your-secret-key-change-in-production"
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "social-media-app"
    val jwtRealm = System.getenv("JWT_REALM") ?: "social-media"
    
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

object JWTConfig {
    private val jwtSecret = System.getenv("JWT_SECRET") ?: "your-secret-key-change-in-production"
    private val jwtIssuer = System.getenv("JWT_ISSUER") ?: "social-media-app"
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    
    fun generateToken(userId: Int, username: String): String {
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 86400000 * 30)) // 30 days
            .sign(algorithm)
    }
}
