package properties

import androidx.compose.ui.graphics.ImageBitmap
import getImageBitmap
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import properties.settings.preset.Presets
import java.io.File

class UserContainer {
    lateinit var user: User
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    private val file = File("data/user/user.json")
    private val profileImagesFolder = File("data/user/profile images")
    private val presetsFile = File("data/user/presets.json")

    private data class ProfileImageData(var name: String?, var image: ImageBitmap?)
    private var currentProfileImage = ProfileImageData(null, null)

    var presets: Presets<User> = Presets(
        folder = File("data/user/presets"),
        serializer = User.serializer(),
        createValue = { user.copy() },
    )

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
        loadSettings()
        loadPresets()
    }

    private fun loadSettings() {
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

    private fun loadPresets() {
        presets.load()

        if (presetsFile.exists()) {
            try {
                presets.selectPreset(json.decodeFromString<String?>(presetsFile.readText()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        presets.onSelect {
            user = it
        }
    }

    fun save() {
        file.writeText(json.encodeToString(user))
        presetsFile.writeText(json.encodeToString(presets.selectedPresetName))
    }
}
