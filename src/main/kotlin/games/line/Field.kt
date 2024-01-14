package games.line

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Field(
    val width: Int,
    val height: Int,
) {
    val cells = run {
        val newCells = mutableStateListOf<SnapshotStateList<Boolean>>()

        repeat(width) {
            val newColumn = mutableStateListOf<Boolean>()
            repeat(height) { newColumn.add(false) }
            newCells.add(newColumn)
        }

        newCells
    }

    fun set(x: Int, y: Int, value: Boolean) {
        cells[x][y] = value
    }

    fun set(x: Int, y: Int, piece: Piece): Boolean {
        if (!canSet(x, y, piece)) return false

        repeat(piece.width) { cx ->
            repeat(piece.height) { cy ->
                cells[x + cx][y + cy] = cells[x + cx][y + cy] || piece.cells[cx][cy]
            }
        }

        return true
    }

    fun canSet(x: Int, y: Int, piece: Piece): Boolean {
        if (x + piece.width > width || y + piece.height > height) return false

        repeat(piece.width) { cx ->
            repeat(piece.height) { cy ->
                val cell = cells[x + cx][y + cy]
                val pieceCell = piece.cells[cx][cy]

                if (pieceCell && cell) {
                    return false
                }
            }
        }

        return true
    }

    fun check(): Int {
        var score = 0
        val finishedRows = mutableListOf<Int>()
        val finishedColumns = mutableListOf<Int>()

        repeat(width) { x ->
            var count = 0

            repeat(height) { y ->
                if (cells[x][y]) count++
            }

            if (count == height) {
                repeat(height) {
                    finishedRows.add(x)
                }

                score++
            }

            count = 0
        }

        repeat(height) { y ->
            var count = 0

            repeat(width) { x ->
                if (cells[x][y]) count++
            }

            if (count == width) {
                repeat(width) {
                    finishedColumns.add(y)
                }

                score++
            }

            count = 0
        }

        finishedRows.forEach { x ->
            repeat(height) { y ->
                cells[x][y] = false
            }
        }

        finishedColumns.forEach { y ->
            repeat(width) { x ->
                cells[x][y] = false
            }
        }

        return score
    }
}