package properties.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import properties.settings.preset.SettingsPresets
import java.io.File

class SettingsContainer {
    var settings: Settings by mutableStateOf(Settings())
    var presets: SettingsPresets by mutableStateOf(SettingsPresets())

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val settingsFile = File("data/settings/settings.json")
    private val presetsFile = File("data/settings/presets.json")

    fun load() {
        File("data/settings").mkdirs()
        loadSettings()
        loadPresets()
    }

    private fun loadSettings() {
        settings = if (settingsFile.exists()) {
            try {
                json.decodeFromString<Settings>(settingsFile.readText())

            } catch (e: Exception) {
                e.printStackTrace()
                Settings()
            }
        } else { Settings() }
    }

    private fun loadPresets() {
        presets = if (presetsFile.exists()) {
            try {
                json.decodeFromString<SettingsPresets>(presetsFile.readText())

            } catch (e: Exception) {
                e.printStackTrace()
                SettingsPresets()
            }
        } else { SettingsPresets() }

        presets.load()
        presets.onSelect()
    }

    fun save() {
        settingsFile.writeText(json.encodeToString(settings))
        presetsFile.writeText(json.encodeToString(presets))
    }
}
