package character

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable(with = CharacterMetaDataSerializer::class)
class CharacterMetaData(
    favorite: Boolean = false,
) {
    var favorite: Boolean by mutableStateOf(favorite)

    fun copy(): CharacterMetaData {
        return CharacterMetaData(favorite)
    }
}
