import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.currentNanoTime
import org.jetbrains.skiko.toImage
import properties.*
import properties.Properties
import properties.settings.Settings
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/* -= shortcuts =- */
val user: User get() = Properties.user()
val lang: Language get() = Properties.language()
val settings: Settings get() = Properties.settings()
val style: Style get() = Properties.style()


/* -= image functions =- */
val emptyImageBitmap: ImageBitmap = ImageBitmap(1, 1)

fun getImageBitmap(imagePath: String): ImageBitmap? {
    return if (File(imagePath).exists())
        Image.makeFromEncoded(File(imagePath).readBytes()).toComposeImageBitmap()
    else
        null
}

fun getImageBitmap(imageFile: File): ImageBitmap? {
    return if (imageFile.exists())
        Image.makeFromEncoded(imageFile.readBytes()).toComposeImageBitmap()
    else
        null
}

fun getImageBitmap(imageBytes: ByteArray): ImageBitmap = Image.makeFromEncoded(imageBytes).toComposeImageBitmap()

fun ImageBitmap.saveWebPTo(file: File) {
    val data = this.toAwtImage().toImage().encodeToData(EncodedImageFormat.WEBP, 95)
    data?.let { file.writeBytes(it.bytes) }
}

fun ImageBitmap.saveWebPTo(path: String) {
    saveWebPTo(File(path))
}

fun ImageBitmap.encodeToWebP(): ByteArray? {
    return this.toAwtImage().toImage().encodeToData(EncodedImageFormat.WEBP, 95)?.bytes
}

fun ImageBitmap.scaleAndCropImage(width: Int, height: Int): ImageBitmap {
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

fun File.loadAllImages(): Map<String, ImageBitmap> {
    val files = this.listFiles() ?: return mapOf()

    val list = mutableMapOf<String, ImageBitmap>()
    for (f in files) {
        try {
            list[f.name] = getImageBitmap(f) ?: continue
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return list
}

fun copyAndGetImage(file: File, to: File): Pair<String, ImageBitmap>? {
    getImageBitmap(file)?.let { img ->
        val name = uniqueName(file.nameWithoutExtension, file.extension, to)
        file.copyTo(File("${to.path}/$name.${file.extension}"))
        return Pair("$name.${file.extension}", img)

    } ?: return null
}


/* -= additional functions =- */
fun Int.roundToStep(step: Int): Int {
    val steps = this / step
    val lastStep = this % step
    val centerOfStep = step.toFloat() / 2F

    return (steps + if (lastStep > centerOfStep) 1 else 0) * step
}

fun Long.toTimeString(): String {
    val date = Date(this)
    val format = SimpleDateFormat("hh:mma dd/MM/yyyy")
    return format.format(date)
}

fun decodeAllUTF(string: String): String {
    var newString = string
    val matches = Regex("\\\\u[0-9a-z]{4}").findAll(string)

    matches.forEach {
        val char = Integer.parseInt(it.value.drop(2), 16)
        newString = newString.replace(it.value, Char(char).toString())
    }

    return newString
}

fun String.runCommand(workingDir: File): String? {
    return try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        //proc.waitFor(60, TimeUnit.MINUTES)
        proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

/**
 * @param baseName base name of the file (Example: "name")
 * @param extension extension of the file (Example: "webp")
 * @param folder what folder create unique name for
 *
 * @return unique name without extension
 */
fun uniqueName(baseName: String, extension: String, folder: File): String {
    var i = 0
    while (File("${folder.absolutePath}/${baseName}${if (i > 0) "_" else ""}${if (i > 0) i else ""}.$extension").exists()) { i++ }
    return "${baseName}${if (i > 0) "_" else ""}${if (i > 0) i else ""}"
}

fun uniqueName(name: String, list: List<String>): String {
    var i = 0
    while ("${name}${if (i > 0) "_" else ""}${if (i > 0) i else ""}" in list) { i++ }
    return "${name}${if (i > 0) "_" else ""}${if (i > 0) i else ""}"
}

/**
 * @param baseName base name of the file (Example: "name")
 * @param extension extension of the file (Example: "webp")
 * @param folder what folder create unique name for
 *
 * @return unique name without extension
 */
fun uniqueNameIncludingZero(baseName: String, extension: String, folder: File): String {
    var i = 0
    while (File("${folder.absolutePath}/${baseName}${if (i > 0) "_" else ""}$i.$extension").exists()) { i++ }
    return "${baseName}${if (i > 0) "_" else ""}$i"
}

fun uniqueNameIncludingZero(name: String, list: List<String>): String {
    var i = 0
    while ("${name}${if (i > 0) "_" else ""}$i" in list) { i++ }
    return "${name}${if (i > 0) "_" else ""}$i"
}

fun String.toFileName(): String = this
    .replace(" ", "_")
    .replace("<", "")
    .replace(">", "")
    .replace(":", "")
    .replace("\"", "")
    .replace("/", "")
    .replace("\\", "")
    .replace("|", "")
    .replace("?", "")
    .replace("*", "")

fun <K, V> Map<K, V>.toState(): SnapshotStateMap<K, V> {
    val newList = mutableStateMapOf<K, V>()
    forEach {
        newList[it.key] = it.value
    }
    return newList
}

fun Map<String, *>.toStringList(): List<String> {
    val list = mutableListOf<String>()
    forEach { (string, _) -> list.add(string) }
    return list
}

fun <T> List<T>.randomWithAmount(amount: Int): List<T> {
    val list = this.toMutableList()
    val newList = mutableListOf<T>()

    repeat(amount) {
        if (list.isEmpty()) return newList

        val index = Random(currentNanoTime()).nextInt(0, list.size)
        newList.add(list[index])
        list.removeAt(index)
    }

    return newList
}