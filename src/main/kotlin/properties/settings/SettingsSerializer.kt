package properties.settings

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class SettingsSerializer : KSerializer<Settings> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.Settings") {
        element<String?>("preset_name")
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

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Settings = decoder.decodeStructure(descriptor) {
        val settings = Settings()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> settings.presetName = decodeNullableSerializableElement(descriptor, 0, String.serializer())
                1 -> settings.language = decodeStringElement(descriptor, 1)
                2 -> settings.link = decodeStringElement(descriptor, 2)
                3 -> settings.stableDiffusionLink = decodeStringElement(descriptor, 3)
                4 -> settings.stableDiffusionApiEnabled = decodeBooleanElement(descriptor, 4)
                5 -> settings.profileImagesEnabled = decodeBooleanElement(descriptor, 5)
                6 -> settings.background = decodeStringElement(descriptor, 6)
                7 -> settings.generating = decodeSerializableElement(descriptor, 8, GeneratingSettings.serializer())
                8 -> settings.promptSettings = decodeSerializableElement(descriptor, 9, PromptSettings.serializer())
                9 -> settings.imageGenerating = decodeSerializableElement(descriptor, 10, ImageGeneratingSettings.serializer())
                10 -> settings.normalizeResults = decodeBooleanElement(descriptor, 11)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure settings
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Settings) = encoder.encodeStructure(descriptor) {
        encodeNullableSerializableElement(descriptor, 0, String.serializer(), value.presetName)
        encodeStringElement(descriptor, 1, value.language)
        encodeStringElement(descriptor, 2, value.link)
        encodeStringElement(descriptor, 3, value.stableDiffusionLink)
        encodeBooleanElement(descriptor, 4, value.stableDiffusionApiEnabled)
        encodeBooleanElement(descriptor, 5, value.profileImagesEnabled)
        encodeStringElement(descriptor, 6, value.background)
        encodeSerializableElement(descriptor, 7, GeneratingSettings.serializer(), value.generating)
        encodeSerializableElement(descriptor, 8, PromptSettings.serializer(), value.promptSettings)
        encodeSerializableElement(descriptor, 9, ImageGeneratingSettings.serializer(), value.imageGenerating)
        encodeBooleanElement(descriptor, 10, value.normalizeResults)
    }
}