package character.chat.newChat

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

class AChat2Serializer : KSerializer<AChat2> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.AChat2") {
        element<String>("file_name")
        element<String>("folder_name")
        element<Long>("create_date")
        element<List<AMessage2>>("messages")
    }
    private val listSerializer = ListSerializer(AMessage2Serializer())

    override fun deserialize(decoder: Decoder): AChat2 = decoder.decodeStructure(descriptor) {
        var fileName: String? = null
        var folderName: String? = null
        var createDate: Long? = null
        var messages: List<AMessage2>? = null

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

        return@decodeStructure AChat2(
            fileName ?: throw SerializationException("Missing field \"file_name\""),
            folderName ?: throw SerializationException("Missing field \"folder_name\""),
            createDate ?: throw SerializationException("Missing field \"create_date\""),
            messages?.toMutableStateList() ?: throw SerializationException("Missing field \"messages\""),
        )
    }

    override fun serialize(encoder: Encoder, value: AChat2) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.fileName)
        encodeStringElement(descriptor, 1, value.folderName)
        encodeLongElement(descriptor, 2, value.createDate)
        encodeSerializableElement(descriptor, 3, listSerializer, value.messages)
    }
}

/*class DiarySerializer : KSerializer<Diary> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("diary.Diary") {
        element<Int>("id")
        element<String>("title")
        element<String>("description")
        element<List<DiaryEntry>>("entries")
    }
    private val listSerializer = ListSerializer(DiaryEntrySerializer())

    override fun deserialize(decoder: Decoder): Diary = decoder.decodeStructure(descriptor) {
        var diary: Diary? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> {
                    val id = decodeIntElement(descriptor, 0)
                    diary = Diary(id , "", "")
                }
                1 -> diary?.title = decodeStringElement(descriptor, 1)
                2 -> diary?.description = decodeStringElement(descriptor, 2)
                3 -> decodeSerializableElement(descriptor, 3, listSerializer).forEach { diary?.addEntry(it) }
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure diary ?: throw SerializationException("Missing field")
    }

    override fun serialize(encoder: Encoder, value: Diary) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.id)
        encodeStringElement(descriptor, 1, value.title)
        encodeStringElement(descriptor, 2, value.description)
        encodeSerializableElement(descriptor, 3, listSerializer, value.entries)
    }
}*/