package character

import androidx.compose.ui.graphics.ImageBitmap
import character.chat.aChat.AChat
import character.chat.Chat
import character.chat.Message
import character.chat.legacyChat.LegacyChat
import character.chat.ChatInfo
import character.chat.aChat.AMessage
import character.chat.aChat.AdditionalMessageInfo
import character.chat.legacyChat.LegacyMessage
import client.utilities.ImageLoaderClient
import getImageBitmap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import runCommand
import uniqueName
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.zip.ZipInputStream

val json = Json {
    prettyPrint = true
}

fun loadAllCharacters(): List<ACharacter>? {
    val allFiles = File("data/characters").listFiles()?.filter { it.extension == "webp" } ?: return null

    return allFiles.mapNotNull { charFile ->
        try {
            val jsonFile = File("data/characters/${charFile.nameWithoutExtension}.json")
            val jsonCharacter =
                loadCharacterFromJson(jsonFile)
                ?: loadCharacterFromWebP(charFile).also { jsonFile.writeText(json.encodeToString(it)) }
                ?: return@mapNotNull null

            val metaFile = File("data/characters/${charFile.nameWithoutExtension}_meta.json")
            val metaData = readCharacterMetaData(metaFile)

            val image = getImageBitmap(charFile) ?: return@mapNotNull null

            ACharacter(charFile.nameWithoutExtension, jsonCharacter, metaData, image)

        } catch (e: Exception) {
            println("${charFile.name} : ${e.message}")
            null
        }
    }
}

fun loadCharacterFromWebP(file: File): CharacterInfo? {
    if (file.extension != "webp") return null

    return try {
        val exifToolRegex = Regex("^User Comment *: ?")
        val text = "exiftool -UserComment \"${file.path}\""
            .runCommand(File("."))
            ?.replace(exifToolRegex, "") ?: return null

        readJsonCharacter(text)

    } catch (e: Exception) { null }
}

fun loadCharacterFromJson(file: File): CharacterInfo? {
    if (!file.exists() || file.extension != "json") return null
    return readJsonCharacter(file.readText())
}

private fun readJsonCharacter(text: String): CharacterInfo? {
    return try {
        val map = Json.parseToJsonElement(text).jsonObject.toMap()

        CharacterInfo(
            map["name"]?.jsonPrimitive?.content ?: return null,
            map["description"]?.jsonPrimitive?.content ?: "",
            map["personality"]?.jsonPrimitive?.content ?: "",
            map["first_mes"]?.jsonPrimitive?.content ?: "",
            map["alternate_greetings"]?.jsonArray?.map { it.jsonPrimitive.content }?.toMutableList() ?: mutableListOf(),
            map["avatar"]?.jsonPrimitive?.content ?: "none",
            map["chat"]?.jsonPrimitive?.content ?: "",
            map["mes_example"]?.jsonPrimitive?.content ?: "",
            map["scenario"]?.jsonPrimitive?.content ?: "",
            map["edit_date"]?.jsonPrimitive?.longOrNull,
            map["create_date"]?.jsonPrimitive?.longOrNull,
            map["add_date"]?.jsonPrimitive?.longOrNull,
        )

    } catch (e: Exception) {
        null
    }
}

private fun readCharacterMetaData(file: File): CharacterMetaData {
    if (!file.exists() || file.extension != "json") return CharacterMetaData()

    val text = file.readText()
    val map = Json.parseToJsonElement(text).jsonObject.toMap()

    return CharacterMetaData(
        map["favorite"]?.jsonPrimitive?.booleanOrNull ?: false
    )
}

fun loadAllChats(file: File): List<Chat>? {
    if (!file.exists()) return null
    val files = file.listFiles() ?: return null
    return files.mapNotNull { loadChat(it) }
}

fun loadChat(file: File): Chat? {
    if (!file.exists()) return null

    return when (file.extension) {
        "jsonl" -> loadJson(file)
        "zip" -> loadZip(file)
        else -> null
    }
}

private fun loadJson(file: File): LegacyChat? {
    return try {
        val jsonLines = file.readText().split("\n")
        val chatInfo = Json.decodeFromString<ChatInfo>(jsonLines.first())

        val messages: MutableList<Message> = jsonLines.mapNotNull {
            parseJsonMessage(it)
        }.toMutableList()

        LegacyChat(file.nameWithoutExtension, file.parentFile.nameWithoutExtension, chatInfo, messages)

    } catch (e: Exception) {
        null
    }
}

private fun parseJsonMessage(text: String): LegacyMessage? {
    return try {
        val map = Json.parseToJsonElement(text).jsonObject.toMap()

        val swipes = map["swipes"]?.jsonArray?.mapNotNull {
            it.jsonPrimitive.content
        }?.toMutableList()

        LegacyMessage(
            map["name"]?.jsonPrimitive?.content ?: return null,
            map["is_user"]?.jsonPrimitive?.booleanOrNull ?: return null,
            map["is_name"]?.jsonPrimitive?.booleanOrNull ?: true,
            map["send_date"]?.jsonPrimitive?.longOrNull,
            map["mes"]?.jsonPrimitive?.content ?: "",
            map["chid"]?.jsonPrimitive?.intOrNull,
            map["swipe_id"]?.jsonPrimitive?.intOrNull,
            swipes,
        )
    } catch (e: Exception) {
        null
    }
}

