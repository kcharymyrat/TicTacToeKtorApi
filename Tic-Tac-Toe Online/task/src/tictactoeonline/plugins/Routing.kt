package tictactoeonline.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.*
import tictactoeonline.*
import java.util.*

const val PLAYER1 = "Player1"
const val PLAYER2 = "Player2"
const val DEFAULT_GAME_SIZE = "3x3"

@Serializable
data class Game(
    var game_status: String,
    var field: MutableList<MutableList<String>>? = null,
    var player1: String? = null,
    var player2: String? = null,
    var size: String? = null
)

@Serializable
enum class GameStatus(val status: String) {
    NOT_STARTED("game not started"),
    FIRST_PLAYER_MOVE("1st player's move"),
    SECOND_PLAYER_MOVE("2nd player's move"),
    FIRST_PLAYER_WON("1st player won"),
    SECOND_PLAYER_WON("2nd player won"),
    DRAW("draw");

    override fun toString(): String {
        return status
    }
}

@Serializable
data class GameSetupInfo(val player1: String, val player2: String, val size: String)

@Serializable
data class RespondsForGameStep(
    var status: String,
    var player1: String? = null,
    var player2: String? = null,
    var size: String? = null
)

@Serializable
enum class RespondsStatus(val status: String) {
    NEW_GAME_STARTER("New game started"),
    FIRST_PLAYER_MOVE("1st player's move"),
    SECOND_PLAYER_MOVE("2nd player's move"),
    FIRST_PLAYER_WON("1st player won"),
    SECOND_PLAYER_WON("2nd player won"),
    DRAW("draw");

    override fun toString(): String {
        return status
    }
}

@Serializable
data class PlayerMove(val move: String)

@Serializable
data class UserCredentials(val email: String, val password: String)

// An empty Game was created
val currentGame = Game(game_status = GameStatus.NOT_STARTED.status)


// To store all successfully registered emails
val registeredEmails = mutableMapOf<String, String>()




