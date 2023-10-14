package character.chat

import androidx.compose.ui.graphics.ImageBitmap
import settings
import user

interface Message {
    var name: String
    var is_user: Boolean
    var is_name: Boolean
    val send_date: Long?
    var mes: String
    val chid: Int?
    var swipe_id: Int?
    var swipes: MutableList<String>?

    val string: String get() = "${if (settings.use_username && is_user) user.name else name}: $mes"

    val stringInstruct: String get() = "${if (is_user) "<|user|>" else "<|model|>"}$mes"

    fun toJSON(): String

    fun additionalInfoToJson(): String = ""

    fun swipeLeft() {
        if (swipe_id == null || swipe_id == 0 || swipes == null) return

        swipe_id = swipe_id!!.dec()
        mes = swipes!![swipe_id!!]
    }

    fun swipeRight() {
        if (swipe_id == null || swipe_id!! >= swipes!!.size - 1 || swipes == null) return

        swipe_id = swipe_id!!.inc()
        mes = swipes!![swipe_id!!]
    }

    fun updateSwipe(index: Int, new: String) {
        if (swipe_id == null || swipes == null) return

        if (index > swipes!!.lastIndex) {
            swipes?.add(new)
            if (swipe_id!! == swipes!!.lastIndex - 1) {
                swipe_id = swipes!!.lastIndex
                mes = swipes!!.last()
            }

        } else {
            swipes!![index] = new
            if (swipe_id == index) {
                mes = swipes!![index]
            }
        }
    }

    fun imageName(index: Int): String? = null

    fun updateImage(imageName: String, swipeIndex: Int) {}

    fun imageNames(): List<String> = emptyList()

    /**
     * @return removed image names.
     */
    fun removeRestImages(): List<String> = emptyList()
}