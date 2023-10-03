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
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import tictactoeonline.*
import tictactoeonline.models.*


@Serializable
data class UserDeserializer(val email: String, val password: String)

@Serializable
data class NewGameSetupDeserializer(val player1: String, val player2: String, val size: String, val private: Boolean)

@Serializable
data class GamePathSuccessRespondSerializer(
    var game_id: Int,
    var status: String,
    var player1: String? = null,
    var player2: String? = null,
    var size: String? = null,
    var private: Boolean,
    var token: String?
)


@Serializable
data class GameRegistrar(
    var game_id: Int,
    var player1: String,
    var player2: String,
    var size: String,
    var private: Boolean,
)

@Serializable
data class GameStatusRespond(
    var game_id: Int,
    var game_status: String,
    var field: MutableList<MutableList<String>>,
    var player1: String,
    var player2: String,
    var size: String,
    var private: Boolean,
    var token: String,
)


@Serializable
class GamesPaginatedResponse(
    var totalPages: Long,
    var totalElements: Long,
    var page: Int,
    var size: Int,
    var numberOfElements: Int,
    var content: MutableList<GameRegistrar>
)

@Serializable
data class MyGameRegistrar(
    var game_id: Int,
    var game_status: String,
    var field: MutableList<MutableList<String>>,
    var player1: String,
    var player2: String,
    var size: String,
    var private: Boolean,
    var token: String
)

@Serializable
class MyGamesPaginatedResponse(
    var totalPages: Long,
    var totalElements: Long,
    var page: Int,
    var size: Int,
    var numberOfElements: Int,
    var content: MutableList<MyGameRegistrar>
)


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
    NEW_GAME_STARTED("New game started"),
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
data class RespondsForGameStep(
    var game_id: Int,
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

// To store all successfully registered emails
@Serializable
val registeredEmails = mutableMapOf<String, String>()

@Serializable
var allGames = mutableListOf<Game>()

var GAMEID = 1

fun validateJsonUser(userJson: String): UserDeserializer? {
    val user = Json.decodeFromString<UserDeserializer>(userJson)
    print("userSerializable = $user")

    // check if email and passwords are blank
    if (user.email.isBlank() || user.password.isBlank()) {
        return null
    }
    return user
}

fun createJWTokenForGame(email: String): String? {
    return JWT.create()
        .withClaim("email", email)
        .sign(Algorithm.HMAC256(secret))
}

fun generateRandomToken(): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..32)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getEmailFromJWT(call: ApplicationCall): String {
    val principal = call.principal<JWTPrincipal>()
    val userEmail = principal!!.payload.getClaim("email").asString()
    val authorizationHeader = call.request.header("Authorization")
    val jwtToken = authorizationHeader?.removePrefix("Bearer ")
    println("jwtToken = $jwtToken")
    println("userEmail from Token = $userEmail")
    return userEmail
}

val userRepository = UsersRepository()

