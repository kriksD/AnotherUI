package properties.settings

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import properties.settings.generating.GeneratingSettings
import properties.settings.imageGenerating.ImageGeneratingSettings
import properties.settings.prompt.PromptSettings

class SettingsSerializer : KSerializer<Settings> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.Settings") {
        element<String>("language")
        element<String>("link")
        element<String>("stable_diffusion_link")
        element<Boolean>("stable_diffusion_api_enabled")
        element<Boolean>("profile_images_enabled")
        element<String>("background")
        element<GeneratingSettings>("generating")
        element<PromptSettings>("prompt_settings")
        element<ImageGeneratingSettings>("image_generating")
        element<Boolean>("normalize_results")
    }

    override fun deserialize(decoder: Decoder): Settings = decoder.decodeStructure(descriptor) {
        val settings = Settings()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> settings.language = decodeStringElement(descriptor, index)
                1 -> settings.link = decodeStringElement(descriptor, index)
                2 -> settings.stableDiffusionLink = decodeStringElement(descriptor, index)
                3 -> settings.stableDiffusionApiEnabled = decodeBooleanElement(descriptor, index)
                4 -> settings.profileImagesEnabled = decodeBooleanElement(descriptor, index)
                5 -> settings.background = decodeStringElement(descriptor, index)
                6 -> settings.generating = decodeSerializableElement(descriptor, index, GeneratingSettings.serializer())
                7 -> settings.promptSettings = decodeSerializableElement(descriptor, index, PromptSettings.serializer())
                8 -> settings.imageGenerating = decodeSerializableElement(descriptor, index, ImageGeneratingSettings.serializer())
                9 -> settings.normalizeResults = decodeBooleanElement(descriptor, index)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure settings
    }

    override fun serialize(encoder: Encoder, value: Settings) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.language)
        encodeStringElement(descriptor, 1, value.link)
        encodeStringElement(descriptor, 2, value.stableDiffusionLink)
        encodeBooleanElement(descriptor, 3, value.stableDiffusionApiEnabled)
        encodeBooleanElement(descriptor, 4, value.profileImagesEnabled)
        encodeStringElement(descriptor, 5, value.background)
        encodeSerializableElement(descriptor, 6, GeneratingSettings.serializer(), value.generating)
        encodeSerializableElement(descriptor, 7, PromptSettings.serializer(), value.promptSettings)
        encodeSerializableElement(descriptor, 8, ImageGeneratingSettings.serializer(), value.imageGenerating)
        encodeBooleanElement(descriptor, 9, value.normalizeResults)
    }
}