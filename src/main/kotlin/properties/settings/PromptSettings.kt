package properties.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import prompt.PromptType

@Serializable(with = PromptSettingsSerializer::class)
class PromptSettings(
    type: PromptType = PromptType.Instruct,
    pattern: String = """
        {{systemPrompt}}
        {{persona}}
        {{personality}}
        {{scenario}}
        {{messageExample}}
        {{chat}}
    """.trimIndent(),
    systemPrompt: String = "Enter RP mode. You shall reply to {{user}} while staying in character. Your responses must be detailed, creative, immersive, and drive the scenario forward. You will follow {{char}}'s persona.",
    systemInstruct: String = "<|system|>{{prompt}}",
    userInstruct: String = "<|user|>{{prompt}}",
    modelInstruct: String = "<|model|>{{prompt}}",
    stopSequence: String = "<|user|>,<|model|>",
) {
    var type: PromptType by mutableStateOf(type)
    var pattern: String by mutableStateOf(pattern)
    var systemPrompt: String by mutableStateOf(systemPrompt)
    var systemInstruct: String by mutableStateOf(systemInstruct)
    var userInstruct: String by mutableStateOf(userInstruct)
    var modelInstruct: String by mutableStateOf(modelInstruct)
    var stopSequence: String by mutableStateOf(stopSequence)

    fun copy(): PromptSettings {
        return PromptSettings(
            type,
            pattern,
            systemPrompt,
            systemInstruct,
            userInstruct,
            modelInstruct,
            stopSequence,
        )
    }

    val splitStopSequence get() = if (stopSequence.isNotEmpty()) {
        if (stopSequence.contains(",")) {
            stopSequence.split(",")
        } else {
            listOf(stopSequence)
        }
    } else emptyList()
}
