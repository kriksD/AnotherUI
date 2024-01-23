package chat

import androidx.compose.runtime.*
import character.ACharacter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.File
import java.util.*

class ChatLoader {
    private val chatList = mutableStateListOf<Chat>()
    val chats get() = chatList.toList()
    var selected: Chat? by mutableStateOf(null)
        private set

    fun load(character: ACharacter) {
        chatList.clear()
        selected = null

        val folder = File("data/chats/${character.fileName}")
        if (!folder.exists()) return

        folder.listFiles()?.forEach { file ->
            chatList.add(loadJson(file) ?: return@forEach)
        }

        selected = chatList.find { it.fileName == character.jsonData.chat } ?: chatList.lastOrNull()
    }

    private fun loadJson(file: File): Chat? {
        return try {
            if (!file.exists()) return null
            val jsonText = file.readText()
            return Json.decodeFromString<Chat>(jsonText)

        } catch (e: Exception) {
            null
        }
    }

    fun createChat(character: ACharacter): Chat {
        val dateCreate = Calendar.getInstance().timeInMillis
        val newChat = Chat(
            dateCreate.toString(),
            character.fileName,
            dateCreate,
            mutableStateListOf(
                Message(
                    character.jsonData.name,
                    false,
                    dateCreate,
                    mutableStateOf(0),
                    (listOf(character.jsonData.firstMessage) + character.jsonData.alternateGreetings).toMutableStateList(),
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

    fun removeChat(chat: Chat) {
        if (selected == chat) {
            selected = null
        }
        chatList.remove(chat)
    }

    fun selectChat(chat: Chat, character: ACharacter) {
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