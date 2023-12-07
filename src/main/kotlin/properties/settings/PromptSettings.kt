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
    systemInstructPrefix: String = "<|system|>",
    userInstructPrefix: String = "<|user|>",
    modelInstructPrefix: String = "<|model|>",
    stopSequence: String = "<|user|>,<|model|>",
) {
    var type: PromptType by mutableStateOf(type)
    var pattern: String by mutableStateOf(pattern)
    var systemPrompt: String by mutableStateOf(systemPrompt)
    var systemInstructPrefix: String by mutableStateOf(systemInstructPrefix)
    var userInstructPrefix: String by mutableStateOf(userInstructPrefix)
    var modelInstructPrefix: String by mutableStateOf(modelInstructPrefix)
    var stopSequence: String by mutableStateOf(stopSequence)

    fun copy(): PromptSettings {
        return PromptSettings(
            type,
            pattern,
            systemPrompt,
            systemInstructPrefix,
            userInstructPrefix,
            modelInstructPrefix,
            stopSequence,
        )
    }
}
