package character.chat.newChat

import kotlinx.serialization.Serializable

@Serializable
data class AdditionalMessageInfo2(
    val images: Map<Int, String>,
)
