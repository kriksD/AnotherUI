package client

import character.ACharacter
import gpt2Tokenizer.GlobalTokenizer
import settings
import user

fun String.isEnd(charName: String): Boolean = contains("$charName:") || contains("You:") || contains("<START>") || contains("_end_of_chat_")

fun String.isEndToken(expectedTokens: Int): Boolean =
    GlobalTokenizer.countTokens(this) < expectedTokens

fun String.dropRest(charName: String): String = this
    .replace("$charName:", "")
    .substringBefore("${user.name}:")
    .substringBefore("You:")
    .substringBefore("<START>")
    .substringBefore("<|user|>")
    .substringBefore("<|model|>")
    .trimIndent()

fun String.formatExamples(character: ACharacter): String {
    return this
        .replace("{{char}}", character.jsonData.name)
        .replace("<char>", character.jsonData.name)
        .replace(Regex("\\{\\{user}}:?"), if (settings.use_username) "${user.name}:" else "You:")
        .replace(Regex("<user>:?"), if (settings.use_username) "${user.name}:" else "You:")
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