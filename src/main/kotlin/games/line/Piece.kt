package games.line

import org.jetbrains.skiko.currentNanoTime
import kotlin.random.Random

class Piece(
    val width: Int,
    val height: Int,
    val cells: List<List<Boolean>>,
) {

    // Generated with GPT 3.5
    fun rotate(): Piece {
        if (width != height) {
            // Non-square piece, just swap width and height
            return Piece(height, width, cells.transpose())
        }

        val rotatedCells = MutableList(height) { MutableList(width) { false } }

        for (x in 0 until width) {
            for (y in 0 until height) {
                rotatedCells[y][width - 1 - x] = cells[x][y]
            }
        }

        return Piece(width, height, rotatedCells)
    }

    // Generated with GPT 3.5
    private fun List<List<Boolean>>.transpose(): List<List<Boolean>> {
        val transposed = MutableList(this[0].size) { MutableList(this.size) { false } }
        for (x in this.indices) {
            for (y in this[x].indices) {
                transposed[y][x] = this[x][y]
            }
        }
        return transposed
    }

    companion object {
        private val presets = listOf(
            Piece(1, 1, listOf(listOf(true))),
            Piece(1, 2, listOf(listOf(true, true))),
            Piece(1, 1, listOf(listOf(true))),
            Piece(1, 2, listOf(listOf(true, true))),
            Piece(1, 3, listOf(listOf(true, true, true))),
            Piece(1, 4, listOf(listOf(true, true, true, true))),
            Piece(
                2,
                2,
                listOf(
                    listOf(true, true),
                    listOf(true, true),
                )
            ),
            Piece(
                2,
                2,
                listOf(
                    listOf(true, true),
                    listOf(true, false),
                )
            ),
            Piece(
                2,
                2,
                listOf(
                    listOf(false, true),
                    listOf(true, false),
                )
            ),
            Piece(
                2,
                2,
                listOf(
                    listOf(true, true),
                    listOf(true, false),
                )
            ),
            Piece(
                2,
                2,
                listOf(
                    listOf(false, true),
                    listOf(true, false),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(true, true),
                    listOf(true, true),
                    listOf(true, true),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(true, true),
                    listOf(true, false),
                    listOf(true, false),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(true, true),
                    listOf(false, true),
                    listOf(false, true),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(false, true),
                    listOf(true, true),
                    listOf(true, false),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(true, false),
                    listOf(true, true),
                    listOf(false, true),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(true, false),
                    listOf(true, true),
                    listOf(true, false),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(true, true),
                    listOf(true, false),
                    listOf(true, true),
                )
            ),
            Piece(
                3,
                2,
                listOf(
                    listOf(false, true),
                    listOf(true, false),
                    listOf(false, true),
                )
            ),
            Piece(
                3,
                3,
                listOf(
                    listOf(true, true, true),
                    listOf(true, false, false),
                    listOf(true, false, false),
                )
            ),
            Piece(
                3,
                3,
                listOf(
                    listOf(false, false, true),
                    listOf(false, true, false),
                    listOf(true, false, false),
                )
            ),
            Piece(
                3,
                3,
                listOf(
                    listOf(true, false, true),
                    listOf(false, true, false),
                    listOf(true, false, true),
                )
            ),
            Piece(
                3,
                3,
                listOf(
                    listOf(false, true, false),
                    listOf(true, true, true),
                    listOf(false, true, false),
                )
            ),
            Piece(
                3,
                3,
                listOf(
                    listOf(false, false, true),
                    listOf(false, true, false),
                    listOf(true, false, true),
                )
            ),
        )

        fun createRandom(): Piece {
            var piece = presets.random()

            repeat(Random(currentNanoTime()).nextInt(0, 3)) {
                piece = piece.rotate()
            }

            return piece
        }
    }
}