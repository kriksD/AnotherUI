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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import colorBackgroundLighter
import colorBackgroundSecond
import colorText
import composableFunctions.AppearDisappearAnimation
import gameCellSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import normalText
import padding
import shortAnimationDuration
import smallText
import tinyIconSize

@Composable
fun PairGame(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var score by remember { mutableStateOf(0) }
    var field by remember { mutableStateOf(PairGameField(8, 8)) }
    var selected1 by remember { mutableStateOf<IntOffset?>(null) }
    var selected2 by remember { mutableStateOf<IntOffset?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(padding),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(((gameCellSize.value + padding.value * 2) * 4).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier
                    .size(tinyIconSize)
                    .clickable {
                        score = 0
                        field = PairGameField(8, 8)
                    },
                tint = colorText
            )

            Text(
                text = "Score: $score",
                color = colorText,
                fontSize = normalText,
            )
        }

        Row {
            repeat(field.width) { x ->
                Column {
                    repeat(field.height) { y ->
                        Box(
                            modifier = Modifier
                                .size(gameCellSize + padding)
                                .padding(start = padding, top = padding)
                                .background(
                                    if (x == selected1?.x && y == selected1?.y || x == selected2?.x && y == selected2?.y)
                                        colorBackgroundSecond
                                    else
                                        colorBackgroundLighter
                                )
                                .clickable(enabled = !field.cells[x][y].opened) {
                                    if (selected1 == null) {
                                        selected1 = IntOffset(x, y)
                                        field.cells[x][y].opened = true
                                        return@clickable
                                    }

                                    if (selected2 == null) {
                                        selected2 = IntOffset(x, y)
                                        field.cells[x][y].opened = true

                                        coroutineScope.launch {
                                            withContext(Dispatchers.IO) {
                                                delay(1000L)

                                                score += field.check(selected1!!.x, selected1!!.y, selected2!!.x, selected2!!.y)

                                                selected1 = null
                                                selected2 = null
                                            }
                                        }
                                    }
                                },
                        ) {
                            AppearDisappearAnimation(
                                field.cells[x][y].opened,
                                duration = shortAnimationDuration,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = field.cells[x][y].symbol.toString(),
                                    color = colorText,
                                    fontSize = smallText,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private class PairGameField(
    val width: Int,
    val height: Int,
) {
    val symbolSet = listOf(
        '♡', '♥', '↝', '▣', '▨', '▲', '△', '◲',
        '◱', '◳', '◰', '■', '□', '∏', '∐', '⋱',
        '⋰', '⋯', '⋮', '⨝', '⩩', '⫷', '⫸', '⁜',
        '※', '♪', '●', '○', '◶', '◵', '◴', '◷',
    )

    val cells = run {
        val symbolsQueue = (symbolSet + symbolSet).shuffled()
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

private class Cell(
    val symbol: Char,
    var opened: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        return other is Cell && symbol == other.symbol
    }

    override fun hashCode(): Int {
        return symbol.hashCode()
    }
}