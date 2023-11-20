package games.pair

class Cell(
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