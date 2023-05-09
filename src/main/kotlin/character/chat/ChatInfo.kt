package character.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatInfo(
    val user_name: String,
    val character_name: String,
    val create_date: Long
)
