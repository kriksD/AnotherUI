package client

import character.ACharacter
import settings
import user

fun String.dropRest(charName: String): String = this
    .replace("$charName:", "")
    .substringBefore("${user.name}:")
    .substringBefore("You:")
    .substringBefore("<START>")
    .substringBefore("<|user|>")
    .substringBefore("<|model|>")

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