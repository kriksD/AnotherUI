package character.chat.newChat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import encodeToWebP
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import toStringList
import uniqueName
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Serializable(AChat2Serializer::class)
class AChat2(
    val fileName: String,
    val folderName: String,
    val createDate: Long,
    val messages: SnapshotStateList<AMessage2>,
) {
    private var images: MutableMap<String, ImageBitmap> = mutableMapOf()

    fun addMessage(content: String, name: String, isUser: Boolean): AMessage2 {
        val newMessage = createNewMessage(content, name, isUser)
        messages.add(newMessage)
        return newMessage
    }

    fun createNewMessage(content: String, name: String, isUser: Boolean): AMessage2 = AMessage2(
        name,
        isUser,
        Calendar.getInstance().timeInMillis,
        mutableStateOf(content),
        mutableStateOf(0),
        mutableStateListOf(content),
    )

    fun removeLast() {
        messages.removeLast()
    }

    fun removeAfterInclusively(index: Int) {
        while (messages.lastIndex >= index) {
            removeLast()
        }
    }

    fun clear() {
        val first = messages.first()
        messages.clear()
        messages.add(first)
    }

    fun save() {
        File("data/chats").mkdirs()
        File("data/chats/$folderName").mkdirs()
        updateZip(Json.encodeToString(this))
    }

    private fun updateZip(chatJson: String) {
        val zipFile = File("data/chats/$folderName/$fileName.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            zipFile.inputStream().use { input ->
                val zipInput = ZipInputStream(input)
                var entry = zipInput.nextEntry
                while (entry != null) {
                    if (!entry.name.endsWith(".jsonl") && images[entry.name.removeSuffix(".webp")] != null) {
                        zip.putNextEntry(entry)
                        zipInput.copyTo(zip)
                        zip.closeEntry()
                    }
                    entry = zipInput.nextEntry
                }
            }

            zip.putNextEntry(ZipEntry("chat.jsonl"))
            zip.write(chatJson.toByteArray())
            zip.closeEntry()

            images.forEach { (name, image) ->
                image.encodeToWebP()?.let { imgBytes ->
                    zip.putNextEntry(ZipEntry("$name.webp"))
                    zip.write(imgBytes)
                    zip.closeEntry()
                }
            }
        }
    }

    fun delete() {
        val file = File("data/chats/$folderName/$fileName.zip")

        if (file.exists()) {
            file.delete()
        }
    }

    fun swipeLeft() { messages.last().swipeLeft() }

    fun swipeRight() { messages.last().swipeRight() }

    fun updateSwipe(index: Int, newContent: String) {
        messages.last().updateSwipe(index, newContent)
    }

    fun image(messageIndex: Int, swipeIndex: Int): ImageBitmap? {
        return images[messages[messageIndex].imageName(swipeIndex)]
    }

    fun updateImage(image: ImageBitmap, messageIndex: Int, swipeIndex: Int) {
        if (image(messageIndex, swipeIndex) != null) {
            val name = messages[messageIndex].imageName(swipeIndex)!!
            images[name] = image
            messages[messageIndex].updateImage(name, swipeIndex)

        } else {
            val name = uniqueName("image", images.toStringList())
            images[name] = image
            messages[messageIndex].updateImage(name, swipeIndex)
        }
    }

    fun updateImages(list: Map<String, ImageBitmap>) {
        images = list.toMutableMap()
    }
}