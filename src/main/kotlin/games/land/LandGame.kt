package games.land

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
import androidx.compose.ui.unit.dp
import colorBackgroundLighter
import colorText
import composableFunctions.AppearDisappearAnimation
import composableFunctions.CheckboxText
import composableFunctions.DescriptionSlider
import gameCellSize
import games.Cells
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import normalText
import padding
import smallText
import tinyIconSize

@Composable
fun LandGame(
    isSettingsOpen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val teams = remember { mutableListOf(
        Team(TeamType.Teal, 0),
        Team(TeamType.Red, 0),
        Team(TeamType.Black, 0),
        Team(TeamType.White, 0)
    ) }
    var currentWidth by remember { mutableStateOf(8) }
    var currentHeight by remember { mutableStateOf(8) }
    var field by remember { mutableStateOf(Field(currentWidth, currentHeight, teams)) }
    var areActionsGoing by remember { mutableStateOf(false) }
    var onlyBots by remember { mutableStateOf(false) }

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
                            field = Field(currentWidth, currentHeight, teams)
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
                            field = Field(currentWidth, currentHeight, teams)
                        },
                        valueRange = 8F..16F,
                        intStep = 1,
                        modifier = Modifier.weight(1F),
                    )
                }

                CheckboxText(
                    text = "Only bots",
                    enabled = onlyBots,
                    onChange = {
                        onlyBots = it
                    },
                )
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
                        teams.clear()
                        teams.add(Team(TeamType.Teal, 0))
                        teams.add(Team(TeamType.Red, 0))
                        teams.add(Team(TeamType.Black, 0))
                        teams.add(Team(TeamType.White, 0))
                        field = Field(currentWidth, currentHeight, teams)
                    },
                tint = colorText
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(padding),
            ) {
                teams.forEach { team ->
                    Text(
                        text = team.toString(),
                        color = team.color,
                        fontSize = normalText,
                    )
                }
            }
        }

        Cells(
            width = field.width,
            height = field.height,
            onClick = { x, y ->
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        areActionsGoing = true

                        if (!onlyBots) {
                            field.action(x, y, teams.find { it.type == TeamType.Teal }!!)
                            delay(1000L)
                        }

                        teams.filter {
                            it.type != TeamType.Teal && it.score != 0
                        }.forEach { team ->
                            val timeStart = System.currentTimeMillis()
                            field.action(team)
                            val timeEnd = System.currentTimeMillis()

                            delay(1000L - (timeEnd - timeStart))
                        }

                        while (
                            teams.find { it.type == TeamType.Teal }!!.score == 0 || onlyBots
                            && teams.count { it.score != 0 } >= 2
                        ) {
                            teams.filter {
                                (it.type != TeamType.Teal || onlyBots) && it.score != 0
                            }.forEach { team ->
                                val timeStart = System.currentTimeMillis()
                                field.action(team)
                                val timeEnd = System.currentTimeMillis()

                                delay(1000L - (timeEnd - timeStart))
                            }
                        }

                        areActionsGoing = false
                    }
                }
            },
            isEnable = { x, y ->
                !areActionsGoing && field.cells[x][y].team?.type == TeamType.Teal && field.isPossible(x, y, teams.find { it.type == TeamType.Teal }!!)
            }
        ) { x, y ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(field.cells[x][y].team?.color ?: colorBackgroundLighter),
            ) {
                if (field.cells[x][y].isBase) {
                    Text(
                        text = field.cells[x][y].team?.baseSymbol?.toString() ?: "",
                        color = colorText,
                        fontSize = smallText,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}