import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.expect.Expectation.expect
import java.util.regex.Pattern.compile;
import org.hyperskill.hstest.testing.expect.json.JsonChecker.*;
import java.io.File
import java.nio.file.Paths

class TicTacToeOnlineTest : StageTest<Any>() {

    private val testEnv = createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
    }

    @DynamicTest
    fun test0_deleteDbFile(): CheckResult {
        val dbFile = File(Paths.get("", "build", "db.mv.db").toAbsolutePath().toString())
        dbFile.delete()
        return CheckResult.correct()
    }

    @DynamicTest
    fun test1(): CheckResult {
        var result: CheckResult = CheckResult.correct();
        try {
            withApplication(testEnv) {
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
                            .value("game_id", isInteger(2))
                            .value("status", compile("New game started"))
                            .value("player1", compile(""))
                            .value("player2", compile("alex@hyperskill.org"))
                            .value("size", compile("3x6"))
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
                        "size": "11x6",
                        "private": false
                    }
                """.trimIndent()
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
                        "size": "12x6",
                        "private": false
                    }
                """.trimIndent()
                    )
                }

                handleRequest(HttpMethod.Get, "/games?page=0") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /games?page=0")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /games?page=0")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("totalPages", isInteger(2))
                            .value("totalElements", isInteger(12))
                            .value("page", isInteger(0))
                            .value("size", isInteger(10))
                            .value("numberOfElements", isInteger(10))
                            .value(
                                "content",
                                isArray(10)
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
                                            .value("private", isBoolean(false))
                                    )
                                    .item(
                                        2,
                                        isObject()
                                            .value("game_id", isInteger(3))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                                    .item(
                                        3,
                                        isObject()
                                            .value("game_id", isInteger(4))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                                    .item(
                                        4,
                                        isObject()
                                            .value("game_id", isInteger(5))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                                    .item(
                                        5,
                                        isObject()
                                            .value("game_id", isInteger(6))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                                    .item(
                                        6,
                                        isObject()
                                            .value("game_id", isInteger(7))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                                    .item(
                                        7,
                                        isObject()
                                            .value("game_id", isInteger(8))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                                    .item(
                                        8,
                                        isObject()
                                            .value("game_id", isInteger(9))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                                    .item(
                                        9,
                                        isObject()
                                            .value("game_id", isInteger(10))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(true))
                                    )
                            )
                    )
                }

                handleRequest(HttpMethod.Get, "/games?page=1") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /games?page=1")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /games?page=1")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("totalPages", isInteger(2))
                            .value("totalElements", isInteger(12))
                            .value("page", isInteger(1))
                            .value("size", isInteger(10))
                            .value("numberOfElements", isInteger(2))
                            .value(
                                "content",
                                isArray(2)
                                    .item(
                                        0,
                                        isObject()
                                            .value("game_id", isInteger(11))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("11x6"))
                                            .value("private", isBoolean(false))
                                    )
                                    .item(
                                        1,
                                        isObject()
                                            .value("game_id", isInteger(12))
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("12x6"))
                                            .value("private", isBoolean(false))
                                    )
                            )
                    )
                }

                handleRequest(HttpMethod.Get, "/games/my?page=1") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFsZXhAaHlwZXJza2lsbC5vcmcifQ.v1j3WkYqH1zb7vO6D7ylINhB47yp1HFrrmjYT8vwPO8"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /games/my?page=1")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /games/my?page=1")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("totalPages", isInteger(2))
                            .value("totalElements", isInteger(11))
                            .value("page", isInteger(1))
                            .value("size", isInteger(10))
                            .value("numberOfElements", isInteger(1))
                            .value(
                                "content",
                                isArray(1)
                                    .item(
                                        0,
                                        isObject()
                                            .value("game_id", isInteger(12))
                                            .value("game_status", isString("game not started"))
                                            .value(
                                                "field",
                                                isArray(12)
                                                    .item(0, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(1, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(2, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(3, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(4, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(5, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(6, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(7, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(8, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(9, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(10, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(11, isArray(" ", " ", " ", " ", " ", " "))
                                            )
                                            .value("player1", compile(""))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("12x6"))
                                            .value("private", isBoolean(false))
                                            .value("token", isString(""))
                                    )
                            )
                    )
                }

                handleRequest(HttpMethod.Post, "/game/2/join") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.5_G2rDHUYjJFzrqih0HXGuNTxxQMo6S5A0YFdFD9J8Q"
                    )
                    setBody(" { } ")
                }

                handleRequest(HttpMethod.Get, "/games/my?page=0") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pcmFAaHlwZXJza2lsbC5vcmcifQ.5_G2rDHUYjJFzrqih0HXGuNTxxQMo6S5A0YFdFD9J8Q"
                    )
                    setBody(" { } ")
                }.apply {
                    if (response.status() != HttpStatusCode.OK) {
                        result =
                            CheckResult.wrong("Expected status: 200 OK\nFound:${response.status()}\nRoute: /games/my?page=0")
                        return@apply
                    }
                    if (response.content.isNullOrBlank()) {
                        result = CheckResult.wrong("Empty response!\nRoute: /games/my?page=0")
                        return@apply
                    }
                    expect(response.content).asJson().check(
                        isObject()
                            .value("totalPages", isInteger(1))
                            .value("totalElements", isInteger(2))
                            .value("page", isInteger(0))
                            .value("size", isInteger(10))
                            .value("numberOfElements", isInteger(2))
                            .value(
                                "content",
                                isArray(2)
                                    .item(
                                        0,
                                        isObject()
                                            .value("game_id", isInteger(1))
                                            .value("game_status", isString("game not started"))
                                            .value(
                                                "field",
                                                isArray(4)
                                                    .item(0, isArray(" ", " ", " "))
                                                    .item(1, isArray(" ", " ", " "))
                                                    .item(2, isArray(" ", " ", " "))
                                                    .item(3, isArray(" ", " ", " "))
                                            )
                                            .value("player1", compile("mira@hyperskill.org"))
                                            .value("player2", compile(""))
                                            .value("size", compile("4x3"))
                                            .value("private", isBoolean(false))
                                            .value("token", isString(""))
                                    )
                                    .item(
                                        1,
                                        isObject()
                                            .value("game_id", isInteger(2))
                                            .value("game_status", isString("1st player's move"))
                                            .value(
                                                "field",
                                                isArray(3)
                                                    .item(0, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(1, isArray(" ", " ", " ", " ", " ", " "))
                                                    .item(2, isArray(" ", " ", " ", " ", " ", " "))
                                            )
                                            .value("player1", compile("mira@hyperskill.org"))
                                            .value("player2", compile("alex@hyperskill.org"))
                                            .value("size", compile("3x6"))
                                            .value("private", isBoolean(false))
                                            .value("token", isString(""))
                                    )
                            )
                    )
                }

            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }


}