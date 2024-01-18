package properties.settings.imageGenerating

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable(with = ImageGeneratingSettingsSerializer::class)
class ImageGeneratingSettings(
    seed: Int = -1,
    steps: Int = 20,
    cfgScale: Int = 7,
    width: Int = 384,
    height: Int = 256,
    negativePrompt: String = "worst quality, low quality, normal quality, lowres, less details",
    style: String = "very detailed, best quality, masterpiece, highres",
    samplerIndex: String = "Euler",
    saveImages: Boolean = false,
) {
    var seed: Int by mutableStateOf(seed)
    var steps: Int by mutableStateOf(steps)
    var cfgScale: Int by mutableStateOf(cfgScale)
    var width: Int by mutableStateOf(width)
    var height: Int by mutableStateOf(height)
    var negativePrompt: String by mutableStateOf(negativePrompt)
    var style: String by mutableStateOf(style)
    var samplerIndex: String by mutableStateOf(samplerIndex)
    var saveImages: Boolean by mutableStateOf(saveImages)

    fun copy(): ImageGeneratingSettings {
        return ImageGeneratingSettings(
            seed,
            steps,
            cfgScale,
            width,
            height,
            negativePrompt,
            style,
            samplerIndex,
            saveImages,
        )
    }
}