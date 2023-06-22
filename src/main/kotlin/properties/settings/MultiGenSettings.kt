package properties.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class MultiGenSettings(
    var enabled: Boolean = false,
    var tokens_per_step: Int = 32,
    var tries: Int = 1,
) {
    companion object {
        fun createFromJson(jsonObject: JsonObject): MultiGenSettings? {
            return try {
                val map = jsonObject.toMap()
                val newSettings = MultiGenSettings()

                map["enabled"]?.jsonPrimitive?.booleanOrNull?.let { newSettings.enabled = it }
                map["tokens_per_step"]?.jsonPrimitive?.intOrNull?.let { newSettings.tokens_per_step = it }
                map["tries"]?.jsonPrimitive?.intOrNull?.let { newSettings.tries = it }

                newSettings
            } catch (e: Exception) { null }
        }
    }
}