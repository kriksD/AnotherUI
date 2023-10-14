package character.chat.newChat

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import character.ACharacter
import character.chat.newChat.converter.ChatConverter
import getImageBitmap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.zip.ZipInputStream

class ChatLoader {
    private val converter: ChatConverter = ChatConverter()

    private val chatList = mutableStateListOf<AChat2>()
    val chats get() = chatList.toList()
    var selected: AChat2? by mutableStateOf(null)
        private set

    fun load(character: ACharacter) {
        chatList.clear()
        selected = null

        val folder = File("data/chats/${character.fileName}")
        if (!folder.exists()) return

        folder.listFiles()?.forEach { file ->
            chatList.add(loadZip(file) ?: converter.loadChatFromOtherFormats(file) ?: return@forEach)
        }

        selected = chatList.find { it.fileName == character.jsonData.chat } ?: chatList.lastOrNull()
    }

    private fun loadZip(file: File): AChat2? {
        return try {
            var jsonText = ""
            var images: MutableMap<String, ImageBitmap> = mutableMapOf()

            loadFilesFromZip(
                file,
                whenChat = { jsonText = it },
                whenImages = { images = it.toMutableMap() },
            )

            return Json.decodeFromString<AChat2>(jsonText).also { it.updateImages(images) }

        } catch (e: Exception) {
            null
        }
    }

    private fun loadFilesFromZip(
        file: File,
        whenChat: (String) -> Unit = {},
        whenImages: (Map<String, ImageBitmap>) -> Unit = {},
    ) {
        val images = mutableMapOf<String, ImageBitmap>()
        ZipInputStream(FileInputStream(file)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "chat.jsonl" -> whenChat(zip.readBytes().decodeToString())
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

    fun saveSelected() {
        selected?.save()
    }

    fun createChat(character: ACharacter): AChat2 {
        val dateCreate = Calendar.getInstance().timeInMillis
        val newChat = AChat2(
            dateCreate.toString(),
            character.fileName,
            dateCreate,
            mutableStateListOf(
                AMessage2(
                    character.jsonData.name,
                    false,
                    dateCreate,
                    mutableStateOf(character.jsonData.first_mes),
                    mutableStateOf(0),
                    mutableStateListOf(character.jsonData.first_mes),
                )
            ),
        )

        chatList.add(newChat)
        selected = newChat
        character.jsonData.chat = newChat.fileName
        character.save()
        return newChat
    }

    fun removeChat(chat: AChat2) {
        chatList.remove(chat)
    }

    fun selectChat(chat: AChat2, character: ACharacter) {
        selected = chat
        character.jsonData.chat = chat.fileName
        character.save()
    }
}