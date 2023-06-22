package properties.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class ImageGeneratingSettings(
    var seed: Int = -1,
    var steps: Int = 20,
    var cfg_scale: Int = 7,
    var width: Int = 384,
    var height: Int = 256,
    var negative_prompt: String = "worst quality, low quality, normal quality, lowres, less details",
    var style: String = "very detailed, best quality, masterpiece, highres",
    var sampler_index: String = "Euler",
    var save_images: Boolean = false,
) {
    companion object {
        fun createFromJson(jsonObject: JsonObject): ImageGeneratingSettings? {
            return try {
                val map = jsonObject.toMap()
                val newSettings = ImageGeneratingSettings()

                map["seed"]?.jsonPrimitive?.intOrNull?.let { newSettings.seed = it }
                map["steps"]?.jsonPrimitive?.intOrNull?.let { newSettings.steps = it }
                map["cfg_scale"]?.jsonPrimitive?.intOrNull?.let { newSettings.cfg_scale = it }
                map["width"]?.jsonPrimitive?.intOrNull?.let { newSettings.width = it }
                map["height"]?.jsonPrimitive?.intOrNull?.let { newSettings.height = it }
                map["negative_prompt"]?.jsonPrimitive?.contentOrNull?.let { newSettings.negative_prompt = it }
                map["style"]?.jsonPrimitive?.contentOrNull?.let { newSettings.style = it }
                map["sampler_index"]?.jsonPrimitive?.contentOrNull?.let { newSettings.sampler_index = it }
                map["save_images"]?.jsonPrimitive?.booleanOrNull?.let { newSettings.save_images = it }

                newSettings
            } catch (e: Exception) { null }
        }
    }
}