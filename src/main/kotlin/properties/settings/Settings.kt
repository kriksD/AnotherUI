package properties.settings

import ViewType
import kotlinx.serialization.*

@Serializable
data class Settings(
    var language: String = "en",
    var link: String = "http://localhost:5000",
    var stable_diffusion_link: String = "http://127.0.0.1:7860",
    var stable_diffusion_api_enabled: Boolean = false,
    var background: String = "bg1.png",
    var characters_list_view: ViewType = ViewType.Grid,
    var use_username: Boolean = false,
    val generating: GeneratingSettings = GeneratingSettings(),
    val image_generating: ImageGeneratingSettings = ImageGeneratingSettings(),
    val multi_gen: MultiGenSettings = MultiGenSettings()
)