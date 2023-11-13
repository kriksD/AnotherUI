import androidx.compose.runtime.toMutableStateList

class CheckManager<K>(
    vararg values: CheckValue<K>
) {
    private val values = values.toList().toMutableStateList()

    fun getKeys(): List<K> = values.map { it.key }

    fun get(key: K): Boolean? = values.find { it.key == key }?.checked

    fun getChecked(): K? = values.find { it.checked }?.key

    fun check(key: K) {
        repeat(values.size) { i ->
            val value = values[i]
            values[i] = value.copy(checked = value.key == key)
        }
    }
}

data class CheckValue<K>(
    val key: K,
    val checked: Boolean,
)