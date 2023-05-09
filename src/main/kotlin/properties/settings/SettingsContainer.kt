package properties.settings

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

class SettingsContainer {
    lateinit var settings: Settings
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    val file = File("data/settings.json")

    fun load() {
        settings = if (file.exists()) {
            try {
                json.decodeFromString(file.readText())

            } catch (e: Exception) { Settings() }
        } else { Settings() }
    }

    fun save() {
        file.writeText(json.encodeToString(settings))
    }
}
