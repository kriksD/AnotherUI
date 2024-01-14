package character

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import emptyImageBitmap
import getImageBitmap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skiko.toImage
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

class ACharacter(
    val fileName: String,
    val jsonData: CharacterInfo,
    val metaData: CharacterMetaData,
    image: ImageBitmap = getImageBitmap("data/DummyCharacter.webp") ?: emptyImageBitmap,
) {
    var image: ImageBitmap = run {
        /*val newSize = if (image.width / image.height > 400 / 600) {
            calculateScaledDownSize(image.width, image.height, Int.MAX_VALUE, style.image_height * 2)
        } else {
            calculateScaledDownSize(image.width, image.height, style.image_width * 2, Int.MAX_VALUE)
        }

        image.scaleAndCropImage(newSize.first, newSize.second)*/
        image
    }
        set(value) {
            /*val newSize = if (image.width / image.height > 400 / 600) {
                calculateScaledDownSize(image.width, image.height, Int.MAX_VALUE, style.image_height * 2)
            } else {
                calculateScaledDownSize(image.width, image.height, style.image_width * 2, Int.MAX_VALUE)
            }

            field = value.scaleAndCropImage(newSize.first, newSize.second)*/
            field = value
        }

    private fun ImageBitmap.scaleAndCropImage(width: Int, height: Int): ImageBitmap {
        val bufferedImage = this.toAwtImage()

        val outputImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val scaleX = width.toDouble() / bufferedImage.width.toDouble()
        val scaleY = height.toDouble() / bufferedImage.height.toDouble()
        val scale = kotlin.math.max(scaleX, scaleY)

        val scaledWidth = (bufferedImage.width * scale).toInt()
        val scaledHeight = (bufferedImage.height * scale).toInt()

        val scaledImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = scaledImage.createGraphics()
        g2d.drawImage(bufferedImage, 0, 0, scaledWidth, scaledHeight, null)
        g2d.dispose()

        val x = (scaledWidth - width) / 2
        val y = (scaledHeight - height) / 2

        val croppedImage = scaledImage.getSubimage(x, y, width, height)

        val g2dOut = outputImage.createGraphics()
        g2dOut.drawImage(croppedImage, 0, 0, null)
        g2dOut.dispose()

        return outputImage.toComposeImageBitmap()
    }

    private fun calculateScaledDownSize(originalWidth: Int, originalHeight: Int, maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
        val widthRatio = originalWidth.toDouble() / maxWidth.toDouble()
        val heightRatio = originalHeight.toDouble() / maxHeight.toDouble()
        val scaleRatio = maxOf(widthRatio, heightRatio)

        val scaledWidth = (originalWidth.toDouble() / scaleRatio).toInt()
        val scaledHeight = (originalHeight.toDouble() / scaleRatio).toInt()

        return Pair(scaledWidth, scaledHeight)
    }


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
        saveMeta()
    }

    fun saveWithImage(image: ImageBitmap? = null) {
        saveImage(image)
        save()
    }

    private fun saveImage(image: ImageBitmap? = null) {
        val imageToSave = image ?: this.image
        val data = imageToSave.toAwtImage().toImage().encodeToData(EncodedImageFormat.WEBP, 95)
        data?.let {
            File("data/characters/$fileName.webp").writeBytes(it.bytes)
        } ?: run {
            val dummyFile = File("data/DummyCharacter.webp")
            dummyFile.copyTo(File("data/characters/$fileName.webp"), overwrite = true)
        }
    }

    fun saveMeta() {
        val metaFile = File("data/characters/${fileName}_meta.json")
        metaFile.writeText(json.encodeToString(metaData))
    }

    fun delete() {
        File("data/characters/$fileName.webp").delete()
        File("data/characters/$fileName.json").delete()
        File("data/characters/${fileName}_meta.json").delete()
    }
}