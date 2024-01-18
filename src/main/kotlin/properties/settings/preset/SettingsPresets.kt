package properties.settings.preset

import kotlinx.serialization.Serializable
import properties.settings.generating.GeneratingSettings
import properties.settings.imageGenerating.ImageGeneratingSettings
import properties.settings.prompt.PromptSettings
import settings
import java.io.File

@Serializable(with = SettingsPresetsSerializer::class)
class SettingsPresets(
    generatingSettingsPresetName: String? = null,
    imageGeneratingSettingsPresetName: String? = null,
    promptSettingsPresetName: String? = null,
) {
    val generatingPresets = Presets(
        selectedPreset = generatingSettingsPresetName,
        folder = File("data/settings/presets/generating"),
        serializer = GeneratingSettings.serializer(),
        createValue = { it?.copy() ?: GeneratingSettings() },
    )

    val imageGeneratingPresets = Presets(
        selectedPreset = imageGeneratingSettingsPresetName,
        folder = File("data/settings/presets/imageGenerating"),
        serializer = ImageGeneratingSettings.serializer(),
        createValue = { it?.copy() ?: ImageGeneratingSettings() },
    )

    val promptPresets = Presets(
        selectedPreset = promptSettingsPresetName,
        folder = File("data/settings/presets/prompt"),
        serializer = PromptSettings.serializer(),
        createValue = { it?.copy() ?: PromptSettings() },
    )

    fun load() {
        generatingPresets.load()
        imageGeneratingPresets.load()
        promptPresets.load()
    }

    fun onSelect() {
        generatingPresets.onSelect { settings.generating = it.copy() }
        imageGeneratingPresets.onSelect { settings.imageGenerating = it.copy() }
        promptPresets.onSelect { settings.promptSettings = it.copy() }
    }
}
