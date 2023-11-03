package character.chat.newChat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import saveWebPTo
import uniqueNameIncludingZero
import java.io.File
import java.util.*

@Serializable(AChat2Serializer::class)
class AChat2(
    val fileName: String,
    val folderName: String,
    val createDate: Long,
    val messages: SnapshotStateList<AMessage2>,
) {
    fun addMessage(content: String, name: String, isUser: Boolean): AMessage2 {
        val newMessage = createNewMessage(content, name, isUser)
        messages.add(newMessage)
        return newMessage
    }

    private fun createNewMessage(content: String, name: String, isUser: Boolean): AMessage2 = AMessage2(
        name,
        isUser,
        Calendar.getInstance().timeInMillis,
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

    private val json = Json { prettyPrint = true }

    fun save() {
        File("data/chats/$folderName").mkdirs()
        val chatJson = json.encodeToString(this)
        val jsonFile = File("data/chats/$folderName/$fileName.json")
        jsonFile.writeText(chatJson)
    }

    fun delete() {
        messages.forEach { message ->
            message.images.values.forEach {
                val file = File(it)
                if (file.exists()) file.delete()
            }
        }

        val file = File("data/chats/$folderName/$fileName.json")
        if (file.exists()) file.delete()
    }

    fun saveImage(image: ImageBitmap): String {
        val folder = File("data/chats/$folderName/images/${fileName}")
        folder.mkdirs()
        val imageFileName = uniqueNameIncludingZero("", "webp", folder)
        val file = File("data/chats/$folderName/images/${fileName}/${imageFileName}.webp")
        image.saveWebPTo(file)
        return file.path
    }
}