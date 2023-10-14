package character

import getImageBitmap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import runCommand
import uniqueName
import java.io.File

class CharacterLoader {
    fun loadAllCharacters(): List<ACharacter>? {
        val allFiles = File("data/characters").listFiles()?.filter { it.extension == "webp" } ?: return null

        return allFiles.mapNotNull { charFile ->
            try {
                val jsonFile = File("data/characters/${charFile.nameWithoutExtension}.json")
                val jsonCharacter = loadCharacterFromJson(jsonFile)
                        ?: loadCharacterFromWebP(charFile).also { jsonFile.writeText(json.encodeToString(it)) }
                        ?: return@mapNotNull null

                val image = getImageBitmap(charFile) ?: return@mapNotNull null

                ACharacter(charFile.nameWithoutExtension, jsonCharacter, image)

            } catch (e: Exception) {
                println("${charFile.name} : ${e.message}")
                null
            }
        }
    }

    fun loadCharacter(file: File): CharacterInfo? {
        if (!file.exists()) return null

        return when (file.extension) {
            "webp" -> loadCharacterFromWebP(file)
            "json" -> loadCharacterFromJson(file)
            else -> null
        }
    }

    private fun loadCharacterFromWebP(file: File): CharacterInfo? {
        if (file.extension != "webp") return null

        return try {
            val exifToolRegex = Regex("^User Comment *: ?")
            val text = "exiftool -UserComment \"${file.path}\""
                .runCommand(File("."))
                ?.replace(exifToolRegex, "") ?: return null

            readJsonCharacter(text)

        } catch (e: Exception) { null }
    }

    private fun loadCharacterFromJson(file: File): CharacterInfo? {
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

    fun createNewCharacter(name: String) = ACharacter(
        uniqueName(name, "webp", File("data/characters")),
        CharacterInfo(name = name),
    ).also { it.save() }


    fun loadNewCharacter(file: File): ACharacter? {
        val loadedCharacterJson = loadCharacter(file) ?: return null

        val characterImage = when(file.extension) {
            "webp" -> getImageBitmap(file)
            else -> getImageBitmap("data/DummyCharacter.webp")
        } ?: return null

        return ACharacter(
            uniqueName(loadedCharacterJson.name, "webp", File("data/characters")),
            loadedCharacterJson,
            characterImage,
        ).also { it.save() }
    }
}