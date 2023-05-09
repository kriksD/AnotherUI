package character

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class JsonCharacter(
    val public_id: String? = null,
    var name: String,
    var description: String = "",
    var personality: String = "",
    var first_mes: String = "",
    val avatar: String = "none",
    var chat: String = "",
    var mes_example: String = "",
    var scenario: String = "",
    var edit_date: Long? = Calendar.getInstance().timeInMillis,
    val create_date: Long? = Calendar.getInstance().timeInMillis,
    var add_date: Long? = Calendar.getInstance().timeInMillis,
)
