package properties

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var name: String = "You",
    var profile_image_file: String = "DummyCharacter.webp",
)
