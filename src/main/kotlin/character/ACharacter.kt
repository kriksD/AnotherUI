package character

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import emptyImageBitmap
import getImageBitmap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skiko.toImage
import java.io.File
import java.util.*

data class ACharacter(
    val fileName: String,
    val jsonData: CharacterInfo,
    var image: ImageBitmap = getImageBitmap("data/DummyCharacter.webp") ?: emptyImageBitmap,
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun save() {
        val file = File("data/characters/$fileName.webp")
        val jsonFile = File("data/characters/$fileName.json")

        jsonData.edit_date = Calendar.getInstance().timeInMillis
        val json = Json { explicitNulls = false }
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
    }

    fun saveWithImage() {
        saveImage()
        save()
    }

    private fun saveImage() {
        val data = image.toAwtImage().toImage().encodeToData(EncodedImageFormat.WEBP, 95)
        data?.let {
            File("data/characters/$fileName.webp").writeBytes(it.bytes)
        } ?: run {
            val dummyFile = File("data/DummyCharacter.webp")
            dummyFile.copyTo(File("data/characters/$fileName.webp"), overwrite = true)
        }
    }
}