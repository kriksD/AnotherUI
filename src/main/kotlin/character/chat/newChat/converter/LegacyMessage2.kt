package character.chat.newChat.converter

data class LegacyMessage2(
    var name: String,
    var is_user: Boolean,
    var is_name: Boolean,
    val send_date: Long?,
    var mes: String,
    val chid: Int?,
    var swipe_id: Int?,
    var swipes: MutableList<String>?,
)
