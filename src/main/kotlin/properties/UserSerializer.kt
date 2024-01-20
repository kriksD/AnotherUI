package properties

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class UserSerializer : KSerializer<User> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("User") {
        element<String>("name")
        element<String>("description")
        element<String>("profile_image_file")
    }

    override fun deserialize(decoder: Decoder): User = decoder.decodeStructure(descriptor) {
        val settings = User()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> settings.name = decodeStringElement(descriptor, index)
                1 -> settings.description = decodeStringElement(descriptor, index)
                2 -> settings.profileImageFile = decodeStringElement(descriptor, index)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure settings
    }

    override fun serialize(encoder: Encoder, value: User) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.name)
        encodeStringElement(descriptor, 1, value.description)
        encodeStringElement(descriptor, 2, value.profileImageFile)
    }
}