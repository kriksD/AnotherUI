package character

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = ChubFixDataSerializer::class)
class ChubFixData(
    val data: CharacterInfo,
)

class ChubFixDataSerializer : KSerializer<ChubFixData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChubFixData") {
        element<CharacterInfo>("data")
    }

    override fun deserialize(decoder: Decoder): ChubFixData = decoder.decodeStructure(descriptor) {
        var data: CharacterInfo? = null
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> data = decodeSerializableElement(descriptor, 0, CharacterInfoSerializer())
                else -> throw SerializationException("Unexpected index $index")
            }
        }
        return@decodeStructure ChubFixData(data ?: throw SerializationException("Missing field \"data\""))
    }

    override fun serialize(encoder: Encoder, value: ChubFixData) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, CharacterInfoSerializer(), value.data)
        }
    }
}
