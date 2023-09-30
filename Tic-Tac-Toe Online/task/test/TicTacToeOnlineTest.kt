import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.exception.outcomes.WrongAnswer
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.TestedProgram

class TicTacToeTest : StageTest<Any>() {
    @DynamicTest
    fun test1_1(): CheckResult {
        try {
            val pr = TestedProgram()

            var output = pr.start()
            output = pr.execute("")
            var cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("First *player's *name: *Player1 *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("Expected:\nFirst player's name: Player1\nFound:\n$output")

            output = pr.execute("")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Second *player's *name: *Player2 *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("Expected:\nSecond player's name: Player2\nFound:\n$output")

            output = pr.execute("")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Field *size: *3x3 *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("Expected:\nField size: 3x3\nFound:\n$output")

            if( !cleanOutput.contains("""
            |---|---|---|-y
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |
            x
        """.trimIndent()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|-y
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |
            x
            """.trimIndent()+"\nFound:\n$output")

            return CheckResult.correct()

        } catch(e: Exception) {
            throw WrongAnswer(e.message)
        }
    }
    @DynamicTest
    fun test1_2(): CheckResult {
        try {
            val pr = TestedProgram()

            var output = pr.start()
            output = pr.execute("Artem")
            var cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("First *player's *name: *Artem *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("Expected:\nFirst player's name: Artem\nFound:\n$output")

            output = pr.execute("Lollipop")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Second *player's *name: *Lollipop *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("Expected:\nSecond player's name: Lollipop\nFound:\n$output")

            output = pr.execute("2x5")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Field *size: *2x5 *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("Expected:\nField size: 2x5\nFound:\n$output")

            if( !cleanOutput.contains("""
            |---|---|---|---|---|-y
            |   |   |   |   |   |
            |---|---|---|---|---|
            |   |   |   |   |   |
            |---|---|---|---|---|
            |
            x
        """.trimIndent()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|---|---|-y
            |   |   |   |   |   |
            |---|---|---|---|---|
            |   |   |   |   |   |
            |---|---|---|---|---|
            |
            x
            """.trimIndent()+"\nFound:\n$output")

            return CheckResult.correct()
        } catch (e: Exception) {
            throw WrongAnswer(e.message)
        }
    }
    @DynamicTest
    fun test1_3(): CheckResult {
        try {
            val pr = TestedProgram()

            var output = pr.start()
            output = pr.execute("")
            var cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("First *player's *name: *Player1 *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("The default name for one of the players is wrong")

            output = pr.execute("Mary")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Second *player's *name: *Mary *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("The program incorrectly set the name of one of the players")

            output = pr.execute("2x2")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Field *size: *3x3 *\\n".toRegex(setOf(RegexOption.IGNORE_CASE))))
                throw WrongAnswer("The program has set the wrong size for the playing field!")

            if( !cleanOutput.contains("""
            |---|---|---|-y
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |
            x
        """.trimIndent().lowercase()))
                throw WrongAnswer("The program incorrectly displayed an empty playing field before the game started")

            return CheckResult.correct()
        } catch (e: Exception) {
            throw WrongAnswer(e.message)
        }

    }

    @DynamicTest
    fun test2_1(): CheckResult {
        try {
            val pr = TestedProgram()

            var output = pr.start()
            output = pr.execute("")
            output = pr.execute("")
            output = pr.execute("")

            output = pr.execute("(2,1)")
            var cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("""
            |---|---|---|
            |   |   |   |
            |---|---|---|
            | X |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
        """.trimIndent().lowercase()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|
            |   |   |   |
            |---|---|---|
            | X |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            """.trimIndent() + "\nFound:\n$output")

            output = pr.execute("(2,1)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Wrong move entered".lowercase()))
                throw WrongAnswer("Expected:\nWrong move entered\nFound:\n$output")

            output = pr.execute("(,1)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Wrong move entered".lowercase()))
                throw WrongAnswer("Expected:\nWrong move entered\nFound:\n$output")

            output = pr.execute("sdfsdfs")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Wrong move entered".lowercase()))
                throw WrongAnswer("Expected:\nWrong move entered\nFound:\n$output")

            output = pr.execute("(100,2)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Wrong move entered".lowercase()))
                throw WrongAnswer("Expected:\nWrong move entered\nFound:\n$output")

            output = pr.execute("(2,2)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("""
            |---|---|---|
            |   |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
        """.trimIndent().lowercase()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|
            |   |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            """.trimIndent() + "\nFound:\n$output")

            output = pr.execute("(1,1)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("""
            |---|---|---|
            | X |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
        """.trimIndent().lowercase()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|
            | X |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            """.trimIndent() + "\nFound:\n$output")

            output = pr.execute("(3,2)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("""
            |---|---|---|
            | X |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            |   | O |   |
            |---|---|---|
        """.trimIndent().lowercase()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|
            | X |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            |   | O |   |
            |---|---|---|
            """.trimIndent() + "\nFound:\n$output")

            output = pr.execute("(3,1)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("""
            |---|---|---|
            | X |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
        """.trimIndent().lowercase()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|
            | X |   |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            | X | O |   |
            |---|---|---|
            """.trimIndent() + "\nFound:\n$output")

            if( !cleanOutput.contains("Player1 wins!".lowercase()))
                throw WrongAnswer("Expected:\nPlayer1 wins!\nFound:\n$output")

            return CheckResult.correct()
        } catch (e: Exception) {
            throw WrongAnswer(e.message)
        }
    }
    @DynamicTest
    fun test2_2(): CheckResult {
        try {
            val pr = TestedProgram()

            var output = pr.start()
            output = pr.execute("Biba")
            output = pr.execute("Boba")

            output = pr.execute("1x10")
            var cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Field size: 1x10".lowercase()))
                throw WrongAnswer("Expected:\nField size: 1x10\nFound:\n$output")

            output = pr.execute("(1,1)")
            output = pr.execute("(1,10)")
            output = pr.execute("(1,2)")
            output = pr.execute("(1,9)")
            output = pr.execute("(1,4)")

            output = pr.execute("(1,8)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("""
            |---|---|---|---|---|---|---|---|---|---|
            | X | X |   | X |   |   |   | O | O | O |
            |---|---|---|---|---|---|---|---|---|---|
        """.trimIndent().lowercase()))
                throw WrongAnswer("Expected:\n"+"""
            |---|---|---|---|---|---|---|---|---|---|
            | X | X |   | X |   |   |   | O | O | O |
            |---|---|---|---|---|---|---|---|---|---|
            """.trimIndent() + "\nFound:\n$output")

            if( !cleanOutput.contains("Boba wins!".lowercase()))
                throw WrongAnswer("Expected:\nBoba wins!\nFound:\n$output")

            return CheckResult.correct()
        } catch (e: Exception) {
            throw WrongAnswer(e.message)
        }

    }
    @DynamicTest
    fun test2_3(): CheckResult {
        try {
            val pr = TestedProgram()

            var output = pr.start()
            output = pr.execute("Max")
            output = pr.execute("Emily")

            output = pr.execute("8x1")
            var cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("Field size: 8x1".lowercase()))
                throw WrongAnswer("Expected:\nField size: 8x1\nFound:\n$output")

            output = pr.execute("(1,1)")
            output = pr.execute("(2,1)")
            output = pr.execute("(3,1)")
            output = pr.execute("(4,1)")
            output = pr.execute("(5,1)")
            output = pr.execute("(6,1)")
            output = pr.execute("(7,1)")

            output = pr.execute("(8,1)")
            cleanOutput = output.lowercase().trim()
            if( !cleanOutput.contains("""
            |---|
            | X |
            |---|
            | O |
            |---|
            | X |
            |---|
            | O |
            |---|
            | X |
            |---|
            | O |
            |---|
            | X |
            |---|
            | O |
            |---|
        """.trimIndent().lowercase()))
                throw WrongAnswer("Expected:\n"+"""
            |---|
            | X |
            |---|
            | O |
            |---|
            | X |
            |---|
            | O |
            |---|
            | X |
            |---|
            | O |
            |---|
            | X |
            |---|
            | O |
            |---|
            """.trimIndent() + "\nFound:\n$output")

            if( !cleanOutput.contains("Draw!".lowercase()))
                throw WrongAnswer("Expected:\nDraw!\nFound:\n$output")

            return CheckResult.correct()
        } catch (e: Exception) {
            throw WrongAnswer(e.message)
        }
    }
}