package chat

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import getImageBitmap
import kotlinx.serialization.Serializable
import settings
import user
import java.io.File

@Serializable(MessageSerializer::class)
class Message(
    var name: String,
    var isUser: Boolean,
    val sendDate: Long,
    var swipeId: MutableState<Int>,
    var swipes: SnapshotStateList<String>,
    var images: MutableMap<Int, String> = mutableMapOf(),
) {
    val content get() = swipes[swipeId.value]

    val string: String get() = "${if (isUser) user.name else name}: $content"

    val stringInstruct: String get() = run {
        val instruct = (if (isUser) settings.promptSettings.userInstruct else settings.promptSettings.modelInstruct)
            .replace("\\n", "\n")

        if (instruct.contains("{{prompt}}")) {
            instruct.replace("{{prompt}}", content)
        } else {
            "$instruct$content"
        }
    }

    val lastStringInstruct: String get() = run {
        val instruct = (if (isUser) settings.promptSettings.userInstruct else settings.promptSettings.modelInstruct)
            .replace("\\n", "\n")
            .replaceAfterLast("{{prompt}}", "")

        if (instruct.contains("{{prompt}}")) {
            instruct.replace("{{prompt}}", content)
        } else {
            "$instruct$content"
        }
    }

    fun swipeLeft() {
        if (swipeId.value == 0) return

        swipeId.value = swipeId.value.dec()
    }

    fun swipeRight() {
        if (swipeId.value >= swipes.lastIndex) return

        swipeId.value = swipeId.value.inc()
    }

    fun updateSwipe(index: Int, new: String) {
        if (index > swipes.lastIndex) {
            swipes.add(new)
            if (swipeId.value == swipes.lastIndex - 1) {
                swipeId.value = swipes.lastIndex
            }

        } else {
            swipes[index] = new
        }
    }

    fun updateImage(swipeIndex: Int, path: String) {
        images[swipeIndex]?.let {
            val oldFile = File(it)
            if (oldFile.exists()) oldFile.delete()
        }

        images[swipeIndex] = path
    }

    fun getImage(index: Int): ImageBitmap? {
        return getImageBitmap(images[index] ?: return null)
    }

    fun copy(
        name: String = this.name,
        isUser: Boolean = this.isUser,
        sendDate: Long = this.sendDate,
        swipeId: Int = this.swipeId.value,
        swipes: SnapshotStateList<String> = this.swipes,
        images: MutableMap<Int, String> = this.images,
    ) = Message(name, isUser, sendDate, mutableStateOf(swipeId), swipes, images)
}