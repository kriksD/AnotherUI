package client

import gpt2Tokenizer.GlobalTokenizer
import settings

data class GeneratedData(
    val message: String,
    val tokensLeft: Int,
    val tries: Int = settings.multi_gen.tries,
    val end: Boolean = false,
) {
    fun tokensToGenerate(): Int =
        if (tokensLeft - settings.multi_gen.tokens_per_step <= 0) tokensLeft else settings.multi_gen.tokens_per_step

    fun nextData(newMessagePart: String, charName: String, tokensToGenerate: Int): GeneratedData {
        val newMessage = message + newMessagePart

        return if (newMessage.isEnd(charName)/* || newMessagePart.isEndToken(tokensToGenerate)*/) { // fix commented somehow
            GeneratedData(
                message = newMessage.dropRest(charName),
                tokensLeft = tokensLeft - GlobalTokenizer.countTokens(newMessagePart),
                tries = tries - 1,
                end = tries - 1 <= 0,
            )
        } else {
            GeneratedData(
                message = newMessage,
                tokensLeft = tokensLeft - tokensToGenerate,
                tries = tries,
            )
        }
    }
}
