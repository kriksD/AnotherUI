package properties.settings.preset

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

class Presets<T>(
    selectedPreset: String? = null,
    private val folder: File,
    private val serializer: KSerializer<T>,
    private val createValue: (T?) -> T,
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val presets: SnapshotStateMap<String, T> = mutableStateMapOf()
    var selectedPresetName by mutableStateOf(selectedPreset)
        private set
    val selectedPresetNameForDisplay get() = selectedPresetName ?: "<custom>"
    val selectedPreset get() = selectedPresetName?.let { presets[it] }
    val presetNames get() = presets.keys

    init {
        folder.mkdirs()
    }

    fun load() {
        folder.mkdirs()

        folder.listFiles()?.forEach {
            if (it.extension != "json") return@forEach

            try {
                val preset = json.decodeFromString(serializer, it.readText())
                presets[it.nameWithoutExtension] = preset

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createPreset(name: String) {
        if (presets.keys.contains(name)) return

        presets[name] = createValue(selectedPreset)
        selectPreset(name)

        val presetFile = folder.resolve("$name.json")
        val preset = presets[name] ?: return
        presetFile.writeText(json.encodeToString(serializer, preset))
    }

    fun saveSelectedPreset(newValue: T) {
        if (selectedPreset == null) return

        presets[selectedPresetName!!] = newValue
        val presetFile = folder.resolve("$selectedPresetName.json")
        val preset = presets[selectedPresetName] ?: return
        presetFile.writeText(json.encodeToString(serializer, preset))
    }

    fun deletePreset(name: String) {
        presets.remove(name)
        val presetFile = folder.resolve("$name.json")
        presetFile.delete()

        selectedPresetName = null
    }


    private val onSelect = mutableListOf<(T) -> Unit>()
    fun onSelect(action: (T) -> Unit) {
        onSelect.add(action)
    }

    fun selectPreset(name: String?) {
        if (!presets.keys.contains(name)) return

        selectedPresetName = name

        selectedPreset?.let { preset -> onSelect.forEach { it(preset) } }
    }
}