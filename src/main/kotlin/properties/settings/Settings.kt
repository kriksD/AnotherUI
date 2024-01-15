package properties.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.*

@Serializable(with = SettingsSerializer::class)
class Settings(
    presetName: String? = null,
    language: String = "en",
    link: String = "http://localhost:5000",
    stableDiffusionLink: String = "http://127.0.0.1:7860",
    stableDiffusionApiEnabled: Boolean = false,
    profileImagesEnabled: Boolean = true,
    normalizeResults: Boolean = true,
    background: String = "bg1.png",
    generating: GeneratingSettings = GeneratingSettings(),
    promptSettings: PromptSettings = PromptSettings(),
    imageGenerating: ImageGeneratingSettings = ImageGeneratingSettings(),
) {
    var presetName: String? by mutableStateOf(presetName)
    var language: String by mutableStateOf(language)
    var link: String by mutableStateOf(link)
    var stableDiffusionLink: String by mutableStateOf(stableDiffusionLink)
    var stableDiffusionApiEnabled: Boolean by mutableStateOf(stableDiffusionApiEnabled)
    var profileImagesEnabled: Boolean by mutableStateOf(profileImagesEnabled)
    var normalizeResults: Boolean by mutableStateOf(normalizeResults)
    var background: String by mutableStateOf(background)
    var generating: GeneratingSettings by mutableStateOf(generating)
    var promptSettings: PromptSettings by mutableStateOf(promptSettings)
    var imageGenerating: ImageGeneratingSettings by mutableStateOf(imageGenerating)

    fun copy(): Settings {
        return Settings(
            presetName,
            language,
            link,
            stableDiffusionLink,
            stableDiffusionApiEnabled,
            profileImagesEnabled,
            normalizeResults,
            background,
            generating.copy(),
            promptSettings.copy(),
            imageGenerating.copy(),
        )
    }
}