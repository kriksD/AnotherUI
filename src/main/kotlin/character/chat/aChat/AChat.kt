package character.chat.aChat

import androidx.compose.ui.graphics.ImageBitmap
import character.chat.Chat
import character.chat.ChatInfo
import character.chat.Message
import encodeToWebP
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

data class AChat(
    override val fileName: String,
    override val characterFileName: String,
    override val chatInfo: ChatInfo,
    override val messages: MutableList<Message>,
) : Chat {
    private var images: MutableMap<String, ImageBitmap> = mutableMapOf()

    override fun addMessage(text: String, is_user: Boolean): Message {
        val last = messages.lastOrNull()
        last?.removeRestImages()?.forEach {
            images.remove(it)
        }

        return super.addMessage(text, is_user)
    }

    override fun createNewMessage(text: String, is_user: Boolean, withSwipes: Boolean) = AMessage(
        if (is_user) chatInfo.user_name else chatInfo.character_name,
        is_user,
        true,
        Calendar.getInstance().timeInMillis,
        text,
        null,
        if (withSwipes) 0 else null,
        if (withSwipes) mutableListOf(text) else null,
    )

    override fun removeLast() {
        messages.last().imageNames().forEach { name ->
            images.remove(name)
        }
        super.removeLast()
    }

    override fun save() {
        var basicInfo = ""
        var additionalInfo = ""
        basicInfo += Json.encodeToString(chatInfo) + "\n"

        for (mes in messages) {
            basicInfo += mes.toJSON() + "\n"
            additionalInfo += mes.additionalInfoToJson() + "\n"
        }

        basicInfo = basicInfo.dropLast(1)
        additionalInfo = additionalInfo.dropLast(1)
        File("data/chats").mkdir()
        File("data/chats/$characterFileName").mkdir()
        updateZip(basicInfo, additionalInfo)
    }

    private fun updateZip(basicInfo: String, additionalInfo: String) {
        val zipFile = File("data/chats/$characterFileName/$fileName.zip")

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
            zip.write(basicInfo.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("additional.jsonl"))
            zip.write(additionalInfo.toByteArray())
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

    override fun delete() {
        val file = File("data/chats/$characterFileName/$fileName.zip")

        if (file.exists()) {
            file.delete()
        }
    }

    override fun image(messageIndex: Int, swipeIndex: Int): ImageBitmap? {
        return images[messages[messageIndex].imageName(swipeIndex)]
    }

    override fun updateImage(image: ImageBitmap, messageIndex: Int, swipeIndex: Int) {
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

    override fun updateImages(list: Map<String, ImageBitmap>) {
        images = list.toMutableMap()
    }
}
