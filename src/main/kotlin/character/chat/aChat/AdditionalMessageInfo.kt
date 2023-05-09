package character.chat.aChat

import kotlinx.serialization.Serializable

@Serializable
data class AdditionalMessageInfo(
    val images: Map<Int, String>,
)
