package chat.newChat

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class AMessage2Serializer : KSerializer<AMessage2> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("chat.AChat2") {
        element<String>("name")
        element<Boolean>("is_user")
        element<Long>("send_date")
        element<Int>("swipe_id")
        element<List<String>>("swipes")
        element<Map<Int, String>>("images")
    }
    private val swipeListSerializer = ListSerializer(String.serializer())
    private val imageListSerializer = MapSerializer(Int.serializer(), String.serializer())

    override fun deserialize(decoder: Decoder): AMessage2 = decoder.decodeStructure(descriptor) {
        var name: String? = null
        var isUser: Boolean? = null
        var sendDate: Long? = null
        var swipeId: Int? = null
        var swipes: List<String>? = null
        var images: Map<Int, String>? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> name = decodeStringElement(descriptor, 0)
                1 -> isUser = decodeBooleanElement(descriptor, 1)
                2 -> sendDate = decodeLongElement(descriptor, 2)
                3 -> swipeId = decodeIntElement(descriptor, 3)
                4 -> swipes = decodeSerializableElement(descriptor, 4, swipeListSerializer)
                5 -> images = decodeSerializableElement(descriptor, 5, imageListSerializer)
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure AMessage2(
            name ?: throw SerializationException("Missing field \"name\""),
            isUser ?: throw SerializationException("Missing field \"is_user\""),
            sendDate ?: throw SerializationException("Missing field \"send_date\""),
            mutableStateOf(swipeId ?: throw SerializationException("Missing field \"swipe_id\"")),
            swipes?.toMutableStateList() ?: throw SerializationException("Missing field \"swipes\""),
            images?.toMutableMap() ?: throw SerializationException("Missing field \"images\""),
        )
    }

    override fun serialize(encoder: Encoder, value: AMessage2) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.name)
        encodeBooleanElement(descriptor, 1, value.isUser)
        encodeLongElement(descriptor, 2, value.sendDate)
        encodeIntElement(descriptor, 3, value.swipeId.value)
        encodeSerializableElement(descriptor, 4, swipeListSerializer, value.swipes)
        encodeSerializableElement(descriptor, 5, imageListSerializer, value.images)
    }
}