fun Application.configureRouting() {

    routing {

        post("/signup") {
            println()
            println("post(\"/signup\")")

            val receivedText = call.receiveText()
            println("receivedText = $receivedText")

            try {
                val user = Json.decodeFromString<UserCredentials>(receivedText)
                print("user = $user")
                if (user.email.isBlank() || user.password.isBlank()) throw Exception("Blank email or password")
                if (doesEmailExist(user.email)) throw Exception("Email already exists")

                registeredEmails[user.email] = user.password

                call.respondText(
                    text = Json.encodeToString(mapOf("status" to "Signed Up")),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            } catch (_: Exception) {
                call.respondText(
                    text = Json.encodeToString(mapOf("status" to "Registration failed")),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.Forbidden
                )
            }
        }

        post("/signin") {
            println()
            println("post(\"/signin\")")

            val receivedText = call.receiveText()
            println("receivedText = $receivedText")

            try {
                val user = Json.decodeFromString<UserCredentials>(receivedText)
                println("user =$user")
                if (!doesEmailExist(user.email)) throw Exception("No such user exists")
                if (registeredEmails[user.email] != user.password) throw Exception("No such user exists")

                val token = JWT.create()
                    .withClaim("email", user.email)
                    .sign(Algorithm.HMAC256(secret))

                call.respondText(
                    text = Json.encodeToString(mapOf("status" to "Signed In", "token" to token)),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )

                println("token = $token")
            } catch (_:Exception) {
                call.respondText(
                    text = Json.encodeToString(mapOf("status" to "Authorization failed")),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.Forbidden
                )
            }
        }


        post("/game") {
            println()
            println("post(\"/game\")")
            try {
                val receivedText = call.receiveText()
                println("call.receiveText() = $receivedText, isBlank = ${receivedText.isBlank()}")

                val gameSetupInfo = Json.decodeFromString<GameSetupInfo>(receivedText)
                println("gameSetupInfo = $gameSetupInfo")

                val respondForGameStep = initialSetupOfRespondAndGame(gameSetupInfo)

                println("try => respondForGameStep = ${Json.encodeToString(respondForGameStep)}")
                println("try => newGame = ${Json.encodeToString(currentGame)}")

                call.respondText(
                    text = Json.encodeToString(respondForGameStep),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {

                val gameSetupInfo = GameSetupInfo(
                    player1 = PLAYER1,
                    player2 = PLAYER2,
                    size = DEFAULT_GAME_SIZE
                )
                println("gameSetupInfo = $gameSetupInfo")

                val respondForGameStep = initialSetupOfRespondAndGame(gameSetupInfo)

                println("catch => respondForGameStep = ${Json.encodeToString(respondForGameStep)}")
                println("catch => newGame = ${Json.encodeToString(currentGame)}")

                call.respondText(
                    text = Json.encodeToString(respondForGameStep),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            }

        }  // end of post("/game")

        post("/game/move") {
            println()
            println("post(\"/game/move\")")

            try {
                val receivedText = call.receiveText()
                println("call.receiveText() = $receivedText, isBlank = ${receivedText.isBlank()}")

                val playerMove = Json.decodeFromString<PlayerMove>(receivedText)
                val moveCoords = playerMove.move
                val moveCoordsPair = getMoveCoords(moveCoords)
                println("try => playerMove = ${Json.encodeToString(playerMove)}")

                val fieldCoordsPair = getGameFieldCoords(currentGame.size.toString())
                val x = fieldCoordsPair.first
                val y = fieldCoordsPair.second
                val field = currentGame.field!!

                if (!validateMoveCoords(moveCoords, x, y, field)) throw Exception("Incorrect or impossible move")

                val symbol = getPlayerSymbol(currentGame.game_status)
                val nextPlayerGameStatus = changePlayerTurn(currentGame.game_status)

                insertMove(moveCoordsPair, field, symbol)
                if (isWon(moveCoordsPair, field, symbol)) {
                    if (symbol == "X") currentGame.game_status = GameStatus.FIRST_PLAYER_WON.status
                    else currentGame.game_status = GameStatus.SECOND_PLAYER_WON.status
                } else if (isDraw(field)) {
                    currentGame.game_status = GameStatus.DRAW.status
                } else {
                    currentGame.game_status = nextPlayerGameStatus
                }
                call.respondText(
                    text = Json.encodeToString(mapOf("status" to "Move done")),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respondText(
                    text = Json.encodeToString(mapOf("status" to "Incorrect or impossible move")),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.BadRequest
                )
            }
        }  // end of post("/game/move")

        get("/game/status") {
            println()
            println("get(\"/game/status\")")
            val receivedText = call.receiveText()
            println("call.receiveText() = $receivedText, isBlank = ${receivedText.isBlank()}")
            println("Json.encodeToString(newGame) = ${Json.encodeToString(currentGame)}")

            call.respondText(
                text = Json.encodeToString(currentGame),
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.OK
            )
        } // end of get("/game/status")

    }  // end_of_routing {}
}  // end_Application.confugure()

fun doesEmailExist(email: String): Boolean {
    return registeredEmails.contains(email)
}



fun getPlayerSymbol(player: String): String {
    return if (player == GameStatus.FIRST_PLAYER_MOVE.status) "X" else "O"
}

fun changePlayerTurn(player: String): String {
    println("player = $player")
    return if (player == GameStatus.FIRST_PLAYER_MOVE.status) GameStatus.SECOND_PLAYER_MOVE.status else GameStatus.FIRST_PLAYER_MOVE.status
}

// ------Initial Setup Functions---mostly used in get("/post")--------//
fun initialSetupOfRespondAndGame(gameSetupInfo: GameSetupInfo): RespondsForGameStep {
    val respondForGameStep = getRespondForGameStep(
        status = RespondsStatus.NEW_GAME_STARTER.status,
        player1 = gameSetupInfo.player1,
        player2 = gameSetupInfo.player2,
        size = gameSetupInfo.size
    )

    currentGameInitialSetup(
        GameStatus.FIRST_PLAYER_MOVE.status,
        respondForGameStep.player1.toString(),
        respondForGameStep.player2.toString(),
        respondForGameStep.size.toString()
    )

    return respondForGameStep
}

fun currentGameInitialSetup(gameStatus: String, player1: String, player2: String, size: String) {
    // generate the fieldListOfList
    val fieldCoordsPair = getGameFieldCoords(size)
    val x = fieldCoordsPair.first
    val y = fieldCoordsPair.second
    val fieldListOfList = createListOfListField(x, y)

    currentGame.game_status = gameStatus
    currentGame.field = fieldListOfList
    currentGame.player1 = player1
    currentGame.player2 = player2
    currentGame.size = size
}

fun getRespondForGameStep(status: String, player1: String?, player2: String?, size: String?): RespondsForGameStep {
    var validatedSize = size
    if (size != null) {
        val fieldCoordPair = getGameFieldCoords(size)
        validatedSize = "${fieldCoordPair.first}x${fieldCoordPair.second}"
    }
    return RespondsForGameStep(
        status = status,
        player1 = player1,
        player2 = player2,
        size = validatedSize
    )
}

// ------ END Of Initial Setup Functions---mostly used in get("/post")--------//