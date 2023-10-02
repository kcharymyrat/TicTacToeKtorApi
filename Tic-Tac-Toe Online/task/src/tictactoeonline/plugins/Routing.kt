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



@Serializable
data class Game(
    var game_id: Int,
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
    var game_id: Int,
    var status: String,
    var player1: String? = null,
    var player2: String? = null,
    var size: String? = null
)

@Serializable
data class GameRegistrar(
    var game_id: Int,
    var player1: String,
    var player2: String,
    var size: String
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


// To store all successfully registered emails
@Serializable
val registeredEmails = mutableMapOf<String, String>()

@Serializable
var allGames = mutableListOf<Game>()

var GAMEID = 1

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
        }  // end of post("/signup")

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
        }  // end of post("/signin")

        authenticate("gameAuth") {
            post("/game") {
                println()
                println("post(\"/game\")")
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userEmail = principal!!.payload.getClaim("email").asString()
                    println("userEmail from Token = $userEmail")

                    val receivedText = call.receiveText()
                    println("call.receiveText() = $receivedText, isBlank = ${receivedText.isBlank()}")

                    val gameSetupInfo = Json.decodeFromString<GameSetupInfo>(receivedText)
                    println("gameSetupInfo = $gameSetupInfo")

                    if (userEmail != gameSetupInfo.player1 && userEmail != gameSetupInfo.player2) throw Exception("Wrong Game setup")

//                    if (userEmail == gameSetupInfo.player1) {
//                        if (gameSetupInfo.player2 == "") throw Exception("Identical emails")
//                    }
//                    if (userEmail == gameSetupInfo.player2) {
//                        if (gameSetupInfo.player1 == "") throw Exception("Identical emails")
//                    }

                    val respondAndGame = initialSetupOfRespondAndGame(gameSetupInfo)
                    val respondForGameStep = respondAndGame.first
                    val newGame = respondAndGame.second

                    println("try => respondForGameStep = ${Json.encodeToString(respondForGameStep)}")
                    println("try => newGame = ${Json.encodeToString(newGame)}")

                    call.respondText(
                        text = Json.encodeToString(respondForGameStep),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    println("catch => Creating a game failed")
                    call.respondText(
                        text = Json.encodeToString(mapOf("status" to "Creating a game failed")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.Forbidden
                    )
                }

            }  // end of post("/game")

            post("/game/{game_id}/join") {
                println()
                val id = call.parameters["game_id"]?.toInt()
                println("post(\"/game/$id/join\")")
                try {
                    if (id == null) throw Exception("No such game_id was inserted")

                    val game = allGames.find { it.game_id == id } ?: throw Exception("No game with game_id = $id")
                    println("game = ${Json.encodeToString(game)}")

                    if(!(game.player1 == "" || game.player2 == "")) throw Exception("Not your game to join")

                    val principal = call.principal<JWTPrincipal>()
                    val email = principal!!.payload.getClaim("email").asString()
                    println("email from token = $email")

                    if (game.player1 == "") game.player1 = email else game.player2 = email
                    game.game_status = GameStatus.FIRST_PLAYER_MOVE.status

                    println("joined: game = ${Json.encodeToString(game)}")

                    call.respondText(
                        text = Json.encodeToString(mapOf("status" to "Joining the game succeeded")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    call.respondText(
                        text = Json.encodeToString(mapOf("status" to "Joining the game failed")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.Forbidden
                    )
                }
            }  // post("/game/{game_id}/join")

            post("/game/{game_id}/move") {
                println()
                val id = call.parameters["game_id"]?.toInt()
                println("post(\"/game/$id/move\")")
                try {
                    val game = allGames.find { it.game_id == id } ?: throw Exception("You have no rights to make this move")
                    val status = game.game_status
                    val player1 = game.player1!!
                    val player2 = game.player2!!
                    println("game = $game")
                    val receivedText = call.receiveText()
                    println("call.receiveText() = $receivedText")

                    val principal = call.principal<JWTPrincipal>()
                    val email = principal!!.payload.getClaim("email").asString()
                    println("email = $email")

                    val firstStatus = GameStatus.FIRST_PLAYER_MOVE.status
                    val secondStatus = GameStatus.SECOND_PLAYER_MOVE.status

                    if (status == firstStatus && email != player1) throw Exception("You have no rights to make this move")
                    if (status == secondStatus && email != player2) throw Exception("You have no rights to make this move")


                    val playerMove = Json.decodeFromString<PlayerMove>(receivedText)
                    val moveCoords = playerMove.move
                    val moveCoordsPair = getMoveCoords(moveCoords)

                    println("try => playerMove = ${Json.encodeToString(playerMove)}")

                    val fieldCoordsPair = getGameFieldCoords(game.size.toString())
                    val x = fieldCoordsPair.first
                    val y = fieldCoordsPair.second
                    val field = game.field!!


                    if (!validateMoveCoords(moveCoords, x, y, field)) throw Exception("Incorrect or impossible move")

                    val symbol = getPlayerSymbol(game.game_status)
                    val nextPlayerGameStatus = changePlayerTurn(game.game_status)


                    println("symbol = $symbol, nextPlayerGameStatus = $nextPlayerGameStatus")

                    if (game.game_status == GameStatus.FIRST_PLAYER_WON.status) throw Exception("You have no rights to make this move")
                    if (game.game_status == GameStatus.SECOND_PLAYER_WON.status) throw Exception("You have no rights to make this move")

                    insertMove(moveCoordsPair, field, symbol)
                    if (isWon(moveCoordsPair, field, symbol)) {
                        if (symbol == "X") game.game_status = GameStatus.FIRST_PLAYER_WON.status
                        else game.game_status = GameStatus.SECOND_PLAYER_WON.status
                    } else if (isDraw(field)) {
                        game.game_status = GameStatus.DRAW.status
                    } else {
                        game.game_status = nextPlayerGameStatus
                    }


                    call.respondText(
                        text = Json.encodeToString(mapOf("status" to "Move done")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                    println("move DONE")
                } catch (e: Exception) {
                    println("move FAILED")
                    if (e.message == "You have no rights to make this move") {
                        call.respondText(
                            text = Json.encodeToString(mapOf("status" to "${e.message}")),
                            contentType = ContentType.Application.Json,
                            status = HttpStatusCode.Forbidden
                        )
                    } else {
                        call.respondText(
                            text = Json.encodeToString(mapOf("status" to "${e.message}")),
                            contentType = ContentType.Application.Json,
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }
            }  // end of post("/game/{game_id}/move")

            get("/game/{game_id}/status") {
                println()
                val id = call.parameters["game_id"]?.toInt()
                println("get(\"/game/$id/status\")")
                try {
                    if (id == null) throw Exception("No such game_id was inserted")

                    val game = allGames.find { it.game_id == id } ?: throw Exception("No game with game_id = $id")

                    println("Json.encodeToString(game) = ${Json.encodeToString(game)}")

                    val principal = call.principal<JWTPrincipal>()
                    val email = principal!!.payload.getClaim("email").asString()
                    println("email = $email")

                    if(!(email == game.player1 || email == game.player2)) throw Exception("Not your game to check")

                    call.respondText(
                        text = Json.encodeToString(game),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    call.respondText(
                        text = Json.encodeToString(mapOf("status" to "Failed to get game status")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.Forbidden
                    )
                }



            } // end of get("/game/{game_id}/status")

            get("/games") {
                println()
                println("get(\"/games\")")

                println("allGames = ${Json.encodeToString(allGames)}")

                val gamesToRegister = mutableListOf<GameRegistrar>()
                allGames.forEach { registerNewGames(gamesToRegister, it.game_id, it.player1!!, it.player2!!, it.size!!) }

                println("gamesToRegister = $gamesToRegister")

                call.respondText(
                    text = Json.encodeToString(gamesToRegister),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            }  // end of get("/games")

        } // end of authenticate("gameAuth")

    }  // end_of_routing {}
}  // end Application.configure()

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
fun initialSetupOfRespondAndGame(gameSetupInfo: GameSetupInfo): Pair<RespondsForGameStep, Game> {
    val respondForGameStep = getRespondForGameStep(
        status = RespondsStatus.NEW_GAME_STARTER.status,
        player1 = gameSetupInfo.player1,
        player2 = gameSetupInfo.player2,
        size = gameSetupInfo.size
    )

    val newGame = newGameInitialSetup(
        GameStatus.NOT_STARTED.status,
        respondForGameStep.player1.toString(),
        respondForGameStep.player2.toString(),
        respondForGameStep.size.toString()
    )
    allGames.add(newGame)

    GAMEID++

    return Pair(respondForGameStep, newGame)
}

fun getRespondForGameStep(status: String, player1: String?, player2: String?, size: String?): RespondsForGameStep {
    var validatedSize = size
    if (size != null) {
        val fieldCoordPair = getGameFieldCoords(size)
        validatedSize = "${fieldCoordPair.first}x${fieldCoordPair.second}"
    }
    return RespondsForGameStep(
        game_id = GAMEID,
        status = status,
        player1 = player1,
        player2 = player2,
        size = validatedSize
    )
}

fun registerNewGames(gamesToRegister: MutableList<GameRegistrar>, game_id: Int, player1: String, player2: String, size: String) {
    gamesToRegister.add(GameRegistrar(game_id, player1, player2, size))
}

fun newGameInitialSetup(gameStatus: String, player1: String, player2: String, size: String): Game {
    // generate the fieldListOfList
    val fieldCoordsPair = getGameFieldCoords(size)
    val x = fieldCoordsPair.first
    val y = fieldCoordsPair.second
    val fieldListOfList = createListOfListField(x, y)

    return Game(
        game_id = GAMEID,
        game_status = gameStatus,
        field = fieldListOfList,
        player1 = player1,
        player2 = player2,
        size = size
    )
}



// ------ END Of Initial Setup Functions---mostly used in get("/post")--------//