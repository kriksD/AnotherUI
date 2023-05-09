package gpt2Tokenizer

object GlobalTokenizer {
    val tokenizer = run {
        val encoder = loadEncoder()
        val decoder = encoder.entries.associateBy({ it.value }, { it.key })
        val bpeRanks = loadBpeRanks()

        GPT2Tokenizer(encoder, decoder, bpeRanks)
    }

    fun encode(text: String): List<Int> = tokenizer.encode(text)

    fun countTokens(text: String): Int = tokenizer.countTokens(text)
}