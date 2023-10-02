import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import tictactoeonline.module
import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.expect.Expectation.expect
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.util.regex.Pattern.compile;
import org.hyperskill.hstest.testing.expect.json.JsonChecker.*;
import java.io.File
import java.nio.file.Paths

@Serializable
data class SigninResponse(val status: String, val token: String)

@Serializable
data class CreateGameResponse(
    val game_id: Int,
    val status: String,
    val player1: String,
    val player2: String,
    val size: String,
    val private: Boolean,
    val token: String
)

class TicTacToeOnlineTest : StageTest<Any>() {

    @DynamicTest
    fun test0_deleteDbFile(): CheckResult {
        val dbFile = File(Paths.get("", "build", "db.mv.db").toAbsolutePath().toString())
        dbFile.delete()
        return CheckResult.correct()
    }

    @DynamicTest
    fun test1_part1(): CheckResult {
        var result: CheckResult = CheckResult.correct();
        try {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Post, "/signup") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(""" { "email":"alex@hyperskill.org", "password":"hs2023"} """)
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /signup")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /signup")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Signed Up"))
                    )
                }

                handleRequest(HttpMethod.Post, "/signup") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(""" { "email":"mira@hyperskill.org", "password":"112233"} """)
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /signup")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /signup")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Signed Up"))
                    )
                }
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest
    fun test1_part2(): CheckResult {
        var result: CheckResult = CheckResult.correct();
        try {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Post, "/signup") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(""" { "email":"alex@hyperskill.org", "password":"1234"} """)
                }.apply {
                    if (response.status() != HttpStatusCode.Forbidden) {
                        result =
                            CheckResult.wrong("Expected status: 403 Forbidden\nFound:${response.status()}\nRoute: /signup")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /signup")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Registration failed"))
                    )
                }

                handleRequest(HttpMethod.Post, "/signin") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(""" { "email":"alex@hyperskill.org", "password":"1234"} """)
                }.apply {
                    if (response.status() != HttpStatusCode.Forbidden) {
                        result =
                            CheckResult.wrong("Expected status: 403 Forbidden\nFound:${response.status()}\nRoute: /signin")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /signin")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Authorization failed"))
                    )
                }

                handleRequest(HttpMethod.Post, "/signin") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(""" { "email":"alex@hyperskill.org", "password":"hs2023"} """)
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /signin")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /signin")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Signed In"))
                            .value("token", isString())
                    )
                    val resp: String = response.content!!
                    val jsonResponse = Json.decodeFromString<SigninResponse>(resp)
                    if (jsonResponse.token != "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8" &&
                        jsonResponse.token != "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.VQIBO0jQ8qW-308raJtSrvqufTEPDWcJyQsfwjnjTLQ"
                    ) {
                        result = CheckResult.wrong(
                            """
                        Invalid login token!
                        Expected eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8 or eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.VQIBO0jQ8qW-308raJtSrvqufTEPDWcJyQsfwjnjTLQ
                        Found: ${jsonResponse.token}
                        Route: /signin
                    """.trimIndent()
                        )
                        return@apply
                    }
                }

                handleRequest(HttpMethod.Post, "/signin") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(""" { "email":"mira@hyperskill.org", "password":"112233"} """)
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /signin")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /signin")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Signed In"))
                            .value("token", isString())
                    )
                    val resp: String = response.content!!
                    val jsonResponse = Json.decodeFromString<SigninResponse>(resp)
                    if (jsonResponse.token != "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.Q5JRRLXBVRbu16BcvcQNUMj_WXrEmFDLPM5QZYA9DFA" &&
                        jsonResponse.token != "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.5_G2rDHUYjJFzrqih0HXGuNTxxQMo6S5A0YFdFD9J8Q"
                    ) {
                        result = CheckResult.wrong(
                            """
                        Invalid login token!
                        Expected eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.Q5JRRLXBVRbu16BcvcQNUMj_WXrEmFDLPM5QZYA9DFA or eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.5_G2rDHUYjJFzrqih0HXGuNTxxQMo6S5A0YFdFD9J8Q
                        Found: ${jsonResponse.token}
                        Route: /signin
                    """.trimIndent()
                        )
                        return@apply
                    }
                }
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest
    fun test2_part1(): CheckResult {
        var result: CheckResult = CheckResult.correct();
        try {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Post, "/game") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.5_G2rDHUYjJFzrqih0HXGuNTxxQMo6S5A0YFdFD9J8Q"
                    )
                    setBody(
                        """
                    {
                        "player1": "mira@hyperskill.org",
                        "player2": "",
                        "size": "4x3",
                        "private": false
                    }
                """.trimIndent()
                    )
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("game_id", isInteger(1))
                            .value("status", compile("New game started"))
                            .value("player1", compile("mira@hyperskill.org"))
                            .value("player2", compile(""))
                            .value("size", compile("4x3"))
                            .value("private", isBoolean(false))
                            .value("token", isString(""))
                    )
                }

                handleRequest(HttpMethod.Post, "/game") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(
                        """
                    {
                        "player1": "",
                        "player2": "alex@hyperskill.org",
                        "size": "3x6",
                        "private": true
                    }
                """.trimIndent()
                    )
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("game_id", isInteger(2))
                            .value("status", compile("New game started"))
                            .value("player1", compile(""))
                            .value("player2", compile("alex@hyperskill.org"))
                            .value("size", compile("3x6"))
                            .value("private", isBoolean(true))
                            .value("token", isString())
                    )
                }

            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest
    fun test2_part2(): CheckResult {
        var result: CheckResult = CheckResult.correct();
        try {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Get, "/games") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /games")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /games")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isArray(2)
                            .item(
                                0,
                                isObject()
                                    .value("game_id", isInteger(1))
                                    .value("player1", compile("mira@hyperskill.org"))
                                    .value("player2", compile(""))
                                    .value("size", compile("4x3"))
                                    .value("private", isBoolean(false))
                            )
                            .item(
                                1,
                                isObject()
                                    .value("game_id", isInteger(2))
                                    .value("player1", compile(""))
                                    .value("player2", compile("alex@hyperskill.org"))
                                    .value("size", compile("3x6"))
                                    .value("private", isBoolean(true))
                            )
                    )
                }

            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest
    fun test3_deleteDbFile(): CheckResult {
        val dbFile = File(Paths.get("", "build", "db.mv.db").toAbsolutePath().toString())
        dbFile.delete()
        return CheckResult.correct()
    }

    @DynamicTest
    fun test4(): CheckResult {
        var result: CheckResult = CheckResult.correct();
        try {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Post, "/game") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.5_G2rDHUYjJFzrqih0HXGuNTxxQMo6S5A0YFdFD9J8Q"
                    )
                    setBody(
                        """
                    {
                        "player1": "mira@hyperskill.org",
                        "player2": "",
                        "size": "4x3",
                        "private": false
                    }
                """.trimIndent()
                    )
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("game_id", isInteger(1))
                            .value("status", compile("New game started"))
                            .value("player1", compile("mira@hyperskill.org"))
                            .value("player2", compile(""))
                            .value("size", compile("4x3"))
                            .value("private", isBoolean(false))
                            .value("token", isString(""))
                    )
                }

                var gameJoinToken: String = ""
                handleRequest(HttpMethod.Post, "/game") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.5_G2rDHUYjJFzrqih0HXGuNTxxQMo6S5A0YFdFD9J8Q"
                    )
                    setBody(
                        """
                    {
                        "player1": "",
                        "player2": "mira@hyperskill.org",
                        "size": "3x6",
                        "private": true
                    }
                """.trimIndent()
                    )
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("game_id", isInteger(2))
                            .value("status", compile("New game started"))
                            .value("player1", compile(""))
                            .value("player2", compile("mira@hyperskill.org"))
                            .value("size", compile("3x6"))
                            .value("private", isBoolean(true))
                            .value("token", isString())
                    )
                    val resp: String = response.content!!
                    val responseJson = Json.decodeFromString<CreateGameResponse>(resp)
                    gameJoinToken = responseJson.token
                }


                handleRequest(HttpMethod.Post, "/game/1/join") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game/1/join")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game/1/join")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Joining the game succeeded"))
                    )
                }

                handleRequest(HttpMethod.Post, "/game/2/join/fr67sl4g5fltwwsgjl4ftyj9t20062ia") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.Forbidden) {
                        result =
                            CheckResult.wrong("Expected status: 403 Forbidden\nFound:${response.status()}\nRoute: /game/2/join/fr67sl4g5fltwwsgjl4ftyj9t20062ia")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game/2/join/fr67sl4g5fltwwsgjl4ftyj9t20062ia")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Joining the game failed"))
                    )
                }

                handleRequest(HttpMethod.Post, "/game/2/join/$gameJoinToken") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game/2/join/$gameJoinToken")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game/2/join/$gameJoinToken")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("status", compile("Joining the game succeeded"))
                    )
                }

                handleRequest(HttpMethod.Get, "/game/1/status") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game/1/status")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game/1/status")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("game_id", isInteger(1))
                            .value("game_status", compile("1st player's move"))
                            .value(
                                "field",
                                isArray(4)
                                    .item(0, isArray(" ", " ", " "))
                                    .item(1, isArray(" ", " ", " "))
                                    .item(2, isArray(" ", " ", " "))
                                    .item(3, isArray(" ", " ", " "))
                            )
                            .value("player1", compile("mira@hyperskill.org"))
                            .value("player2", compile("alex@hyperskill.org"))
                            .value("size", compile("4x3"))
                            .value("private", isBoolean(false))
                            .value("token", isString(""))
                    )
                }

                handleRequest(HttpMethod.Get, "/game/2/status") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /game/2/status")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /game/2/status")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("game_id", isInteger(2))
                            .value("game_status", compile("1st player's move"))
                            .value(
                                "field",
                                isArray(3)
                                    .item(0, isArray(" ", " ", " ", " ", " ", " "))
                                    .item(1, isArray(" ", " ", " ", " ", " ", " "))
                                    .item(2, isArray(" ", " ", " ", " ", " ", " "))
                            )
                            .value("player1", compile("alex@hyperskill.org"))
                            .value("player2", compile("mira@hyperskill.org"))
                            .value("size", compile("3x6"))
                            .value("private", isBoolean(true))
                            .value("token", isString(gameJoinToken))
                    )
                }
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }
}