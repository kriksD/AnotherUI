package properties.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

class SettingsContainer {
    var settings: Settings by mutableStateOf(Settings())
    private val presets: SnapshotStateList<Settings> = mutableStateListOf()
    val presetsList: List<Settings> get() = presets

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val file = File("data/settings/settings.json")
    private val presetsFolder = File("data/settings/presets")

    fun load() {
        presetsFolder.mkdirs()
        loadSettings()
        loadPresets()
    }

    private fun loadSettings() {
        settings = if (file.exists()) {
            try {
                json.decodeFromString<Settings>(file.readText())

            } catch (e: Exception) {
                e.printStackTrace()
                Settings()
            }
        } else { Settings() }
    }

    private fun loadPresets() {
        if (!presetsFolder.exists()) return

        presetsFolder.listFiles()?.forEach {
            if (it.extension != "json") return@forEach

            try {
                val preset = json.decodeFromString<Settings>(it.readText())
                presets.add(preset)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun save() {
        file.writeText(json.encodeToString(settings))
    }

    fun createPreset(name: String) {
        val presetFile = File("data/settings/presets/$name.json")
        settings.presetName = name
        presets.add(settings)
        presetFile.writeText(json.encodeToString(settings))
    }

    fun saveCurrentPreset() {
        if (settings.presetName == null) return

        val presetFile = File("data/settings/presets/${settings.presetName}.json")
        presetFile.writeText(json.encodeToString(settings))
        presets.indexOfFirst { it.presetName == settings.presetName }.let {
            if (it != -1) presets[it] = settings
        }
    }

    fun deletePreset(name: String) {
        val presetFile = File("data/settings/presets/$name.json")
        presetFile.delete()
        presets.removeIf { it.presetName == name }
    }

    fun selectPreset(name: String) {
        settings = presets.first { it.presetName == name }.copy()
    }
}
