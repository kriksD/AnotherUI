package games.line

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
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
import composableFunctions.AppearDisappearAnimation
import composableFunctions.DescriptionSlider
import gameCellSize
import normalText
import padding
import tinyIconSize

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LineGame(
    isSettingsOpen: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var score by remember { mutableStateOf(0) }
    var currentWidth by remember { mutableStateOf(8) }
    var currentHeight by remember { mutableStateOf(8) }
    var field by remember { mutableStateOf(Field(currentWidth, currentHeight)) }
    var nextPiece by remember { mutableStateOf(Piece.createRandom()) }
    var selectedX by remember { mutableStateOf(0) }
    var selectedY by remember { mutableStateOf(0) }

    Column(
        verticalArrangement = Arrangement.spacedBy(padding),
        modifier = modifier,
    ) {
        AppearDisappearAnimation(
            visible = isSettingsOpen,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.1F)
            ) {
                Row {
                    DescriptionSlider(
                        name = "Width",
                        value = currentWidth.toFloat(),
                        onValueChange = {
                            currentWidth = it.toInt()
                        },
                        onValueChangeFinished = {
                            currentWidth = it.toInt()

                            score = 0
                            field = Field(currentWidth, currentHeight)
                            nextPiece = Piece.createRandom()
                        },
                        valueRange = 8F..16F,
                        intStep = 1,
                        modifier = Modifier.weight(1F),
                    )

                    DescriptionSlider(
                        name = "Height",
                        value = currentHeight.toFloat(),
                        onValueChange = {
                            currentHeight = it.toInt()
                        },
                        onValueChangeFinished = {
                            currentHeight = it.toInt()

                            score = 0
                            field = Field(currentWidth, currentHeight)
                            nextPiece = Piece.createRandom()
                        },
                        valueRange = 8F..16F,
                        intStep = 1,
                        modifier = Modifier.weight(1F),
                    )
                }
            }
        }

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
                        field = Field(currentWidth, currentHeight)
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