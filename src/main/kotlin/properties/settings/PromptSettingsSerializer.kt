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
import prompt.PromptType

class PromptSettingsSerializer : KSerializer<PromptSettings> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.AChat2") {
        element<String>("type")
        element<String>("pattern")
        element<String>("system_prompt")
        element<String>("system_instruct_prefix")
        element<String>("user_instruct_prefix")
        element<String>("model_instruct_prefix")
        element<String>("stop_sequence")
    }

    override fun deserialize(decoder: Decoder): PromptSettings = decoder.decodeStructure(descriptor) {
        val promptSettings = PromptSettings()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> promptSettings.type = PromptType.valueOf(decodeStringElement(descriptor, 0))
                1 -> promptSettings.pattern = decodeStringElement(descriptor, 1)
                2 -> promptSettings.systemPrompt = decodeStringElement(descriptor, 2)
                3 -> promptSettings.systemInstructPrefix = decodeStringElement(descriptor, 3)
                4 -> promptSettings.userInstructPrefix = decodeStringElement(descriptor, 4)
                5 -> promptSettings.modelInstructPrefix = decodeStringElement(descriptor, 5)
                6 -> promptSettings.stopSequence = decodeStringElement(descriptor, 6)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure promptSettings
    }

    override fun serialize(encoder: Encoder, value: PromptSettings) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.type.name)
        encodeStringElement(descriptor, 1, value.pattern)
        encodeStringElement(descriptor, 2, value.systemPrompt)
        encodeStringElement(descriptor, 3, value.systemInstructPrefix)
        encodeStringElement(descriptor, 4, value.userInstructPrefix)
        encodeStringElement(descriptor, 5, value.modelInstructPrefix)
        encodeStringElement(descriptor, 6, value.stopSequence)
    }
}