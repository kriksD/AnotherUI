package chat

import kotlinx.serialization.Serializable

@Serializable
data class BasicMessageInfo(
    var name: String,
    var is_user: Boolean,
    var is_name: Boolean,
    val send_date: Long?,
    var mes: String,
    val chid: Int?,
    var swipe_id: Int?,
    var swipes: MutableList<String>?,
)
