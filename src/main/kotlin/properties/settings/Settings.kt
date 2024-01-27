package properties.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.*
import properties.settings.generating.GeneratingSettings
import properties.settings.imageGenerating.ImageGeneratingSettings
import properties.settings.prompt.PromptSettings

@Serializable(with = SettingsSerializer::class)
class Settings(
    language: String = "en",
    link: String = "http://localhost:5000",
    stableDiffusionLink: String = "http://127.0.0.1:7860",
    stableDiffusionApiEnabled: Boolean = false,
    tokenStreaming: Boolean = false,
    tokenStreamingDuration: Int = 1000,
    profileImagesEnabled: Boolean = true,
    normalizeResults: Boolean = true,
    background: String = "bg1.png",
    generating: GeneratingSettings = GeneratingSettings(),
    promptSettings: PromptSettings = PromptSettings(),
    imageGenerating: ImageGeneratingSettings = ImageGeneratingSettings(),
) {
    var language: String by mutableStateOf(language)
    var link: String by mutableStateOf(link)
    var stableDiffusionLink: String by mutableStateOf(stableDiffusionLink)
    var stableDiffusionApiEnabled: Boolean by mutableStateOf(stableDiffusionApiEnabled)
    var tokenStreaming: Boolean by mutableStateOf(tokenStreaming)
    var tokenStreamingDuration: Int by mutableStateOf(tokenStreamingDuration)
    var profileImagesEnabled: Boolean by mutableStateOf(profileImagesEnabled)
    var normalizeResults: Boolean by mutableStateOf(normalizeResults)
    var background: String by mutableStateOf(background)
    var generating: GeneratingSettings by mutableStateOf(generating)
    var promptSettings: PromptSettings by mutableStateOf(promptSettings)
    var imageGenerating: ImageGeneratingSettings by mutableStateOf(imageGenerating)

    val tokenStreamingDurationValueRange get() = 500F..10000F

    fun copy(): Settings {
        return Settings(
            language,
            link,
            stableDiffusionLink,
            stableDiffusionApiEnabled,
            tokenStreaming,
            tokenStreamingDuration,
            profileImagesEnabled,
            normalizeResults,
            background,
            generating.copy(),
            promptSettings.copy(),
            imageGenerating.copy(),
        )
    }
}