package properties.settings

import kotlinx.serialization.Serializable

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
)