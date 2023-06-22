package properties.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ChangeableContextSettings(
    var enabled: Boolean = false,
    var buffer: Int = 512,
) {
    companion object {
        fun createFromJson(jsonObject: JsonObject): ChangeableContextSettings? {
            return try {
                val map = jsonObject.toMap()
                val newSettings = ChangeableContextSettings()

                map["enabled"]?.jsonPrimitive?.booleanOrNull?.let { newSettings.enabled = it }
                map["buffer"]?.jsonPrimitive?.intOrNull?.let { newSettings.buffer = it }

                newSettings
            } catch (e: Exception) { null }
        }
    }
}
