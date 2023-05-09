package properties

import androidx.compose.ui.graphics.ImageBitmap
import getImageBitmap
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

class UserContainer {
    lateinit var user: User
    private val profileImages = mutableMapOf<String, ImageBitmap>()
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val file = File("data/user/user.json")
    private val profileImagesFolder = File("data/user/profile images")
    private data class ProfileImageData(var name: String?, var image: ImageBitmap?)
    private var currentProfileImage = ProfileImageData(null, null)

    fun getProfileImage(): ImageBitmap? {
        if (currentProfileImage.name != user.profile_image_file) {
            getImageBitmap("${profileImagesFolder.path}/${user.profile_image_file}")?.let { img ->
                currentProfileImage.name = user.profile_image_file
                currentProfileImage.image = img
            }
        }

        return currentProfileImage.image
    }

    fun load() {
        user = try {
            if (file.exists()) {
                json.decodeFromString(file.readText())
            } else {
                User()
            }
        } catch (e: Exception) {
            User()
        }
    }

    fun save() {
        file.writeText(json.encodeToString(user))
    }
}
