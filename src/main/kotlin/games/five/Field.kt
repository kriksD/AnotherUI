package games.five

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import games.symbolSet

class Field(
    val width: Int,
    val height: Int,
) {
    val cells = run {
        val newCells = mutableStateListOf<SnapshotStateList<Char>>()

        repeat(width) {
            val newColumn = mutableStateListOf<Char>()
            repeat(height) {
                newColumn.add(symbolSet.random())
            }
            newCells.add(newColumn)
        }

        newCells
    }

    fun check(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int, x4: Int, y4: Int, x5: Int, y5: Int): Int {
        var score = 0

        val cell1 = cells[x1][y1]
        val cell2 = cells[x2][y2]
        val cell3 = cells[x3][y3]
        val cell4 = cells[x4][y4]
        val cell5 = cells[x5][y5]

        if (cell1 == cell2 && cell1 == cell3 && cell1 == cell4 && cell1 == cell5) {
            cells[x1][y1] = symbolSet.random()
            cells[x2][y2] = symbolSet.random()
            cells[x3][y3] = symbolSet.random()
            cells[x4][y4] = symbolSet.random()
            cells[x5][y5] = symbolSet.random()

            score++
        }

        return score
    }

    fun fiveExists(): Boolean {
        val flattenCells = cells.flatten()

        symbolSet.forEach { symbol ->
            if (flattenCells.count { it == symbol } == 5) {
                return true
            }
        }

        return false
    }
}