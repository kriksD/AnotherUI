package properties.settings.preset

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

class SettingsPresetsSerializer : KSerializer<SettingsPresets> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.SettingsPresets") {
        element<String>("generating_settings_preset")
        element<String>("image_generating_settings_preset")
        element<String>("prompt_settings_preset")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): SettingsPresets = decoder.decodeStructure(descriptor) {
        var generatingPresetName: String? = null
        var imageGeneratingPresetName: String? = null
        var promptPresetName: String? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> generatingPresetName = decodeNullableSerializableElement(descriptor, 0, String.serializer())
                1 -> imageGeneratingPresetName = decodeNullableSerializableElement(descriptor, 1, String.serializer())
                2 -> promptPresetName = decodeNullableSerializableElement(descriptor, 2, String.serializer())
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure SettingsPresets(generatingPresetName, imageGeneratingPresetName, promptPresetName)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: SettingsPresets) = encoder.encodeStructure(descriptor) {
        encodeNullableSerializableElement(descriptor, 0, String.serializer(), value.generatingPresets.selectedPresetName)
        encodeNullableSerializableElement(descriptor, 1, String.serializer(), value.imageGeneratingPresets.selectedPresetName)
        encodeNullableSerializableElement(descriptor, 2, String.serializer(), value.promptPresets.selectedPresetName)
    }
}