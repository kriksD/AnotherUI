package games.land

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import colorBackground
import colorBackgroundSecond
import colorTextError
import colorTextSecond
import games.symbolSet

class Team(
    val type: TeamType,
    score: Int,
) {
    var score: Int by mutableStateOf(score)

    fun copy() = Team(type, score)

    val color get() = when (type) {
        TeamType.Teal -> colorBackgroundSecond
        TeamType.Red -> colorTextError
        TeamType.Black -> colorBackground
        TeamType.White -> colorTextSecond
    }

    val baseSymbol = symbolSet.random()

    override fun toString(): String = "$type: $score"

    override fun equals(other: Any?): Boolean {
        return other is Team && other.type == type
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}

enum class TeamType {
    Teal, Red, Black, White;
}