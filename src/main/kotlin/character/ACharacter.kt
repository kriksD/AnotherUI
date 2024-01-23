package character

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import getImageBitmap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skiko.toImage
import java.io.File
import java.util.*

class ACharacter(
    val fileName: String,
    val jsonData: CharacterInfo,
    val metaData: CharacterMetaData,
    image: ImageBitmap? = null,
) {
    var image: ImageBitmap? by mutableStateOf(image)

    @OptIn(ExperimentalSerializationApi::class)
    fun save() {
        val file = File("data/characters/$fileName.webp")
        val jsonFile = File("data/characters/$fileName.json")

        jsonData.editDate = Calendar.getInstance().timeInMillis
        val json = Json {
            prettyPrint = true
            explicitNulls = false
        }
        val jsonNormal = json.encodeToString(jsonData)
        val jsonForWebP = jsonNormal
            .replace("\"", "\\\"")
            .replace("\\\\\"", "\\\\\\\"")

        if (file.exists()) {
            //"exiftool -UserComment=\"$jsonForWebP\" \"${file.path}\"".runCommand(File("."))
            //File("data/characters/$fileName.webp_original").delete()

        } else {
            saveWithImage()
        }

        jsonFile.writeText(jsonNormal)
        saveMeta()
    }

    fun saveWithImage(image: ImageBitmap? = null) {
        saveImage(image)
        save()
    }

    private fun saveImage(image: ImageBitmap? = null) {
        val imageToSave = image ?: this.image ?: run {
            val dummyFile = File("data/DummyCharacter.webp")
            dummyFile.copyTo(File("data/characters/$fileName.webp"), overwrite = true)
            loadImage()

            return
        }

        val data = imageToSave.toAwtImage().toImage().encodeToData(EncodedImageFormat.WEBP, 95)
        data?.let {
            File("data/characters/$fileName.webp").writeBytes(it.bytes)
        }
    }

    fun saveMeta() {
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        val metaFile = File("data/characters/${fileName}_meta.json")
        metaFile.writeText(json.encodeToString(metaData))
    }

    fun delete() {
        File("data/characters/$fileName.webp").delete()
        File("data/characters/$fileName.json").delete()
        File("data/characters/${fileName}_meta.json").delete()
    }

    fun loadImage() {
        if (image != null) return
        image = getImageBitmap("data/characters/$fileName.webp")
    }

    fun unloadImage() {
        image = null
    }
}