package properties.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class GeneratingSettings(
    var max_context_length: Int = 1600,
    var max_length: Int = 200,
    var rep_pen: Float = 1.08F,
    var rep_pen_range: Int = 1024,
    var rep_pen_slope: Float = 0.9F,
    var temperature: Float = 0.65F,
    var tfs: Float = 0.65F,
    var top_a: Float = 0.9F,
    var top_k: Int = 0,
    var top_p: Float = 0.9F,
    var typical: Float = 1F,
) {
    companion object {
        fun createFromJson(jsonObject: JsonObject): GeneratingSettings? {
            return try {
                val map = jsonObject.toMap()
                val newSettings = GeneratingSettings()

                map["max_context_length"]?.jsonPrimitive?.intOrNull?.let { newSettings.max_context_length = it }
                map["max_length"]?.jsonPrimitive?.intOrNull?.let { newSettings.max_length = it }
                map["rep_pen"]?.jsonPrimitive?.floatOrNull?.let { newSettings.rep_pen = it }
                map["rep_pen_range"]?.jsonPrimitive?.intOrNull?.let { newSettings.rep_pen_range = it }
                map["rep_pen_slope"]?.jsonPrimitive?.floatOrNull?.let { newSettings.rep_pen_slope = it }
                map["temperature"]?.jsonPrimitive?.floatOrNull?.let { newSettings.temperature = it }
                map["tfs"]?.jsonPrimitive?.floatOrNull?.let { newSettings.tfs = it }
                map["top_a"]?.jsonPrimitive?.floatOrNull?.let { newSettings.top_a = it }
                map["top_k"]?.jsonPrimitive?.intOrNull?.let { newSettings.top_k = it }
                map["top_p"]?.jsonPrimitive?.floatOrNull?.let { newSettings.top_p = it }
                map["typical"]?.jsonPrimitive?.floatOrNull?.let { newSettings.typical = it }

                newSettings
            } catch (e: Exception) { null }
        }
    }
}
