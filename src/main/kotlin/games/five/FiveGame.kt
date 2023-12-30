package games.five

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import colorBackgroundLighter
import colorBackgroundSecond
import colorText
import composableFunctions.AppearDisappearAnimation
import gameCellSize
import normalText
import padding
import smallText
import tinyIconSize

@Composable
fun FiveGame(
    isSettingsOpen: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var score by remember { mutableStateOf(0) }
    var field by remember { mutableStateOf(Field(8, 8)) }
    var fiveExists by remember { mutableStateOf(field.fiveExists()) }

    var selected1 by remember { mutableStateOf<IntOffset?>(null) }
    var selected2 by remember { mutableStateOf<IntOffset?>(null) }
    var selected3 by remember { mutableStateOf<IntOffset?>(null) }
    var selected4 by remember { mutableStateOf<IntOffset?>(null) }
    var selected5 by remember { mutableStateOf<IntOffset?>(null) }

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
                        field = Field(8, 8)
                        fiveExists = field.fiveExists()
                    },
                tint = colorText
            )

            Column {
                Text(
                    text = "Score: $score",
                    color = colorText,
                    fontSize = normalText,
                )

                AppearDisappearAnimation(!fiveExists) {
                    Text(
                        text = "No fives on the field!",
                        color = colorText,
                        fontSize = smallText,
                    )
                }
            }
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
                                    if (
                                        x == selected1?.x && y == selected1?.y
                                        || x == selected2?.x && y == selected2?.y
                                        || x == selected3?.x && y == selected3?.y
                                        || x == selected4?.x && y == selected4?.y
                                        || x == selected5?.x && y == selected5?.y
                                    ) colorBackgroundSecond else colorBackgroundLighter
                                )
                                .clickable(
                                    enabled = IntOffset(x, y) != selected1
                                            && IntOffset(x, y) != selected2
                                            && IntOffset(x, y) != selected3
                                            && IntOffset(x, y) != selected4
                                            && IntOffset(x, y) != selected5
                                ) {
                                    if (selected1 == null) {
                                        selected1 = IntOffset(x, y)
                                        return@clickable
                                    }

                                    if (selected2 == null) {
                                        selected2 = IntOffset(x, y)
                                        return@clickable
                                    }

                                    if (selected3 == null) {
                                        selected3 = IntOffset(x, y)
                                        return@clickable
                                    }

                                    if (selected4 == null) {
                                        selected4 = IntOffset(x, y)
                                        return@clickable
                                    }

                                    if (selected5 == null) {
                                        selected5 = IntOffset(x, y)

                                        score += field.check(
                                            selected1!!.x,
                                            selected1!!.y,
                                            selected2!!.x,
                                            selected2!!.y,
                                            selected3!!.x,
                                            selected3!!.y,
                                            selected4!!.x,
                                            selected4!!.y,
                                            selected5!!.x,
                                            selected5!!.y,
                                        )

                                        fiveExists = field.fiveExists()

                                        selected1 = null
                                        selected2 = null
                                        selected3 = null
                                        selected4 = null
                                        selected5 = null
                                    }
                                },
                        ) {
                            Text(
                                text = field.cells[x][y].toString(),
                                color = colorText,
                                fontSize = smallText,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}