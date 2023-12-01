package prompt

fun samplerName(number: Int): String = when (number) {
    0 -> "Top K"
    1 -> "Top A"
    2 -> "Top P and Min P"
    3 -> "Tail Free Sampling"
    4 -> "Typical P"
    5 -> "Temperature"
    6 -> "Repetition Penalty"
    else -> "<unknown>"
}