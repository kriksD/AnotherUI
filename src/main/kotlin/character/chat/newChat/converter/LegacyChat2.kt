package character.chat.newChat.converter

data class LegacyChat2 (
    val fileName: String,
    val characterFileName: String,
    val chatInfo: LegacyChatInfo,
    val messages: MutableList<LegacyMessage2>,
)