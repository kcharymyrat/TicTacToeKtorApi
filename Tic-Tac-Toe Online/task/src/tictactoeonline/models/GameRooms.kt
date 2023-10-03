package tictactoeonline.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction


object GameRooms : IntIdTable() {
    val gameId = reference("game_id", Games)
    val creator = reference("creator", Users)
}

class GameRoomDAO(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<GameRoomDAO>(GameRooms)

    var gameId by GameDAO referencedOn GameRooms.gameId
    var creator by UserDAO referencedOn Users.email

}

class GameRoomsRepository {
    fun create(id: GameDAO, creator: UserDAO) = transaction {
        GameRoomDAO.new {
            this.gameId = id
            this.creator = creator
        }
    }
}