fun Application.configureRouting() {
    val usersRepository = UsersRepository()
    val gameRepository = GamesRepository()
    val gameRoomsRepository = GameRoomsRepository()

    routing {

        post("/signup") {
            println()
            println("post(\"/signup\")")

            val userJson = call.receiveText()
            println("userJson = $userJson")

            try {
                val deserializedUser = validateJsonUser(userJson) ?: throw Exception("Wrong Json input")
                println("deserializedUser = $deserializedUser")

                // Add user to DB
                val userDAO = usersRepository.create(deserializedUser.email, deserializedUser.password)
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

            val userJson = call.receiveText()
            println("userJson = $userJson")

            transaction {
                println("allUser = ${UserDAO.all().map { it.email }}")
            }

            try {
                val deserializedUser = validateJsonUser(userJson) ?: throw Exception("Wrong Json input")
                println("deserializedUser = $deserializedUser")

                val userDAO = usersRepository.findByEmail(deserializedUser.email)
                println("userDAO = ${userDAO?.email} ${userDAO?.password}")
                if (userDAO == null) throw Exception("No such user exists")
                if (userDAO.password != deserializedUser.password) throw Exception("No such user exists")

                val token = createJWTokenForGame(deserializedUser.email) ?: throw Exception("Unable to create JWT token")

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
                    transaction {
                        println("allUser = ${UserDAO.all().map { it.email }}")
                    }

                    val userEmail = getEmailFromJWT(call)

                    val gameSetupJson = call.receiveText()
                    println("call.receiveText() = $gameSetupJson, isBlank = ${gameSetupJson.isBlank()}")

                    val newGameSetup = Json.decodeFromString<NewGameSetupDeserializer>(gameSetupJson)
                    println("newGameSetup = $newGameSetup")

                    println(!(userEmail != newGameSetup.player1 || userEmail != newGameSetup.player2))
                    if (!(userEmail != newGameSetup.player1 || userEmail != newGameSetup.player2)) throw Exception("Wrong Game setup")
                    println("After")

                    // Create GameDB
                    val player1 = if (newGameSetup.player1 == userEmail) userEmail else null
                    val player2 = if (newGameSetup.player2 == userEmail) userEmail else null
                    val size = newGameSetup.size
                    val private = newGameSetup.private
                    val field = Json.encodeToString(sizeToMListOfMList(size))
                    val ranToken = if (private) generateRandomToken() else null

                    val gameDAO = gameRepository.create(GAMEID++, field, player1, player2, size, private, ranToken)

                    println("gameDAO = $gameDAO")

                    val successGamePathRespond = GamePathSuccessRespondSerializer(
                            game_id = transaction { gameDAO.game_id },
                            status = transaction { gameDAO.gameStatus },
                            player1 = transaction { gameDAO.player1 } ?: newGameSetup.player1,
                            player2 = transaction { gameDAO.player2 } ?: newGameSetup.player2,
                            size = transaction { gameDAO.size },
                            private = transaction { gameDAO.isPrivate },
                            token =  transaction { gameDAO.token ?: ""},
                        )

                    println("try => successGamePathRespond = ${Json.encodeToString(successGamePathRespond)}")

                    transaction {
                        val allGames = GameDAO.all().map { Pair(it.id.value, it.game_id) to Pair(it.player1, it.player2) }
                        println("allGames = $allGames")
                    }

                    call.respondText(
                        text = Json.encodeToString(successGamePathRespond),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )

                    println("NOW what")
                } catch (e: Exception) {
                    println("catch => Creating a game failed")
                    println("e = $e")
                    println("${e.message}, ${e.cause}, ${e.localizedMessage}")
                    call.respondText(
                        text = Json.encodeToString(mapOf("status" to "Creating a game failed")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.Forbidden
                    )
                }

            }  // end of post("/game")

            post("/game/{game_id}/join/{token?}") {
                println()
                val game_id = call.parameters["game_id"]?.toInt()
                val token = call.parameters["token"]
                println("post(\"/game/$game_id/join/$token\")")

                try {
                    if (game_id == null) throw Exception("No such game_id was inserted")

//                    val game = allGames.find { it.game_id == id } ?: throw Exception("No game with game_id = $id")
                    val game = gameRepository.findGameByGameId(game_id) ?: throw Exception("No game with game_id = $game_id")
                    println("Game: game_id=${game.game_id}, player1=${transaction { game.player1}}, player2=${ transaction { game.player2}}, token=${game.token}")

//                    if (game.isPrivate && token == null) throw Exception("Game is private, no token provided!")
                     if (game.token != token) throw Exception("Game is private, no token provided!")

                    val player1 = transaction { game.player1 }
                    val player2 = transaction { game.player2 }

                    if(!(player1 == null || player2 == null)) throw Exception("Not your game to join")

                    val principal = call.principal<JWTPrincipal>()
                    val email = principal!!.payload.getClaim("email").asString()
                    println("email from token = $email")

//                    val userDAO = usersRepository.findByEmail(email) ?: throw Exception("No such user game to join")

                    val playerAdded = gameRepository.addPlayer(transaction { game.id.value }, email) ?: throw Exception("Game was not added!")
                    println("playerAdded : game_id=${playerAdded.game_id}, player1=${transaction { playerAdded.player1}}, player2=${ transaction { playerAdded.player2}}, token=${playerAdded.token}")

                    val statusUpdated = gameRepository.updateStatus(transaction { game.id.value }, GameStatus.FIRST_PLAYER_MOVE.status) ?: throw Exception("Game status was not able to update 'status'")
                    println("statusUpdated : game_id=${statusUpdated.game_id}, player1=${transaction { statusUpdated.player1}}, player2=${ transaction { statusUpdated.player2}}, token=${statusUpdated.token}")

                    call.respondText(
                        text = Json.encodeToString(mapOf("status" to "Joining the game succeeded")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    println("e= ${e.message}")
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
                val game_id = call.parameters["game_id"]?.toInt()
                println("get(\"/game/$game_id/status\")")
                try {
                    if (game_id == null) throw Exception("No such game_id was inserted")

                    // val game = allGames.find { it.game_id == game_id } ?: throw Exception("No game with game_id = $game_id")

                    val gameDAO = gameRepository.findGameByGameId(game_id) ?: throw Exception("No game with id=$game_id")
                    val player1 = transaction { gameDAO.player1 }
                    val player2 = transaction { gameDAO.player2 }

                    val principal = call.principal<JWTPrincipal>()
                    val email = principal!!.payload.getClaim("email").asString()
                    println("email = $email")

                    println("-------------------------------------------")
                    println("Game: game_id=${gameDAO.game_id}, player1=${transaction { gameDAO.player1 }}, player2=${ transaction { gameDAO.player2 }}, token=${gameDAO.token}}")
                    println("-------------------------------------------")

                    if(!(email == player1 || email == player2)) throw Exception("Not your game to check")

                    val gameStatusRespond = GameStatusRespond(
                        game_id = gameDAO.game_id,
                        game_status = gameDAO.gameStatus,
                        field = Json.decodeFromString(gameDAO.field),
                        player1 = player1 ?: "",
                        player2 = player2 ?: "",
                        size = gameDAO.size,
                        private = gameDAO.isPrivate,
                        token = gameDAO.token ?: ""
                    )

                    call.respondText(
                        text = Json.encodeToString(gameStatusRespond),
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

//                val registeredGames = mutableListOf<GameRegistrar>()
//                transaction {
//                    val allGames = GameDAO.all().map { it }
//                    for (game in allGames) {
//                        registeredGames.add(
//                            GameRegistrar(
//                                game_id = game.game_id,
//                                player1 = game.player1?.email ?: "",
//                                player2 = game.player2?.email ?: "",
//                                size =game.size,
//                                private = game.isPrivate
//                            )
//                        )
//                    }
//                }

                val pageParam = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val pageSize = 10
                println("pageParam=$pageParam, pageSize=$pageSize")

                try {
                    val totalGamesCount = transaction { GameDAO.all().count() }
                    val totalPages = (totalGamesCount + pageSize - 1) / pageSize
                    println("totalGamesCount=$totalGamesCount, totalPages=$totalPages")


                    val registeredGames = mutableListOf<GameRegistrar>()

                    transaction {

                        val games = GameDAO.all().limit(pageSize, (pageParam * pageSize).toLong())

                        for (game in games) {
                            registeredGames.add(
                                GameRegistrar(
                                    game_id = game.game_id,
                                    player1 = game.player1 ?: "",
                                    player2 = game.player2 ?: "",
                                    size = game.size,
                                    private = game.isPrivate
                                )
                            )
                        }
                    }



                    val response = GamesPaginatedResponse(
                        totalPages = totalPages,
                        totalElements = totalGamesCount,
                        page = pageParam,
                        size = pageSize,
                        numberOfElements = registeredGames.size,
                        content = registeredGames
                    )

//                val response = mapOf(
//                    "totalPages" to totalPages,
//                    "totalElements" to totalGamesCount,
//                    "page" to pageParam,
//                    "size" to pageSize,
//                    "numberOfElements" to registeredGames.size,
//                    "content" to registeredGames
//                )

                    println("registeredGames = $registeredGames")
                    println("response = ${Json.encodeToString(response)}")

                    call.respondText(
                        text = Json.encodeToString(response),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    println("e = ${e.message}")
                }
            }  // end of get("/games")

            get("/games/my") {
                println()
                println("get(\"/games/my\")")

//                val registeredGames = mutableListOf<GameRegistrar>()
//                transaction {
//                    val allGames = GameDAO.all().map { it }
//                    for (game in allGames) {
//                        registeredGames.add(
//                            GameRegistrar(
//                                game_id = game.game_id,
//                                player1 = game.player1?.email ?: "",
//                                player2 = game.player2?.email ?: "",
//                                size =game.size,
//                                private = game.isPrivate
//                            )
//                        )
//                    }
//                }

                val pageParam = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val pageSize = 10
                println("pageParam=$pageParam, pageSize=$pageSize")

                try {
                    var totalGamesCount = transaction { GameDAO.all().count() }
                    var totalPages = (totalGamesCount + pageSize - 1) / pageSize
                    println("totalGamesCount=$totalGamesCount, totalPages=$totalPages")

                    val emailJWT = getEmailFromJWT(call)


                    val registeredGames = mutableListOf<MyGameRegistrar>()

                    transaction {
                        val myGames = GameDAO.find { (Games.player1 eq emailJWT) or (Games.player2 eq emailJWT) }
                        totalGamesCount = myGames.count()
                        totalPages = (totalGamesCount + pageSize - 1) / pageSize

                        val games = myGames.limit(pageSize, (pageParam * pageSize).toLong())



                        for (game in games) {
                            val player1 = game.player1 ?: ""
                            val player2 = game.player2 ?: ""
                            val status = if (player1.isNotBlank() && player2.isNotBlank()) GameStatus.FIRST_PLAYER_MOVE.status else GameStatus.NOT_STARTED.status
                            registeredGames.add(
                                MyGameRegistrar(
                                    game_id = game.game_id,
                                    game_status = status,
                                    field = Json.decodeFromString(game.field),
                                    player1 = game.player1 ?: "",
                                    player2 = game.player2 ?: "",
                                    size = game.size,
                                    private = game.isPrivate,
                                    token = game.token ?: ""
                                )
                            )
                        }
                    }



                    val response = MyGamesPaginatedResponse(
                        totalPages = totalPages,
                        totalElements = totalGamesCount,
                        page = pageParam,
                        size = pageSize,
                        numberOfElements = registeredGames.size,
                        content = registeredGames
                    )

//                val response = mapOf(
//                    "totalPages" to totalPages,
//                    "totalElements" to totalGamesCount,
//                    "page" to pageParam,
//                    "size" to pageSize,
//                    "numberOfElements" to registeredGames.size,
//                    "content" to registeredGames
//                )

                    println("registeredGames = $registeredGames")
                    println("response = ${Json.encodeToString(response)}")

                    call.respondText(
                        text = Json.encodeToString(response),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    println("e = ${e.message}")
                }
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
fun initialSetupOfRespondAndGame(newGameSetupDeserializer: NewGameSetupDeserializer): Pair<RespondsForGameStep, Game> {
    val respondForGameStep = getRespondForGameStep(
        status = RespondsStatus.NEW_GAME_STARTER.status,
        player1 = newGameSetupDeserializer.player1,
        player2 = newGameSetupDeserializer.player2,
        size = newGameSetupDeserializer.size
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

//fun registerNewGames(gamesToRegister: MutableList<GameRegistrar>, game_id: Int, player1: String, player2: String, size: String) {
//    gamesToRegister.add(GameRegistrar(game_id, player1, player2, size))
//}

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



//                    var gameDAO = transaction {
//                        val player1Json = if (newGameSetup.player1 == "") null else newGameSetup.player1
//                        val player2Json = if (newGameSetup.player2 == "") null else newGameSetup.player2
//                        val sizeJson = newGameSetup.size
//                        val privateJson = newGameSetup.private
//                        for (game in GameDAO.all()) {
//                            val player1 = game.player1?.email
//                            val player2 = game.player2?.email
//                            val size = game.size
//                            val private = game.isPrivate
//                            if (player1Json == player1 && player2Json == player2 && sizeJson == size && privateJson == private) {
//                                return@transaction game
//                            }
//                        }
//                        return@transaction null
//                    }
//
//                    println("gameDAO = $gameDAO")