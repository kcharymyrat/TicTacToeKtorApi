package tictactoeonline.models

import org.jetbrains.exposed.sql.*

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.jetbrains.exposed.sql.transactions.transaction


object Users : IntIdTable() {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 64)
}


class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var email by Users.email
    var password by Users.password
}


class UserRepository {
    fun create(email: String, password: String) = transaction {
        User.new {
            this.email = email
            this.password = password
        }
    }

    fun update(id: Int, newEmail: String?, newPassword: String?) = transaction {
        User.findById(id)?.apply {
            if (newEmail != null) this.email = newEmail
            if (newPassword != null) this.password = newPassword
        }
    }

    fun delete(id: Int) = transaction {
        User.findById(id)?.delete() != null
    }

    fun find(id: Int) = transaction {
        User.findById(id)
        // User.find { Users.id eq id }.singleOrNull() - same as above
    }

    fun all() = transaction {
        User.all().toList()
    }

    fun updateByEmail(newEmail: String) = transaction {
        User.find { Users.email eq newEmail }.singleOrNull()?.apply {
            this.email  = newEmail
        }
    }

    fun deleteByEmail(email: String) = transaction {
        User.find { Users.email eq email }.singleOrNull()?.delete() != null
    }

    fun findByEmail(email: String) = transaction {
        User.find { Users.email eq email }.singleOrNull()
    }
}