private fun loadZip(file: File): AChat? {
    return try {
        val jsonLines = mutableListOf<String>()
        val additionalJsonLines = mutableListOf<String>()
        var images: MutableMap<String, ImageBitmap> = mutableMapOf()

        loadFilesFromZip(
            file,
            whenAdditional = { additionalJsonLines.addAll(it) },
            whenChat = { jsonLines.addAll(it) },
            whenImages = { images = it.toMutableMap() },
        )

        val chatInfo = Json.decodeFromString<ChatInfo>(jsonLines.first())
        val messages = jsonLines.mapIndexedNotNull { i, jl ->
            if (i == 0) return@mapIndexedNotNull null
            val ajl = additionalJsonLines.getOrNull(i - 1) ?: return@mapIndexedNotNull null
            parseZipMessage(jl, ajl)
        }

        AChat(
            file.nameWithoutExtension,
            file.parentFile.nameWithoutExtension,
            chatInfo,
            messages.toMutableList()
        ).also { it.updateImages(images) }

    } catch (e: Exception) {
        null
    }
}

private fun loadFilesFromZip(
    file: File,
    whenAdditional: (List<String>) -> Unit = {},
    whenChat: (List<String>) -> Unit = {},
    whenImages: (Map<String, ImageBitmap>) -> Unit = {},
) {
    val images = mutableMapOf<String, ImageBitmap>()
    ZipInputStream(FileInputStream(file)).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            when (entry.name) {
                "additional.jsonl" -> whenAdditional.invoke(zip.readBytes().decodeToString().split("\n"))
                "chat.jsonl" -> whenChat.invoke(zip.readBytes().decodeToString().split("\n"))
                else -> {
                    if (entry.name.endsWith(".webp")) {
                        images[entry.name.removeSuffix(".webp")] = getImageBitmap(zip.readBytes())
                    }
                }
            }

            entry = zip.nextEntry
        }
    }
    whenImages.invoke(images)
}

private fun parseZipMessage(text: String, additional: String): AMessage? {
    return try {
        val map = Json.parseToJsonElement(text).jsonObject.toMap()

        var swipes: MutableList<String>? = mutableListOf()
        map["swipes"]?.jsonArray?.forEach {
            swipes?.add(it.jsonPrimitive.content)
        } ?: run { swipes = null }

        AMessage(
            map["name"]?.jsonPrimitive?.content ?: return null,
            map["is_user"]?.jsonPrimitive?.booleanOrNull ?: return null,
            map["is_name"]?.jsonPrimitive?.booleanOrNull ?: true,
            map["send_date"]?.jsonPrimitive?.longOrNull,
            map["mes"]?.jsonPrimitive?.content ?: "",
            map["chid"]?.jsonPrimitive?.intOrNull,
            map["swipe_id"]?.jsonPrimitive?.intOrNull,
            swipes,
            parseZipAdditional(additional) ?: AdditionalMessageInfo(emptyMap())
        )

    } catch (e: Exception) {
        null
    }
}

private fun parseZipAdditional(text: String): AdditionalMessageInfo? {
    return try {
        return Json.decodeFromString<AdditionalMessageInfo>(text)
    } catch (e: Exception) {
        null
    }
}

fun createNewLegacyChat(character: ACharacter): LegacyChat {
    val chatInfo = ChatInfo("You", character.jsonData.name, Calendar.getInstance().timeInMillis)
    val newChat = LegacyChat(chatInfo.create_date.toString(), character.fileName, chatInfo, mutableListOf())
    newChat.addMessage(character.jsonData.first_mes)
    character.jsonData.chat = newChat.fileName

    return newChat
}

fun createNewChat(character: ACharacter): AChat {
    val chatInfo = ChatInfo("You", character.jsonData.name, Calendar.getInstance().timeInMillis)
    val newChat = AChat(chatInfo.create_date.toString(), character.fileName, chatInfo, mutableListOf())
    newChat.addMessage(character.jsonData.first_mes)
    character.jsonData.chat = newChat.fileName

    return newChat
}

fun createNewCharacter(name: String) = ACharacter(
    uniqueName(name, "webp", File("data/characters")),
    CharacterInfo(name = name),
    CharacterMetaData(),
).also { it.save() }


suspend fun loadNewCharacter(file: File): ACharacter? {
    val loadedCharacterJson = loadCharacterFromWebP(file)
        ?: loadCharacterFromJson(file)
        ?: return null

    val characterImage = if (file.extension == "webp") {
        getImageBitmap(file) ?: return null

    } else if (loadedCharacterJson.avatar != "none" || loadedCharacterJson.avatar.isNotBlank()) {
        val imageLoader = ImageLoaderClient()

        val image = imageLoader.loadImageBitmap(loadedCharacterJson.avatar)
        image ?: getImageBitmap("data/DummyCharacter.webp") ?: return null

    } else {
        getImageBitmap("data/DummyCharacter.webp") ?: return null
    }

    return ACharacter(
        uniqueName(loadedCharacterJson.name, "webp", File("data/characters")),
        loadedCharacterJson,
        CharacterMetaData(),
        characterImage,
    ).also { it.save() }
}