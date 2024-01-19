package chat.legacyChat

import androidx.compose.ui.graphics.ImageBitmap
import chat.Chat
import chat.ChatInfo
import chat.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

data class LegacyChat(
    override val fileName: String,
    override val characterFileName: String,
    override val chatInfo: ChatInfo,
    override val messages: MutableList<Message>,
) : Chat {
    override fun createNewMessage(text: String, is_user: Boolean, withSwipes: Boolean) = LegacyMessage(
        if (is_user) chatInfo.user_name else chatInfo.character_name,
        is_user,
        true,
        Calendar.getInstance().timeInMillis,
        text,
        null,
        if (withSwipes) 0 else null,
        if (withSwipes) mutableListOf(text) else null,
    )

    override fun save() {
        var jsonText = ""
        jsonText += Json.encodeToString(chatInfo) + "\n"

        for (mes in messages) {
            jsonText += mes.toJSON() + "\n"
        }
        jsonText = jsonText.dropLast(1)
        File("data/chats/$characterFileName").mkdir()
        File("data/chats/$characterFileName/$fileName.jsonl").writeText(jsonText)
    }

    override fun delete() {
        val file = File("data/chats/$characterFileName/$fileName.jsonl")

        if (file.exists()) {
            file.delete()
        }
    }
}
