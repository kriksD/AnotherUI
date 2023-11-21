package games.land

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Cell(
    val x: Int,
    val y: Int,
    isBase: Boolean = false,
    team: Team? = null,
) {
    var isBase: Boolean by mutableStateOf(isBase)
    var team: Team? by mutableStateOf(team)

    fun copy(newTeam: Team?) = Cell(x, y, isBase, newTeam)
}