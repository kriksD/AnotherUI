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
import gameCellSize
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
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val teams = remember { mutableListOf(
        Team(TeamType.Teal, 0),
        Team(TeamType.Red, 0),
        Team(TeamType.Black, 0),
        Team(TeamType.White, 0)
    ) }
    var field by remember { mutableStateOf(Field(8, 8, teams)) }
    var areActionsGoing by remember { mutableStateOf(false) }

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
                        teams.clear()
                        teams.add(Team(TeamType.Teal, 0))
                        teams.add(Team(TeamType.Red, 0))
                        teams.add(Team(TeamType.Black, 0))
                        teams.add(Team(TeamType.White, 0))
                        field = Field(8, 8, teams)
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

        Row {
            repeat(field.width) { x ->
                Column {
                    repeat(field.height) { y ->
                        Box(
                            modifier = Modifier
                                .size(gameCellSize + padding)
                                .padding(start = padding, top = padding)
                                .background(field.cells[x][y].team?.color ?: colorBackgroundLighter)
                                .clickable(enabled = !areActionsGoing && field.cells[x][y].team?.type == TeamType.Teal && field.isPossible(x, y, teams.find { it.type == TeamType.Teal }!!)) {
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            areActionsGoing = true

                                            field.action(x, y, teams.find { it.type == TeamType.Teal }!!)
                                            delay(1000L)

                                            teams.filter {
                                                it.type != TeamType.Teal && it.score != 0
                                            }.forEachIndexed { index, team ->
                                                val timeStart = System.currentTimeMillis()
                                                field.action(team)
                                                val timeEnd = System.currentTimeMillis()

                                                delay(1000L - (timeEnd - timeStart))
                                            }

                                            while (
                                                teams.find { it.type == TeamType.Teal }!!.score == 0
                                                && teams.count { it.score != 0 } >= 2
                                            ) {
                                                teams.filter {
                                                    it.type != TeamType.Teal && it.score != 0
                                                }.forEachIndexed { index, team ->
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
        }
    }
}