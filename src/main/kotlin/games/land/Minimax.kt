package games.land

import kotlin.math.sqrt

class TempEvaluator(
    val minP: Double,
    val field: Field,
    val forTeam: Team,
) {
    private val movesValues = mutableMapOf<Pair<Int, Int>, Double>()

    fun getNextMove(): Pair<Int, Int>? {
        println("team: ${forTeam.type}")
        val maxValue = movesValues.maxOfOrNull { it.value } ?: return null
        val minValue = movesValues.minOfOrNull { it.value } ?: return null
        val minPValue = minValue + (maxValue - minValue) * minP
        println("minPValue: $minPValue")

        val filteredMoves = movesValues.filter { it.value >= minPValue }
        filteredMoves.forEach { println("move: ${it.key} | value: ${it.value}") }

        return filteredMoves.keys.randomOrNull()
    }

    fun evaluate() {
        val possibleMoves = field.getAllPossible(forTeam)

        possibleMoves.forEach { move ->
            val baseDistance = evaluateBaseDistanceScore(move)
            val cellPrice = evaluateCellPrice(forTeam, move)

            movesValues[move] = baseDistance + cellPrice
        }
    }

    private fun evaluateBaseDistanceScore(fieldPos: Pair<Int, Int>): Double {
        var fullScore = 0.0

        field.teams.forEach { otherTeam ->
            val otherBasePos = field.getBase(otherTeam) ?: return@forEach

            fullScore += calculateDistance(fieldPos, otherBasePos) / (field.width * field.height) * 1.5
        }

        return fullScore
    }

    private fun evaluateCellPrice(team: Team, fieldPos: Pair<Int, Int>): Double {
        var fullScore = 0.0

        val cellsAround = listOfNotNull(
            field.getCell(fieldPos.first - 1, fieldPos.second),
            field.getCell(fieldPos.first + 1, fieldPos.second),
            field.getCell(fieldPos.first, fieldPos.second - 1),
            field.getCell(fieldPos.first, fieldPos.second + 1),
        )

        val cornerCells = listOfNotNull(
            field.getCell(fieldPos.first - 1, fieldPos.second - 1),
            field.getCell(fieldPos.first - 1, fieldPos.second + 1),
            field.getCell(fieldPos.first + 1, fieldPos.second - 1),
            field.getCell(fieldPos.first + 1, fieldPos.second + 1),
        )

        if ((cellsAround + cornerCells).any { it.team == team && it.isBase }) {
            fullScore += 1.2
        }

        val finalCells = if (team.score >= 12) cornerCells + cellsAround else cellsAround

        finalCells.forEach { cell ->
            if (cell.team == null) {
                fullScore += 0.1
            }

            if (cell.team != team) {
                fullScore += 0.3
            }

            if (cell.isBase && cell.team != team) {
                fullScore += 0.8
            }
        }

        return fullScore
    }

    private fun calculateDistance(point1: Pair<Int, Int>, point2: Pair<Int, Int>): Double {
        val deltaX = point2.first.toDouble() - point1.first.toDouble()
        val deltaY = point2.second.toDouble() - point1.second.toDouble()

        return sqrt(deltaX * deltaX + deltaY * deltaY)
    }
}

class SingleMinimax(
    val field: Field,
    val forTeam: Team,
) {
    val root = MinimaxNode(
        currentTeamIndex = field.teams.indexOf(forTeam),
        forTeamIndex = field.teams.indexOf(forTeam),
        field = field,
        move = null,
        depth = 0,
        maxDepth = 1,
    )

    fun getNextMove(): Pair<Int, Int>? {
        return root.getBestMove()
    }
}

class Minimax(
    val field: Field,
    val forTeam: Team,
) {
    val root = MinimaxNode(
        currentTeamIndex = field.teams.indexOf(forTeam),
        forTeamIndex = field.teams.indexOf(forTeam),
        field = field,
        move = null,
        depth = 0,
        maxDepth = 5,
    )

    fun getNextMove(): Pair<Int, Int>? {
        return root.getBestMove()
    }
}

