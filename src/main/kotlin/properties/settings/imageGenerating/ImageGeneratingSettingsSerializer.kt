package properties.settings.imageGenerating

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class ImageGeneratingSettingsSerializer : KSerializer<ImageGeneratingSettings> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.ImageGeneratingSettings") {
        element<Int>("seed")
        element<Int>("steps")
        element<Int>("cfg_scale")
        element<Int>("width")
        element<Int>("height")
        element<String>("negative_prompt")
        element<String>("style")
        element<String>("sampler_index")
        element<Boolean>("save_images")
    }

    override fun deserialize(decoder: Decoder): ImageGeneratingSettings = decoder.decodeStructure(descriptor) {
        val imageGeneratingSettings = ImageGeneratingSettings()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> imageGeneratingSettings.seed = decodeIntElement(descriptor, 0)
                1 -> imageGeneratingSettings.steps = decodeIntElement(descriptor, 1)
                2 -> imageGeneratingSettings.cfgScale = decodeIntElement(descriptor, 2)
                3 -> imageGeneratingSettings.width = decodeIntElement(descriptor, 3)
                4 -> imageGeneratingSettings.height = decodeIntElement(descriptor, 4)
                5 -> imageGeneratingSettings.negativePrompt = decodeStringElement(descriptor, 5)
                6 -> imageGeneratingSettings.style = decodeStringElement(descriptor, 6)
                7 -> imageGeneratingSettings.samplerIndex = decodeStringElement(descriptor, 7)
                8 -> imageGeneratingSettings.saveImages = decodeBooleanElement(descriptor, 8)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure imageGeneratingSettings
    }

    override fun serialize(encoder: Encoder, value: ImageGeneratingSettings) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.seed)
        encodeIntElement(descriptor, 1, value.steps)
        encodeIntElement(descriptor, 2, value.cfgScale)
        encodeIntElement(descriptor, 3, value.width)
        encodeIntElement(descriptor, 4, value.height)
        encodeStringElement(descriptor, 5, value.negativePrompt)
        encodeStringElement(descriptor, 6, value.style)
        encodeStringElement(descriptor, 7, value.samplerIndex)
        encodeBooleanElement(descriptor, 8, value.saveImages)
    }
}