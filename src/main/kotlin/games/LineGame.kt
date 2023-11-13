package games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecond
import colorText
import colorTextError
import gameCellSize
import normalText
import org.jetbrains.skiko.currentNanoTime
import padding
import tinyIconSize
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LineGame(
    modifier: Modifier = Modifier,
) {
    var score by remember { mutableStateOf(0) }
    var field by remember { mutableStateOf(LineGameField(8, 8)) }
    var nextPiece by remember { mutableStateOf(Piece.createRandom()) }
    var selectedX by remember { mutableStateOf(0) }
    var selectedY by remember { mutableStateOf(0) }

    Column(
        verticalArrangement = Arrangement.spacedBy(padding),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(((gameCellSize.value + padding.value * 2) * 3).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier
                    .size(tinyIconSize)
                    .clickable {
                        score = 0
                        field = LineGameField(8, 8)
                        nextPiece = Piece.createRandom()
                    },
                tint = colorText
            )

            Text(
                text = "Score: $score",
                color = colorText,
                fontSize = normalText,
            )

            Piece(nextPiece)
        }

        Row {
            repeat(field.width) { x ->
                Column {
                    repeat(field.height) { y ->
                        Box(
                            modifier = Modifier
                                .size(gameCellSize + padding)
                                .clickable {
                                    if (field.set(x, y, nextPiece)) nextPiece = Piece.createRandom()

                                    score += field.check()
                                }
                                .onPointerEvent(PointerEventType.Enter) {
                                    selectedX = x
                                    selectedY = y
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = padding, top = padding)
                                    .background(
                                        if (field.cells[x][y]) colorBackgroundSecond else colorBackgroundLighter
                                    )
                                    .background(
                                        if (
                                            nextPiece.cells
                                                .getOrNull(x - selectedX)
                                                ?.getOrNull(y - selectedY) == true
                                        ) {
                                            if (field.canSet(selectedX, selectedY, nextPiece)) colorBackground else colorTextError

                                        } else Color.Transparent
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Piece(
    piece: Piece,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(padding),
        modifier = modifier
    ) {
        repeat(piece.width) { x ->
            Column(
                verticalArrangement = Arrangement.spacedBy(padding),
            ) {
                repeat(piece.height) { y ->
                    Box(
                        modifier = Modifier
                            .size(gameCellSize)
                            .background(if (piece.cells[x][y]) colorBackgroundSecond else Color.Transparent),
                    )
                }
            }
        }
    }
}

private class LineGameField(
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

        repeat(width) { x ->
            var count = 0

            repeat(height) { y ->
                if (cells[x][y]) count++
            }

            if (count == width) {
                repeat(height) { y ->
                    cells[x][y] = false
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

            if (count == height) {
                repeat(width) { x ->
                    cells[x][y] = false
                }

                score++
            }

            count = 0
        }

        return score
    }
}

private class Piece(
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
        val presets = listOf(
            Piece(1, 1, listOf(listOf(true))),
            Piece(1, 2, listOf(listOf(true, true))),
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
                    listOf(true, false),
                    listOf(true, true),
                    listOf(true, false),
                )
            ),
            Piece(
                3,
                3,
                listOf(
                    listOf(true, true, true),
                    listOf(true, true, true),
                    listOf(true, true, true),
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