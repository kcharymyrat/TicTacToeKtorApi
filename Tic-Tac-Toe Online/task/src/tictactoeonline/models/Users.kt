package tictactoeonline.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.jetbrains.exposed.sql.transactions.transaction


object Users : IntIdTable() {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 64)
}


class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(Users)

    var email by Users.email
    var password by Users.password
}


class UsersRepository {
    fun create(email: String, password: String) = transaction {
        UserDAO.new {
            this.email = email
            this.password = password
        }
    }

    fun update(id: Int, newEmail: String?, newPassword: String?) = transaction {
        UserDAO.findById(id)?.apply {
            if (newEmail != null) this.email = newEmail
            if (newPassword != null) this.password = newPassword
        }
    }

    fun delete(id: Int) = transaction {
        UserDAO.findById(id)?.delete() != null
    }

    fun find(id: Int) = transaction {
        UserDAO.findById(id)
        // User.find { Users.id eq id }.singleOrNull() - same as above
    }

    fun all() = transaction {
        UserDAO.all().toList()
    }

    fun updateByEmail(newEmail: String) = transaction {
        UserDAO.find { Users.email eq newEmail }.singleOrNull()?.apply {
            this.email  = newEmail
        }
    }

    fun deleteByEmail(email: String) = transaction {
        UserDAO.find { Users.email eq email }.singleOrNull()?.delete() != null
    }

    fun findByEmail(email: String) = transaction {
        UserDAO.find { Users.email eq email }.singleOrNull()
    }
}
