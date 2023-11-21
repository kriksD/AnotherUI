package character.chat.newChat

import androidx.compose.runtime.*
import character.ACharacter
import character.chat.newChat.converter.ChatConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.File
import java.util.*

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
            chatList.add(loadJson(file) ?: converter.loadChatFromOtherFormats(file) ?: return@forEach)
        }

        selected = chatList.find { it.fileName == character.jsonData.chat } ?: chatList.lastOrNull()
    }

    private fun loadJson(file: File): AChat2? {
        return try {
            if (!file.exists()) return null
            val jsonText = file.readText()
            return Json.decodeFromString<AChat2>(jsonText)

        } catch (e: Exception) {
            null
        }
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
                    mutableStateOf(0),
                    (listOf(character.jsonData.first_mes) + character.jsonData.alternate_greetings).toMutableStateList(),
                )
            ),
        )

        chatList.add(newChat)
        selected = newChat
        character.jsonData.chat = newChat.fileName
        character.save()
        newChat.save()
        return newChat
    }

    fun removeChat(chat: AChat2) {
        if (selected == chat) {
            selected = null
        }
        chatList.remove(chat)
    }

    fun selectChat(chat: AChat2, character: ACharacter) {
        selected = chat
        character.jsonData.chat = chat.fileName
        character.save()
    }

    fun selectIfNotSelected(character: ACharacter) {
        if (selected == null) {
            selected = chatList.lastOrNull()
        }

        if (selected == null) {
            createChat(character)
        }

        selected?.let {
            character.jsonData.chat = it.fileName
            character.save()
        }
    }
}