package character.chat.newChat

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import settings
import user

class AMessage2(
    var name: String,
    var isUser: Boolean,
    val sendDate: Long,
    var content: MutableState<String>,
    var swipeId: MutableState<Int>,
    var swipes: SnapshotStateList<String>,
    var images: MutableMap<Int, String> = mutableMapOf(),
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun toJSON(): String {
        val json = Json { explicitNulls = false }
        return json.encodeToString(this)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun additionalInfoToJson(): String {
        val info = AdditionalMessageInfo2(images)
        val json = Json { explicitNulls = false }
        return json.encodeToString(info)
    }

    val string: String get() = "${if (settings.use_username && isUser) user.name else name}: $content"

    val stringInstruct: String get() = "${if (isUser) "<|user|>" else "<|model|>"}$content"

    fun swipeLeft() {
        if (swipeId.value == 0) return

        swipeId.value = swipeId.value.dec()
        content.value = swipes[swipeId.value]
    }

    fun swipeRight() {
        if (swipeId.value >= swipes.size - 1) return

        swipeId.value = swipeId.value.inc()
        content.value = swipes[swipeId.value]
    }

    fun updateSwipe(index: Int, new: String) {
        if (index > swipes.lastIndex) {
            swipes.add(new)
            if (swipeId.value == swipes.lastIndex - 1) {
                swipeId.value = swipes.lastIndex
                content.value = swipes.last()
            }

        } else {
            swipes[index] = new
            if (swipeId.value == index) {
                content.value = swipes[index]
            }
        }
    }

    fun imageName(index: Int): String? = images[index]

    fun updateImage(imageName: String, swipeIndex: Int) {
        images[swipeIndex] = imageName
    }

    fun imageNames(): List<String> {
        val list = mutableListOf<String>()
        images.forEach { (_, name) -> list.add(name) }
        return list
    }

    /**
     * @return removed image names.
     */
    fun removeRestImages(): List<String> {
        var leave: String? = null
        val removed = mutableListOf<String>()

        images.forEach { (index, name) ->
            if (index != swipeId.value) {
                removed.add(name)
            } else {
                leave = name
            }
        }

        images.clear()
        leave?.let { images[0] = it }
        return removed
    }
}