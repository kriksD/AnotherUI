package character

import androidx.compose.runtime.*
import client.utilities.ImageLoaderClient
import getImageBitmap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import runCommand
import toFileName
import uniqueName
import java.io.File

class CharacterLoader {
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val characterList = mutableStateListOf<ACharacter>()
    val list get() = characterList.toList()
    var selected: ACharacter? by mutableStateOf(null)

    fun load() {
        characterList.clear()
        selected = null

        val allFiles = File("data/characters").listFiles()?.filter { it.extension == "webp" } ?: return

        allFiles.mapNotNull { charFile ->
            try {
                val jsonFile = File("data/characters/${charFile.nameWithoutExtension}.json")
                val jsonCharacter =
                    loadCharacterFromJson(jsonFile)
                        ?: loadCharacterFromWebP(charFile).also { jsonFile.writeText(json.encodeToString(it)) }
                        ?: return@mapNotNull null

                val metaFile = File("data/characters/${charFile.nameWithoutExtension}_meta.json")
                val metaData = readCharacterMetaData(metaFile)

                characterList.add(
                    ACharacter(
                        charFile.nameWithoutExtension,
                        jsonCharacter,
                        metaData,
                    )
                )

            } catch (e: Exception) {
                println("${charFile.name} : ${e.message}")
                null
            }
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
            json.decodeFromString<CharacterInfo>(text)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun readCharacterMetaData(file: File): CharacterMetaData {
        if (!file.exists() || file.extension != "json") return CharacterMetaData()

        return json.decodeFromString<CharacterMetaData>(file.readText())
    }

    fun create(name: String) {
        val fileName = name.toFileName()

        val newCharacter = ACharacter(
            uniqueName(fileName, "webp", File("data/characters")),
            CharacterInfo(name = name),
            CharacterMetaData(),
        )

        characterList.add(0, newCharacter)
        newCharacter.save()
    }

    suspend fun add(file: File) {
        val exception = Exception("Failed to load character from $file")

        val loadedCharacterJson = loadCharacterFromWebP(file)
            ?: loadCharacterFromJson(file)
            ?: throw exception

        val characterImage = if (file.extension == "webp") {
            getImageBitmap(file) ?: throw exception

        } else if (loadedCharacterJson.avatar != "none" && loadedCharacterJson.avatar.isNotBlank()) {
            val imageLoader = ImageLoaderClient()

            val image = imageLoader.loadImageBitmap(loadedCharacterJson.avatar)
            image ?: getImageBitmap("data/DummyCharacter.webp") ?: throw exception

        } else {
            getImageBitmap("data/DummyCharacter.webp") ?: throw exception
        }

        val newCharacter = ACharacter(
            uniqueName(loadedCharacterJson.name.toFileName(), "webp", File("data/characters")),
            loadedCharacterJson,
            CharacterMetaData(),
            characterImage,
        )

        characterList.add(0, newCharacter)
        newCharacter.save()
    }

    fun add(character: ACharacter) {
        characterList.add(0, character)
        character.save()
    }

    fun remove(character: ACharacter, deleteChats: Boolean = false) {
        if (deleteChats) {
            File("data/chats/${character.fileName}").deleteRecursively()
        }

        characterList.remove(character)
        character.delete()
    }
}