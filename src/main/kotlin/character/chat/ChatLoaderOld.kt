package character.chat

import androidx.compose.ui.graphics.ImageBitmap
import character.ACharacter
import character.chat.aChat.AChat
import character.chat.aChat.AMessage
import character.chat.aChat.AdditionalMessageInfo
import character.chat.legacyChat.LegacyChat
import character.chat.legacyChat.LegacyMessage
import getImageBitmap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.zip.ZipInputStream

class ChatLoaderOld {
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
}