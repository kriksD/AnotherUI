package properties.settings.prompt

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
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.PromptSettings") {
        element<String>("type")
        element<String>("pattern")
        element<String>("system_prompt")
        element<String>("system_instruct")
        element<String>("user_instruct")
        element<String>("model_instruct")
        element<String>("stop_sequence")
        element<Boolean>("reinclude_erased_messages")
    }

    override fun deserialize(decoder: Decoder): PromptSettings = decoder.decodeStructure(descriptor) {
        val promptSettings = PromptSettings()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> promptSettings.type = PromptType.valueOf(decodeStringElement(descriptor, 0))
                1 -> promptSettings.pattern = decodeStringElement(descriptor, 1)
                2 -> promptSettings.systemPrompt = decodeStringElement(descriptor, 2)
                3 -> promptSettings.systemInstruct = decodeStringElement(descriptor, 3)
                4 -> promptSettings.userInstruct = decodeStringElement(descriptor, 4)
                5 -> promptSettings.modelInstruct = decodeStringElement(descriptor, 5)
                6 -> promptSettings.stopSequence = decodeStringElement(descriptor, 6)
                7 -> promptSettings.reincludeErasedMessages = decodeBooleanElement(descriptor, 7)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure promptSettings
    }

    override fun serialize(encoder: Encoder, value: PromptSettings) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.type.name)
        encodeStringElement(descriptor, 1, value.pattern)
        encodeStringElement(descriptor, 2, value.systemPrompt)
        encodeStringElement(descriptor, 3, value.systemInstruct)
        encodeStringElement(descriptor, 4, value.userInstruct)
        encodeStringElement(descriptor, 5, value.modelInstruct)
        encodeStringElement(descriptor, 6, value.stopSequence)
        encodeBooleanElement(descriptor, 7, value.reincludeErasedMessages)
    }
}