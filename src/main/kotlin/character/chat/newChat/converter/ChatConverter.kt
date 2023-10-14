package character.chat.newChat.converter

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import character.chat.newChat.AChat2
import character.chat.newChat.AMessage2
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.File

class ChatConverter {
    fun loadChatFromOtherFormats(file: File): AChat2? {
        if (!file.exists()) return null

        return when (file.extension) {
            "jsonl" -> loadJson(file)?.convertToNew()
            else -> null
        }
    }

    private fun loadJson(file: File): LegacyChat2? {
        return try {
            val jsonLines = file.readText().split("\n")
            val chatInfo = Json.decodeFromString<LegacyChatInfo>(jsonLines.first())

            val messages: MutableList<LegacyMessage2> = jsonLines.mapNotNull {
                parseJsonMessage(it)
            }.toMutableList()

            LegacyChat2(file.nameWithoutExtension, file.parentFile.nameWithoutExtension, chatInfo, messages)

        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonMessage(text: String): LegacyMessage2? {
        return try {
            val map = Json.parseToJsonElement(text).jsonObject.toMap()

            val swipes = map["swipes"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.content
            }?.toMutableList()

            LegacyMessage2(
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

    private fun LegacyChat2.convertToNew(): AChat2 {
        return AChat2(
            this.fileName,
            this.characterFileName,
            this.chatInfo.create_date,
            this.messages.map { it.convertToNew() }.toMutableStateList()
        )
    }

    private fun LegacyMessage2.convertToNew(): AMessage2 {
        return AMessage2(
            this.name,
            this.is_user,
            this.send_date ?: 0,
            mutableStateOf(this.mes),
            mutableStateOf(this.swipe_id ?: 0),
            this.swipes?.toMutableStateList() ?: mutableStateListOf()
        )
    }
}