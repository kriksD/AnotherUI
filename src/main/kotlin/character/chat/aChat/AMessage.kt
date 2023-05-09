package character.chat.aChat

import character.chat.BasicMessageInfo
import character.chat.Message
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AMessage(
    override var name: String,
    override var is_user: Boolean,
    override var is_name: Boolean,
    override val send_date: Long?,
    override var mes: String,
    override val chid: Int?,
    override var swipe_id: Int?,
    override var swipes: MutableList<String>?,
    additional: AdditionalMessageInfo = AdditionalMessageInfo(emptyMap()),
) : Message {
    private var imageNameList: MutableMap<Int, String> = additional.images.toMutableMap()

    override fun toString(): String = "$name: $mes"

    @OptIn(ExperimentalSerializationApi::class)
    override fun toJSON(): String {
        val info = BasicMessageInfo(name, is_user, is_name, send_date, mes, chid, swipe_id, swipes)
        val json = Json { explicitNulls = false }
        return json.encodeToString(info)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun additionalInfoToJson(): String {
        val info = AdditionalMessageInfo(imageNameList)
        val json = Json { explicitNulls = false }
        return json.encodeToString(info)
    }

    override fun imageName(index: Int): String? = imageNameList[index]

    override fun updateImage(imageName: String, swipeIndex: Int) {
        imageNameList[swipeIndex] = imageName
    }

    override fun imageNames(): List<String> {
        val list = mutableListOf<String>()
        imageNameList.forEach { (_, name) -> list.add(name) }
        return list
    }

    /**
     * @return removed image names.
     */
    override fun removeRestImages(): List<String> {
        swipe_id?.let { sid ->
            var leave: String? = null
            val removed = mutableListOf<String>()

            imageNameList.forEach { (index, name) ->
                if (index != sid) {
                    removed.add(name)
                } else {
                    leave = name
                }
            }

            imageNameList.clear()
            leave?.let { imageNameList[0] = it }
            return removed
        }

        return emptyList()
    }
}