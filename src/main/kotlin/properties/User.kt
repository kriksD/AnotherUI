package properties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable(with = UserSerializer::class)
class User(
    name: String = "You",
    description: String = "",
    profileImageFile: String = "DummyCharacter.webp",
) {
    var name: String by mutableStateOf(name)
    var description: String by mutableStateOf(description)
    var profileImageFile: String by mutableStateOf(profileImageFile)

    fun copy(): User {
        return User(name, description, profileImageFile)
    }
}
