package properties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable(with = UserSerializer::class)
class User(
    name: String = "You",
    description: String? = null,
    profile_image_file: String = "DummyCharacter.webp",
) {
    var name: String by mutableStateOf(name)
    var description: String? by mutableStateOf(description)
    var profile_image_file: String by mutableStateOf(profile_image_file)

    fun copy(): User {
        return User(name, description, profile_image_file)
    }
}
