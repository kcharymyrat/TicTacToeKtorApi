package tictactoeonline

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*
import tictactoeonline.plugins.*

const val secret = "ut920BwH09AOEDx5"
const val myRealm = "Access to TicTacToe Game"

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(Authentication) {
        jwt("gameAuth") {
            realm = myRealm

            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .build()
            )

            validate {
                if(it.payload.getClaim("email").asString().isNotBlank()) {
                    JWTPrincipal(it.payload)
                } else {
                    null
                }
            }

            challenge {defaultScheme, realm ->
                call.respond(HttpStatusCode.Forbidden, "Token is not valid or has expired")
            }
        }
    }

    configureRouting()
}






