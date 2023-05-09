package client

import character.ACharacter
import gpt2Tokenizer.GlobalTokenizer
import user

fun String.isEnd(charName: String): Boolean = contains("$charName:") || contains("You:") || contains("<START>")

fun String.isEndToken(expectedTokens: Int): Boolean =
    GlobalTokenizer.countTokens(this) < expectedTokens

fun String.dropRest(charName: String): String = this
    .replace("$charName:", "")
    .substringBefore("You:")
    .substringBefore("<START>")
    .trim()

fun String.formatExamples(character: ACharacter): String {
    return this
        .replace("{{char}}", character.jsonData.name)
        .replace("<char>", character.jsonData.name)
        .replace(Regex("\\{\\{user}}:?"), "You:")
        .replace(Regex("<user>:?"), "You:")
        .replace("{{user_name}}", user.name)
        .replace("<user_name>", user.name)
}

fun String.formatAnything(character: ACharacter): String {
    return this
        .replace("{{char}}", character.jsonData.name)
        .replace("<char>", character.jsonData.name)
        .replace("{{user}}", user.name)
        .replace("<user>", user.name)
        .replace("{{user_name}}", user.name)
        .replace("<user_name>", user.name)
}