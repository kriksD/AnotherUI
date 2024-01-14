package games.pair

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import games.symbolSet

class Field(
    val width: Int,
    val height: Int,
) {
    val cells = run {
        val symbolsQueue = makeSymbolSet()
        var symbolIndex = 0
        val newCells = mutableStateListOf<SnapshotStateList<Cell>>()

        repeat(width) {
            val newColumn = mutableStateListOf<Cell>()
            repeat(height) {
                newColumn.add(Cell(symbolsQueue[symbolIndex], false))
                symbolIndex++
            }
            newCells.add(newColumn)
        }

        newCells
    }

    private fun makeSymbolSet(): List<Char> {
        val symbolCount = width * height
        val symbolsQueue = mutableListOf<Char>()

        repeat((symbolCount / 2) / symbolSet.size) {
            symbolsQueue += symbolSet + symbolSet
        }

        val remainingSymbols = (symbolCount / 2) % symbolSet.size
        if (remainingSymbols > 0) {
            val symbols = symbolSet.shuffled().take(remainingSymbols)
            symbolsQueue += symbols + symbols
        }

        if (symbolCount % 2 == 1) {
            symbolsQueue.add(' ')
        }

        return symbolsQueue.shuffled()
    }

    fun check(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        var score = 0

        val cell1 = cells[x1][y1]
        val cell2 = cells[x2][y2]

        if (cell1 == cell2) {
            score += 1

            cell1.opened = true
            cell2.opened = true

        } else {
            cell1.opened = false
            cell2.opened = false
        }

        return score
    }
}