class MinimaxNode(
    val currentTeamIndex: Int,
    val forTeamIndex: Int,
    val field: Field,
    val move: Pair<Int, Int>?,
    val depth: Int,
    val maxDepth: Int,
) {
    private val nodes = mutableListOf<MinimaxNode>()
    var score = 0.0

    init {
        evaluate()

        if (depth <= maxDepth) {
            makeNextNods()
        }
    }

    private fun makeNextNods() {
        if (field.teams[currentTeamIndex].score <= 0) return

        val currentTeam = field.teams[currentTeamIndex]

        if (currentTeam.score <= 0) {
            val newNode = MinimaxNode(
                currentTeamIndex = if (this.currentTeamIndex == field.teams.lastIndex) 0 else this.currentTeamIndex + 1,
                forTeamIndex = this.forTeamIndex,
                field = field,
                move = null,
                depth = depth + 1,
                maxDepth = maxDepth,
            )

            nodes.add(newNode)
            return
        }

        val possibleCells = field.getAllPossible(currentTeam)

        possibleCells.forEach { cellPos ->
            val newField = field.copy()
            newField.action(cellPos.first, cellPos.second, currentTeam)

            val newNode = MinimaxNode(
                currentTeamIndex = if (this.currentTeamIndex == field.teams.lastIndex) 0 else this.currentTeamIndex + 1,
                forTeamIndex = this.forTeamIndex,
                field = newField,
                move = cellPos,
                depth = depth + 1,
                maxDepth = maxDepth,
            )

            nodes.add(newNode)
        }
    }

    private fun evaluate() {
        if (move == null) return

        val currentTeam = field.teams[currentTeamIndex]

        field.action(move.first, move.second, currentTeam)

        val cellsScores = field.teams.filter { it != currentTeam && it.score > 0 }.map { team ->
            field.cells.flatten().count { it.team == team } / (field.width * field.height)
        }

        val baseScores = field.teams.filter { it != currentTeam && it.score > 0 }.map { team ->
            evaluateBaseDistanceScore(team, move)
        }

        val currentTeamCellScore = field.cells.flatten().count { it.team == currentTeam } / (field.width * field.height)
        val currentTeamCellPrice = evaluateCellPrice(currentTeam, move)
        val currentTeamBaseScore = evaluateBaseDistanceScore(currentTeam, move)

        val opponentsAverage = cellsScores.average() + baseScores.average()
        val currentTeamScore = currentTeamCellScore + currentTeamBaseScore + currentTeamCellPrice

        score = currentTeamScore - opponentsAverage
    }

    private fun evaluateBaseDistanceScore(team: Team, fieldPos: Pair<Int, Int>): Double {
        var fullScore = 0.0

        field.teams.filter { it != team }.forEach { otherTeam ->
            val otherBasePos = field.getBase(otherTeam) ?: return@forEach

            fullScore += calculateDistance(fieldPos, otherBasePos) / (field.width * field.height)
        }

        return fullScore
    }

    private fun evaluateCellPrice(team: Team, fieldPos: Pair<Int, Int>): Double {
        if (field.cells[fieldPos.first][fieldPos.second].isBase) {
            return 10.0
        }

        var fullScore = 0.0

        val cellsAround = listOfNotNull(
            field.getCell(fieldPos.first - 1, fieldPos.second),
            field.getCell(fieldPos.first + 1, fieldPos.second),
            field.getCell(fieldPos.first, fieldPos.second - 1),
            field.getCell(fieldPos.first, fieldPos.second + 1),
        )

        cellsAround.forEach { cell ->
            if (cell.team == null) {
                fullScore += 0.1
                return@forEach
            }

            if (cell.team != team) {
                fullScore += 0.2
            }

            if (cell.isBase) {
                fullScore += 5.0
            }
        }

        return fullScore
    }

    private fun calculateDistance(point1: Pair<Int, Int>, point2: Pair<Int, Int>): Double {
        val deltaX = point2.first.toDouble() - point1.first.toDouble()
        val deltaY = point2.second.toDouble() - point1.second.toDouble()

        return sqrt(deltaX * deltaX + deltaY * deltaY)
    }

    fun getBestMove(): Pair<Int, Int>? {
        val results = nodes.map { Pair(it.calculateFinalScore(), it) }
        return results.maxByOrNull { it.first }?.second?.move
    }

    private fun calculateFinalScore(currentScore: Double = 0.0): Double {
        val nextScores = if (nodes.isNotEmpty()) nodes.map { it.calculateFinalScore(score) } else return 0.0
        return if (currentTeamIndex == forTeamIndex) currentScore + nextScores.average() else currentScore - nextScores.average()
    }
}