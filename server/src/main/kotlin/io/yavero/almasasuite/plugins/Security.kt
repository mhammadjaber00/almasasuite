package io.yavero.almasasuite.plugins

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.yavero.almasasuite.config.appConfig
import java.util.*


private const val JWT_EXPIRATION_DAYS = 7L


fun Application.configureSecurity() {

    val jwtConfig = appConfig.jwt


    authentication {
        jwt("jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                throw RuntimeException("Invalid or expired JWT token")
            }
        }
    }
}


fun Application.generateJwtToken(userId: String, email: String): String {
    val jwtConfig = appConfig.jwt

    return JWT.create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * JWT_EXPIRATION_DAYS))
        .sign(Algorithm.HMAC256(jwtConfig.secret))
}


fun hashPassword(password: String): String {
    return BCrypt.withDefaults().hashToString(12, password.toCharArray())
}


fun verifyPassword(password: String, hash: String): Boolean {
    return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}