package properties.settings

import ViewType
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Settings(
    var language: String = "en",
    var link: String = "http://localhost:5000",
    var stable_diffusion_link: String = "http://127.0.0.1:7860",
    var stable_diffusion_api_enabled: Boolean = false,
    var background: String = "bg1.png",
    var characters_list_view: ViewType = ViewType.Grid,
    var use_username: Boolean = false,
    var generating: GeneratingSettings = GeneratingSettings(),
    var image_generating: ImageGeneratingSettings = ImageGeneratingSettings(),
    var multi_gen: MultiGenSettings = MultiGenSettings(),
    var changeable_context: ChangeableContextSettings = ChangeableContextSettings(),
) {
    companion object {
        fun createFromJson(jsonObject: JsonObject): Settings? {
            try {
                val map = jsonObject.toMap()
                val newSettings = Settings()
                map["language"]?.jsonPrimitive?.contentOrNull?.let { newSettings.language = it }
                map["link"]?.jsonPrimitive?.contentOrNull?.let { newSettings.link = it }
                map["stable_diffusion_link"]?.jsonPrimitive?.contentOrNull?.let { newSettings.stable_diffusion_link = it }
                map["stable_diffusion_api_enabled"]?.jsonPrimitive?.booleanOrNull?.let { newSettings.stable_diffusion_api_enabled = it }
                map["characters_list_view"]?.jsonPrimitive?.contentOrNull?.let {
                    try {
                        newSettings.characters_list_view = ViewType.valueOf(it)
                    } catch (e: Exception) {}
                }
                map["use_username"]?.jsonPrimitive?.booleanOrNull?.let { newSettings.use_username = it }

                map["generating"]?.jsonObject?.let { jsonStr ->
                    GeneratingSettings.createFromJson(jsonStr)?.let { newSettings.generating = it }
                }

                map["image_generating"]?.jsonObject?.let { jsonStr ->
                    ImageGeneratingSettings.createFromJson(jsonStr)?.let { newSettings.image_generating = it }
                }

                map["changeable_context"]?.jsonObject?.let { jsonStr ->
                    ChangeableContextSettings.createFromJson(jsonStr)?.let { newSettings.changeable_context = it }
                }

                return newSettings
            } catch (e: Exception) { return null }
        }
    }
}