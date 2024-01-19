package chat

import androidx.compose.ui.graphics.ImageBitmap

interface Chat {
    val fileName: String
    val characterFileName: String
    val chatInfo: ChatInfo
    val messages: MutableList<Message>

    fun addMessage(text: String, is_user: Boolean = false): Message {
        val last = messages.lastOrNull()
        if (last != null && !last.is_user) {
            last.swipe_id = null
            last.swipes = null
        }

        val newMessage = createNewMessage(text, is_user, !is_user)
        messages.add(newMessage)
        return newMessage
    }

    fun createNewMessage(text: String, is_user: Boolean = false, withSwipes: Boolean = true): Message

    fun removeLast() {
        messages.removeLast()
        val last = messages.last()
        if (!last.is_user) {
            last.swipe_id = 0
            last.swipes = mutableListOf(last.mes)
        }
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

    fun save()

    fun delete()

    fun swipeLeft() { messages.last().swipeLeft() }

    fun swipeRight() { messages.last().swipeRight() }

    fun updateSwipe(index: Int, new: String) {
        messages.last().updateSwipe(index, new)
    }

    fun image(messageIndex: Int, swipeIndex: Int = 0): ImageBitmap? = null

    fun updateImage(image: ImageBitmap, messageIndex: Int, swipeIndex: Int) {}

    fun updateImages(list: Map<String, ImageBitmap>) {}
}