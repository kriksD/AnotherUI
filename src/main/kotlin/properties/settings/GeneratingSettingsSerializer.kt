package properties.settings

import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class GeneratingSettingsSerializer : KSerializer<GeneratingSettings> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.GeneratingSettings") {
        element<Int>("max_context_length")
        element<Int>("max_length")
        element<Float>("rep_pen")
        element<Int>("rep_pen_range")
        element<Float>("rep_pen_slope")
        element<Float>("temperature")
        element<Float>("tfs")
        element<Float>("top_a")
        element<Int>("top_k")
        element<Float>("top_p")
        element<Float>("min_p")
        element<Float>("typical")
        element<List<Int>>("sampler_order")
        element<Int>("seed")
    }

    override fun deserialize(decoder: Decoder): GeneratingSettings = decoder.decodeStructure(descriptor) {
        val generatingSettings = GeneratingSettings()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> generatingSettings.maxContextLength = decodeIntElement(descriptor, 0)
                1 -> generatingSettings.maxLength = decodeIntElement(descriptor, 1)
                2 -> generatingSettings.repPen = decodeFloatElement(descriptor, 2)
                3 -> generatingSettings.repPenRange = decodeIntElement(descriptor, 3)
                4 -> generatingSettings.repPenSlope = decodeFloatElement(descriptor, 4)
                5 -> generatingSettings.temperature = decodeFloatElement(descriptor, 5)
                6 -> generatingSettings.tfs = decodeFloatElement(descriptor, 6)
                7 -> generatingSettings.topA = decodeFloatElement(descriptor, 7)
                8 -> generatingSettings.topK = decodeIntElement(descriptor, 8)
                9 -> generatingSettings.topP = decodeFloatElement(descriptor, 9)
                10 -> generatingSettings.minP = decodeFloatElement(descriptor, 10)
                11 -> generatingSettings.typical = decodeFloatElement(descriptor, 11)
                12 -> generatingSettings.samplerOrder = decodeSerializableElement(descriptor, 12, ListSerializer(Int.serializer())).toMutableStateList()
                13 -> generatingSettings.seed = decodeIntElement(descriptor, 13)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure generatingSettings
    }

    override fun serialize(encoder: Encoder, value: GeneratingSettings) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.maxContextLength)
        encodeIntElement(descriptor, 1, value.maxLength)
        encodeFloatElement(descriptor, 2, value.repPen)
        encodeIntElement(descriptor, 3, value.repPenRange)
        encodeFloatElement(descriptor, 4, value.repPenSlope)
        encodeFloatElement(descriptor, 5, value.temperature)
        encodeFloatElement(descriptor, 6, value.tfs)
        encodeFloatElement(descriptor, 7, value.topA)
        encodeIntElement(descriptor, 8, value.topK)
        encodeFloatElement(descriptor, 9, value.topP)
        encodeFloatElement(descriptor, 10, value.minP)
        encodeFloatElement(descriptor, 11, value.typical)
        encodeSerializableElement(descriptor, 12, ListSerializer(Int.serializer()), value.samplerOrder)
        encodeIntElement(descriptor, 13, value.seed)
    }
}