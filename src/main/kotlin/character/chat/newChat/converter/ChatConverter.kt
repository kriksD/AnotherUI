package character.chat.newChat.converter

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import character.chat.Chat
import character.chat.Message
import character.chat.newChat.AChat2
import character.chat.newChat.AMessage2
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class ChatConverter {
    fun loadChatFromOtherFormats(file: File): AChat2? {
        if (!file.exists()) return null

        return try {
            when (file.extension) {
                "jsonl" -> loadJson(file)?.let { convertToNew(it) }?.also {
                    file.delete()
                    it.save()
                }
                "zip" -> loadZip(file)?.let { convertToNew(it) }?.also {
                    file.delete()
                    it.save()
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun loadZip(file: File): LegacyChat2? {
        ZipInputStream(FileInputStream(file)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "chat.jsonl") {
                    val jsonLines = zip.readBytes().decodeToString().split("\n")
                    val chatInfo = Json.decodeFromString<LegacyChatInfo>(jsonLines.first())

                    val messages: MutableList<LegacyMessage2> = jsonLines.mapNotNull {
                        parseJsonMessage(it)
                    }.toMutableList()

                    return LegacyChat2(file.nameWithoutExtension, file.parentFile.nameWithoutExtension, chatInfo, messages)
                }

                entry = zip.nextEntry
            }
        }

        return null
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

    fun convertToNew(legacyChat: LegacyChat2): AChat2 {
        return AChat2(
            legacyChat.fileName,
            legacyChat.characterFileName,
            legacyChat.chatInfo.create_date,
            legacyChat.messages.map { it.convertToNew() }.toMutableStateList()
        )
    }

    private fun LegacyMessage2.convertToNew(): AMessage2 {
        return AMessage2(
            this.name,
            this.is_user,
            this.send_date ?: 0,
            mutableStateOf(this.swipe_id ?: 0),
            this.swipes?.ifEmpty { mutableListOf(this.mes) }?.toMutableStateList() ?: mutableStateListOf(this.mes)
        )
    }

    // ===========================================================

    fun convertToNew(legacyChat: Chat): AChat2 {
        return AChat2(
            legacyChat.fileName,
            legacyChat.characterFileName,
            legacyChat.chatInfo.create_date,
            legacyChat.messages.map { it.convertToNew() }.toMutableStateList()
        )
    }

    private fun Message.convertToNew(): AMessage2 {
        return AMessage2(
            this.name,
            this.is_user,
            this.send_date ?: 0,
            mutableStateOf(this.swipe_id ?: 0),
            this.swipes?.ifEmpty { mutableListOf(this.mes) }?.toMutableStateList() ?: mutableStateListOf(this.mes)
        )
    }
}