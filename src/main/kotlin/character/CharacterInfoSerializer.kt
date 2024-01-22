package character

import kotlinx.serialization.ExperimentalSerializationApi
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

class CharacterInfoSerializer : KSerializer<CharacterInfo> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CharacterInfo") {
        element<String>("name")
        element<String>("description")
        element<String>("personality")
        element<String>("first_mes")
        element<MutableList<String>>("alternate_greetings")
        element<String>("avatar")
        element<String>("chat")
        element<String>("mes_example")
        element<String>("scenario")
        element<Long?>("edit_date")
        element<Long?>("create_date")
        element<Long?>("add_date")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): CharacterInfo = decoder.decodeStructure(descriptor) {
        var name: String? = null
        var description: String? = null
        var personality: String? = null
        var firstMessage: String? = null
        var alternateGreetings: MutableList<String>? = null
        var avatar: String? = null
        var chat: String? = null
        var messageExample: String? = null
        var scenario: String? = null
        var editDate: Long? = null
        var createDate: Long? = null
        var addDate: Long? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> name = decodeStringElement(descriptor, index)
                1 -> description = decodeStringElement(descriptor, index)
                2 -> personality = decodeStringElement(descriptor, index)
                3 -> firstMessage = decodeStringElement(descriptor, index)
                4 -> alternateGreetings = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer())).toMutableList()
                5 -> avatar = decodeStringElement(descriptor, index)
                6 -> chat = decodeStringElement(descriptor, index)
                7 -> messageExample = decodeStringElement(descriptor, index)
                8 -> scenario = decodeStringElement(descriptor, index)
                9 -> editDate = decodeNullableSerializableElement(descriptor, index, Long.serializer())
                10 -> createDate = decodeNullableSerializableElement(descriptor, index, Long.serializer())
                11 -> addDate = decodeNullableSerializableElement(descriptor, index, Long.serializer())
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure CharacterInfo(
            name ?: throw SerializationException("Missing name"),
            description ?: "",
            personality ?: "",
            firstMessage ?: "",
            alternateGreetings ?: mutableListOf(),
            avatar ?: "",
            chat ?: "",
            messageExample ?: "",
            scenario ?: "",
            editDate,
            createDate,
            addDate,
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: CharacterInfo) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.name)
        encodeStringElement(descriptor, 1, value.description)
        encodeStringElement(descriptor, 2, value.personality)
        encodeStringElement(descriptor, 3, value.firstMessage)
        encodeSerializableElement(descriptor, 4, ListSerializer(String.serializer()), value.alternateGreetings)
        encodeStringElement(descriptor, 5, value.avatar)
        encodeStringElement(descriptor, 6, value.chat)
        encodeStringElement(descriptor, 7, value.messageExample)
        encodeStringElement(descriptor, 8, value.scenario)
        encodeNullableSerializableElement(descriptor, 9, Long.serializer(), value.editDate)
        encodeNullableSerializableElement(descriptor, 10, Long.serializer(), value.createDate)
        encodeNullableSerializableElement(descriptor, 11, Long.serializer(), value.addDate)
    }
}
