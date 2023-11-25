package character

import kotlinx.serialization.Serializable

@Serializable
data class CharacterMetaData(
    var favorite: Boolean = false,
)
