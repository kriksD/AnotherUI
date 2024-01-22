package character

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class CharacterMetaDataSerializer : KSerializer<CharacterMetaData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CharacterMetaData") {
        element<Boolean>("favorite")
    }

    override fun deserialize(decoder: Decoder): CharacterMetaData = decoder.decodeStructure(descriptor) {
        val metaData = CharacterMetaData()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> metaData.favorite = decodeBooleanElement(descriptor, index)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure metaData
    }

    override fun serialize(encoder: Encoder, value: CharacterMetaData) = encoder.encodeStructure(descriptor) {
        encodeBooleanElement(descriptor, 0, value.favorite)
    }
}
