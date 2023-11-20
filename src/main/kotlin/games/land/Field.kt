package games.land

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

class Field {
    val width: Int
    val height: Int
    val teams: List<Team>
    val cells: SnapshotStateList<SnapshotStateList<Cell>>

    constructor(
        width: Int,
        height: Int,
        teams: List<Team>,
    ) {
        this.width = width
        this.height = height
        this.teams = teams
        this.cells = setCells()
    }

    private fun setCells(): SnapshotStateList<SnapshotStateList<Cell>> {
        val newCells = mutableStateListOf<SnapshotStateList<Cell>>()

        repeat(width) {
            val newColumn = mutableStateListOf<Cell>()
            repeat(height) {
                newColumn.add(Cell())
            }
            newCells.add(newColumn)
        }

        teams.forEach { team ->
            team.score = 1
            val baseCell = newCells.flatten().filter { it.team == null }.random()
            baseCell.team = team
            baseCell.isBase = true
        }

        return newCells
    }

    constructor(
        width: Int,
        height: Int,
        teams: List<Team>,
        cells: SnapshotStateList<SnapshotStateList<Cell>>,
    ) {
        this.width = width
        this.height = height
        this.teams = teams
        this.cells = cells
    }

    fun copy(): Field {
        val teams = teams.map { it.copy() }
        val cells = cells.map { row ->
            row.map { cell ->
                cell.copy(teams.find { it.type == cell.team?.type })
            }.toMutableStateList()
        }.toMutableStateList()

        return Field(width, height, teams, cells)
    }

    fun action(x: Int, y: Int, team: Team) {
        if (cells[x][y].team != team) return

        val cellsAround = listOfNotNull(getCell(x, y - 1), getCell(x, y + 1), getCell(x - 1, y), getCell(x + 1, y))

        cellsAround.forEach { cell ->
            val cellTeam = cell.team

            if (cellTeam == null) {
                cell.team = team
                team.score++
                return@forEach
            }

            if (cellTeam != team && cell.isBase) {
                cell.isBase = false
                cellTeam.score = 0

                cells.flatten().forEach {
                    if (it.team == cellTeam) {
                        it.team = null
                    }
                }

                cell.team = team
                team.score++

                return@forEach
            }

            if (cellTeam != team) {
                cell.team = team
                cellTeam.score--
                team.score++
            }
        }
    }

    fun action(team: Team) {
        val fieldCopy = copy()
        val minimax = Minimax(
            field = fieldCopy,
            forTeam = fieldCopy.teams.find { it.type == team.type } ?: return,
        )
        val cell = minimax.getNextMove() ?: return
        println(cell)
        //val cell = getAllPossible(team).randomOrNull() ?: return
        action(cell.first, cell.second, team)
    }

    fun getAllPossible(team: Team): List<Pair<Int, Int>> {
        val possibleCells = mutableListOf<Pair<Int, Int>>()

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (isPossible(x, y, team)) {
                    possibleCells.add(Pair(x, y))
                }
            }
        }

        return possibleCells
    }

    fun isPossible(x: Int, y: Int, team: Team): Boolean {
        val cellsAround = listOfNotNull(getCell(x, y - 1), getCell(x, y + 1), getCell(x - 1, y), getCell(x + 1, y))
        return cells[x][y].team == team && !cellsAround.all { it.team == team }
    }

    fun getCell(x: Int, y: Int): Cell? {
        if (x < 0 || x >= width || y < 0 || y >= height) return null
        return cells[x][y]
    }

    fun getBase(team: Team): Pair<Int, Int>? {
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (getCell(x, y)?.team == team && getCell(x, y)?.isBase == true) {
                    return Pair(x, y)
                }
            }
        }

        return null
    }
}