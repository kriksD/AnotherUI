package character

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CharacterInfoJson(
    var name: String,
    var description: String = "",
    var personality: String = "",
    var first_message: String = "",
    val avatar: String = "none",
    var chat: String = "",
    var message_example: String = "",
    var scenario: String = "",
    var edit_date: Long? = Calendar.getInstance().timeInMillis,
    val create_date: Long? = Calendar.getInstance().timeInMillis,
    var add_date: Long? = Calendar.getInstance().timeInMillis,
) {
    companion object {
        fun make(info: CharacterInfo): CharacterInfoJson {
            return CharacterInfoJson(
                info.name,
                info.description,
                info.personality,
                info.first_mes,
                info.avatar,
                info.chat,
                info.mes_example,
                info.scenario,
                info.edit_date
            )
        }
    }
}
