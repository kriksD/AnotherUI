package games.land

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import randomWithAmount

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

        repeat(width) { x ->
            val newColumn = mutableStateListOf<Cell>()
            repeat(height) { y ->
                newColumn.add(Cell(x, y))
            }
            newCells.add(newColumn)
        }

        teams.forEach { team ->
            val baseCell = newCells.flatten().filter { it.team == null }.random()
            baseCell.team = team
            baseCell.isBase = true

            newCells.getOrNull(baseCell.x - 1)?.getOrNull(baseCell.y)?.team = team
            newCells.getOrNull(baseCell.x + 1)?.getOrNull(baseCell.y)?.team = team
            newCells.getOrNull(baseCell.x)?.getOrNull(baseCell.y - 1)?.team = team
            newCells.getOrNull(baseCell.x)?.getOrNull(baseCell.y + 1)?.team = team
        }

        teams.forEach { team ->
            team.score = newCells.flatten().count { it.team == team }
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

        val cellsAround = listOfNotNull(
            getCell(x, y - 1),
            getCell(x, y + 1),
            getCell(x - 1, y),
            getCell(x + 1, y)
        )

        val cornerCellsAround = listOfNotNull(
            getCell(x - 1, y - 1),
            getCell(x + 1, y - 1),
            getCell(x - 1, y + 1),
            getCell(x + 1, y + 1),
        )

        val finalCells = when {
            team.score > 12 -> cellsAround + cornerCellsAround.randomWithAmount(1)
            team.score > 10 -> cellsAround
            team.score > 8 -> cellsAround.randomWithAmount(3)
            team.score > 0 -> cellsAround.randomWithAmount(2)
            else -> cellsAround.randomWithAmount(1)
        }
        //val finalCells = if (team.score >= 16) listOf(cornerCellsAround.random()) + cellsAround else cellsAround

        finalCells.forEach { cell ->
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
        /*val fieldCopy = copy()
        val minimax = Minimax(
            field = fieldCopy,
            forTeam = fieldCopy.teams.find { it.type == team.type } ?: return,
        )
        val cell = minimax.getNextMove() ?: return*/
        //val cell = getAllPossible(team).randomOrNull() ?: return

        val evaluator = TempEvaluator(0.90, this, team)
        evaluator.evaluate()
        val cell = evaluator.getNextMove() ?: return

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
        return !cells[x][y].isBase && cells[x][y].team == team && !cellsAround.all { it.team == team }
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