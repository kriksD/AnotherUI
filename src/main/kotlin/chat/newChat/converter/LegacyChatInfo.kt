package chat.newChat.converter

import kotlinx.serialization.Serializable

@Serializable
data class LegacyChatInfo(
    val user_name: String,
    val character_name: String,
    val create_date: Long
)
