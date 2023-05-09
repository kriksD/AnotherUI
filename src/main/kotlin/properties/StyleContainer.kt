package properties

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class StyleContainer {
    lateinit var style: Style

    val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val file = File("data/style.json")

    fun load() {
        style = try {
            if (file.exists()) {
                json.decodeFromString(file.readText())
            } else {
                Style()
            }
        } catch (e: Exception) {
            Style()
        }
    }

    fun save() {
        file.writeText(json.encodeToString(style))
    }
}