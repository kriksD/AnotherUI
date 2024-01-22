package character

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.Serializable
import java.util.*

@Serializable(with = CharacterInfoSerializer::class)
class CharacterInfo(
    name: String,
    description: String = "",
    personality: String = "",
    firstMessage: String = "",
    alternateGreetings: MutableList<String> = mutableListOf(),
    avatar: String = "none",
    chat: String = "",
    messageExample: String = "",
    scenario: String = "",
    editDate: Long? = Calendar.getInstance().timeInMillis,
    createDate: Long? = Calendar.getInstance().timeInMillis,
    addDate: Long? = Calendar.getInstance().timeInMillis,
) {
    var name: String by mutableStateOf(name)
    var description: String by mutableStateOf(description)
    var personality: String by mutableStateOf(personality)
    var firstMessage: String by mutableStateOf(firstMessage)
    var alternateGreetings: SnapshotStateList<String> = alternateGreetings.toMutableStateList()
    var avatar: String by mutableStateOf(avatar)
    var chat: String by mutableStateOf(chat)
    var messageExample: String by mutableStateOf(messageExample)
    var scenario: String by mutableStateOf(scenario)
    var editDate: Long? by mutableStateOf(editDate)
    var createDate: Long? by mutableStateOf(createDate)
    var addDate: Long? by mutableStateOf(addDate)
}
