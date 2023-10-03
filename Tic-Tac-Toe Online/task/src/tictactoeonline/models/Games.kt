package tictactoeonline.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

import org.jetbrains.exposed.sql.transactions.transaction
import tictactoeonline.plugins.GameStatus


object Games : IntIdTable() {
    val game_id = integer("game_id")
    val gameStatus: Column<String> = varchar("game_status", 20)
    val field: Column<String> = text("field")
    val player1 = reference("player1", Users).nullable()
    val player2 = reference("player2", Users).nullable()
    val size: Column<String> = varchar("size", 10)
    val isPrivate: Column<Boolean> = bool("private")
    val token: Column<String?> = varchar("token", 32).nullable()
}

class GameDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<GameDAO>(Games)

    var game_id by Games.game_id
    var gameStatus by Games.gameStatus
    var field by Games.field
    var player1 by UserDAO optionalReferencedOn Games.player1
    var player2 by UserDAO optionalReferencedOn Games.player2
    var size by Games.size
    var isPrivate by Games.isPrivate
    var token by Games.token
}

class GamesRepository {
    fun create(game_id: Int, field: String, player1: UserDAO?, player2: UserDAO?, size: String, private: Boolean, token: String?) = transaction {
        GameDAO.new {
            this.game_id = game_id
            this.gameStatus = GameStatus.NEW_GAME_STARTED.status
            this.field = field
            this.player1 = player1
            this.player2 = player2
            this.size = size
            this.isPrivate = private
            this.token = token
        }
    }

    fun addPlayer(id: Int, player: UserDAO) = transaction {
        GameDAO.findById(id)?.apply {
            if (this.player1 == null && this.player2 != null) this.player1 = player
            if (this.player1 != null && this.player2 == null) this.player2 = player
        }
    }

    fun updateStatus(id: Int, status: String) = transaction {
        GameDAO.findById(id)?.apply {
            this.gameStatus = status
        }
    }

    fun updateField(id: Int, field: String) = transaction {
        GameDAO.findById(id)?.apply {
            this.field = field
        }
    }

    fun findGameByGameId(game_id: Int) = transaction {
        // choose last one
        var foundGame: GameDAO? = null
        for (game in GameDAO.all()) {
            if (game.game_id == game_id) foundGame = game
        }
        return@transaction foundGame
    }
}


