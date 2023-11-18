package properties.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import prompt.PromptType

@Serializable
data class PromptSettings(
    var type: PromptType = PromptType.Instruct,
    var pattern: String = """
        {{systemPrompt}}
        {{persona}}
        {{personality}}
        {{scenario}}
        {{messageExample}}
        {{chat}}
    """.trimIndent(),
    var system_prompt: String = "Enter RP mode. You shall reply to {{user}} while staying in character. Your responses must be detailed, creative, immersive, and drive the scenario forward. You will follow {{char}}'s persona.",
    var system_instruct_prefix: String = "<|system|>",
    var user_instruct_prefix: String = "<|user|>",
    var model_instruct_prefix: String = "<|model|>",
    var stop_sequence: String = "<|user|>,<|model|>",
) {
    companion object {
        fun createFromJson(jsonObject: JsonObject): PromptSettings? {
            return try {
                val map = jsonObject.toMap()
                val newSettings = PromptSettings()

                map["type"]?.jsonPrimitive?.contentOrNull?.let {
                    try {
                        newSettings.type = PromptType.valueOf(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                map["pattern"]?.jsonPrimitive?.contentOrNull?.let { newSettings.pattern = it }
                map["system_prompt"]?.jsonPrimitive?.contentOrNull?.let { newSettings.system_prompt = it }
                map["system_instruct_prefix"]?.jsonPrimitive?.contentOrNull?.let { newSettings.system_instruct_prefix = it }
                map["user_instruct_prefix"]?.jsonPrimitive?.contentOrNull?.let { newSettings.user_instruct_prefix = it }
                map["model_instruct_prefix"]?.jsonPrimitive?.contentOrNull?.let { newSettings.model_instruct_prefix = it }
                map["stop_sequence"]?.jsonPrimitive?.contentOrNull?.let { newSettings.stop_sequence = it }

                newSettings
            } catch (e: Exception) { null }
        }
    }
}
