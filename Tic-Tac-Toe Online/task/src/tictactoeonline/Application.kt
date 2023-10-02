package tictactoeonline

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import tictactoeonline.models.Users
import tictactoeonline.plugins.*

const val secret = "ut920BwH09AOEDx5"
const val myRealm = "Access to TicTacToe Game"
val driverClassName = "org.h2.Driver"
val jdbcURL = "jdbc:h2:file:./build/db.mv.db "



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
                call.respondText(
                    text = Json.encodeToString(mapOf("status" to "Authorization failed")),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.Unauthorized
                )
            }
        }
    }

    GAMEID = 1
    allGames = mutableListOf<Game>()

    Database.connect(jdbcURL, driverClassName)
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users)
    }

    configureRouting()
}






