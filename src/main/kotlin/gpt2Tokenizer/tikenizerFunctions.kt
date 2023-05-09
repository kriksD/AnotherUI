package gpt2Tokenizer

import decodeAllUTF
import java.io.File

fun loadEncoder(): Map<String, Int> {
    val file = File("data/tokenizer/gpt2-vocab.json")

    return if (file.exists()) {
        hashMapOf<String, Int>().apply {
            val text = file.readText()
            val elementsRegex = Regex("\".+?\": ?\\d+?,").findAll(text)
            elementsRegex.forEach { match ->
                val tokenEnd = Regex("\": ?\\d+,").find(match.value)?.range?.first

                tokenEnd?.let { end ->
                    val token = decodeAllUTF(match.value.substring(1, end))

                    val idText = Regex("\\d+,$")
                        .find(match.value)
                        ?.value
                        ?.dropLast(1)

                    val id = idText?.toIntOrNull()
                    id?.let { put(token, it) }
                }
            }
        }
    } else {
        throw Exception("File \"gpt2-vocab.json\" doesn't exist")
    }
}

fun loadBpeRanks():Map<Pair<String, String>, Int> {
    val file = File("data/tokenizer/gpt2-merges.txt")

    return if (file.exists()) {
        hashMapOf<Pair<String, String>, Int>().apply {
            val texts = file.readLines()
            texts.drop(1).forEachIndexed { i, s ->
                val list = s.split(" ")
                val keyTuple = list[0] to list[1]
                put(keyTuple, i)
            }
        }
    } else {
        throw Exception("File \"gpt2-merges.txt\" doesn't exist")
    }
}