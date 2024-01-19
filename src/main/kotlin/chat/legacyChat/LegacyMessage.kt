package chat.legacyChat

import chat.BasicMessageInfo
import chat.Message
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class LegacyMessage(
    override var name: String,
    override var is_user: Boolean,
    override var is_name: Boolean,
    override val send_date: Long?,
    override var mes: String,
    override val chid: Int?,
    override var swipe_id: Int?,
    override var swipes: MutableList<String>?,
) : Message {
    override fun toString(): String = "$name: $mes"

    @OptIn(ExperimentalSerializationApi::class)
    override fun toJSON(): String {
        val info = BasicMessageInfo(name, is_user, is_name, send_date, mes, chid, swipe_id, swipes)
        val json = Json { explicitNulls = false }
        return json.encodeToString(info)
    }
}