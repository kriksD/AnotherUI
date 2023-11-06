package client

import character.ACharacter
import user

fun String.dropRest(charName: String): String {
    return if ("chat" == "chat") {
        replace("$charName:", "")
            .substringBefore("${user.name}:")
            .substringBefore("You:")
            .substringBefore("<START>")
    } else {
        substringBefore("<START>")
            .substringBefore("<|user|>")
            .substringBefore("<|model|>")
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