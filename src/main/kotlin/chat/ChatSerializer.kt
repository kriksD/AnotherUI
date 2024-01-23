package chat

import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class ChatSerializer : KSerializer<Chat> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Chat") {
        element<String>("file_name")
        element<String>("folder_name")
        element<Long>("create_date")
        element<List<Message>>("messages")
    }
    private val listSerializer = ListSerializer(MessageSerializer())

    override fun deserialize(decoder: Decoder): Chat = decoder.decodeStructure(descriptor) {
        var fileName: String? = null
        var folderName: String? = null
        var createDate: Long? = null
        var messages: List<Message>? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> fileName = decodeStringElement(descriptor, 0)
                1 -> folderName = decodeStringElement(descriptor, 1)
                2 -> createDate = decodeLongElement(descriptor, 2)
                3 -> messages = decodeSerializableElement(descriptor, 3, listSerializer)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure Chat(
            fileName ?: throw SerializationException("Missing field \"file_name\""),
            folderName ?: throw SerializationException("Missing field \"folder_name\""),
            createDate ?: throw SerializationException("Missing field \"create_date\""),
            messages?.toMutableStateList() ?: throw SerializationException("Missing field \"messages\""),
        )
    }

    override fun serialize(encoder: Encoder, value: Chat) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.fileName)
        encodeStringElement(descriptor, 1, value.folderName)
        encodeLongElement(descriptor, 2, value.createDate)
        encodeSerializableElement(descriptor, 3, listSerializer, value.messages)
    }
}