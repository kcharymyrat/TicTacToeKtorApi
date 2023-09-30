package tictactoeonline

fun main() {

    val bothPlayers = getBothPlayerNames()
    val player1 = bothPlayers.first
    val player2 = bothPlayers.second

    println("Enter the field size (3x3 by default)")
    val inputSize = readln().trim()
    val fieldCoords = getGameFieldCoords(inputSize)
    val x = fieldCoords.first
    val y = fieldCoords.second
    println("Field size: ${x}x${y}")
    val listOfListField = createListOfListField(x, y)
//    for (e in listOfListField) println(e)
    prettyPrintField(listOfListField)

    var player = player1
    while (true) {
        println("Enter $player's move as (x,y)")
        val symbol = if (player == player1) "X" else "O"
        val moveString = readln().trim()
        val isCorrectMove = validateMoveCoords(moveString, x, y, listOfListField)
        if (!isCorrectMove) {
            println("Wrong move entered")
            continue
        } else {
            val moveCoordsPair = getMoveCoords(moveString)
            insertMove(moveCoordsPair, listOfListField, symbol)
            prettyPrintField(listOfListField, false)
        }
        println()
        player = if (player == player1) player2 else player1
    }



}

fun insertMove(moveCoords: Pair<Int, Int>, field: MutableList<MutableList<String>>, symbol: String) {
    val x = moveCoords.first
    val y = moveCoords.second
    field[x-1][y-1] = symbol
}


fun getMoveCoords(moveCoords: String): Pair<Int, Int> {
    val coords = moveCoords.trim().removeSurrounding("(", ")")
        .split(",")
        .map {it.trim().toInt()}

    return Pair(coords[0], coords[1])
}

fun validateMoveCoords(moveCoords: String, x: Int, y: Int, field: MutableList<MutableList<String>>): Boolean {

    if(!syntaxCheckForCoords(moveCoords)) return false

    val moveCoordsPair = getMoveCoords(moveCoords)

    val xCoord = moveCoordsPair.first
    val yCoord = moveCoordsPair.second

    if (!(xCoord in 1..x && yCoord in 1..y)) return false

    val cell = field[xCoord - 1][yCoord - 1]
    return cell.isBlank()
}

fun syntaxCheckForCoords(moveCoords: String): Boolean {
    try {
        val coords = moveCoords.trim().removeSurrounding("(", ")")
            .split(",")
            .map {it.trim().toInt()}
        if (coords.size != 2) {
            return false
        }
        return true
    } catch (e: Exception) {
        return false
    }
}


fun prettyPrintField(field: MutableList<MutableList<String>>, isEmpty: Boolean = true) {
    val numOfRows = field.size
    val numOfCols = field[0].size

    for (i in 0..numOfRows) {
        if (i > 0) {
            for (j in 1..numOfCols) {
                val cell = field[i-1][j-1]
                if (cell.isNotEmpty() || cell.isNotBlank()) print("| $cell ")
                else print("|   ")
            }
            print("|")
            println()
        }
        for (j in 1..numOfCols) {
            print("|---")
        }
        if (i == 0 && isEmpty) print("|-y")
        else print("|")
        println()
    }
    if (isEmpty){
        println("|")
        println("x")
    }
}

fun createListOfListField(x: Int, y:Int): MutableList<MutableList<String>> {
    val outerList = mutableListOf<MutableList<String>>()

    for (i in 1..x) {
        val innerList = mutableListOf<String>()
        for (j in 1..y) {
            innerList.add(" ")
        }
        outerList.add(innerList)
    }
    // println("outerList = $outerList")
    return outerList
}

fun getGameFieldCoords(inputSize: String): Pair<Int, Int> {
    val fieldCoordinates = validateFieldSize(inputSize)
    val x = fieldCoordinates.first
    val y = fieldCoordinates.second
    return Pair(x, y)
}

fun validateFieldSize(inputSize: String): Pair<Int, Int> {
    val pattern = Regex("\\d+x\\d+")
    val coordPair = if (pattern.matches(inputSize)) {
        val coordList = inputSize.split("x")
        val x = coordList[0].toInt()
        val y = coordList[1].toInt()
        if (x < 1 || y < 1) {
            Pair(3, 3)
        } else if (x >= 3 || y >= 3) {
            Pair(x, y)
        } else {
            Pair(3, 3)
        }
    } else {
        Pair(3, 3)
    }
    return coordPair
}

fun getBothPlayerNames(): Pair<String, String> {
    println("Enter the first player's name (Player1 by default)")
    val player1 = readln().let { it.ifEmpty { "Player1" } }
    println("First player's name: $player1 ")

    println("Enter the second player's name (Player2 by default)")
    val player2 = readln().let { it.ifEmpty { "Player2" } }
    println("Second player's name: $player2 ")

    return Pair(player1, player2)
}


fun assignPlayerName(playerNum: Int, playerName: String): String {
    return playerName.ifEmpty { "Player${playerNum}" }
}

// Second *player's *name: *Player2

