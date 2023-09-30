package tictactoeonline

import java.util.MissingFormatArgumentException
import kotlin.system.exitProcess

interface Messaging {
    val msg: String
    fun show(vararg strings: String) {
        try {
            println(this.msg.format(*strings))
        } catch(E: MissingFormatArgumentException) {
            println("$this: Invalid number of arguments.")
            exitProcess(0)
        }
    }
}

enum class Console(override val msg: String): Messaging {
    FirstPlayer("\"Enter the first player's name (Player1 by default)\""),
    FirstPlayerName("First player's name: %s"),
    SecondPlayer("\"Enter the second player's name (Player2 by default)\""),
    SecondPlayerName("Second player's name: %s"),
    FieldSize("Enter the field size (3x3 by default)\"")
}