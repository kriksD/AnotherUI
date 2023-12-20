package client

import character.ACharacter
import prompt.PromptType
import settings
import user

fun String.dropRest(charName: String): String {
    return if (settings.promptSettings.type == PromptType.Chat) {
        replace("$charName:", "")
            .substringBefore("${user.name}:")
            .substringBefore("You:")
            .substringBefore("<START>")
    } else {
        var text = substringBefore("<START>")
        settings.promptSettings.splitStopSequence.forEach { text = text.substringBefore(it) }
        text
    }
}

fun String.format(character: ACharacter): String {
    return this
        .replace("{{char}}", character.jsonData.name, true)
        .replace("<char>", character.jsonData.name, true)
        .replace("{{user}}", user.name, true)
        .replace("<user>", user.name, true)
        .replace("{{user_name}}", user.name, true)
        .replace("<user_name>", user.name, true)
}

fun String.trimCorrectly(): String {
    var result = this

    while (result.first() == '\n' || result.first() == '\t' || result.first() == ' ') result = result.drop(1)
    while (result.last() == '\n' || result.last() == '\t' || result.last() == ' ') result = result.dropLast(1)

    result = result.replace(Regex("(?<!\\n)\\n(?!\\n)"), "\n\n")
    result = result.replace(Regex("\\n{3,}"), "\n\n")

    return result
}