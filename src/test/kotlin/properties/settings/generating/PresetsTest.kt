package properties.settings.generating

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import properties.settings.preset.Presets
import java.io.File

class PresetsTest {

    @Test
    fun `test generating setting preset creating`() {
        val presetsClass = Presets(
            folder = File("data/settings/presets/generating"),
            serializer = GeneratingSettings.serializer(),
            createValue = { GeneratingSettings() },
        )
        val presetName = "test"

        presetsClass.createPreset(presetName)

        assertNotNull(presetsClass.selectedPresetName)
        assertEquals(presetName, presetsClass.selectedPresetName)
        assertNotNull(presetsClass.selectedPreset)

        val file = File("data/settings/presets/generating/$presetName.json")
        assertTrue(file.exists())
        file.delete()
    }

    @Test
    fun `test generating setting preset saving`() {
        val presetsClass = Presets(
            folder = File("data/settings/presets/generating"),
            serializer = GeneratingSettings.serializer(),
            createValue = { GeneratingSettings() },
        )
        val presetName = "test"
        val newTemperatureValue = 2.0F

        presetsClass.createPreset(presetName)
        presetsClass.selectedPreset?.temperature = newTemperatureValue
        presetsClass.selectedPreset?.let { presetsClass.saveSelectedPreset(it) }

        val file = File("data/settings/presets/generating/$presetName.json")
        assertTrue(file.exists())

        val presetsClass2 = Presets(
            folder = File("data/settings/presets/generating"),
            serializer = GeneratingSettings.serializer(),
            createValue = { GeneratingSettings() },
        )
        presetsClass2.load()
        presetsClass2.selectPreset(presetName)

        assertEquals(newTemperatureValue, presetsClass2.selectedPreset?.temperature)

        file.delete()
    }

    @Test
    fun `test generating setting preset deleting`() {
        val presetsClass = Presets(
            folder = File("data/settings/presets/generating"),
            serializer = GeneratingSettings.serializer(),
            createValue = { GeneratingSettings() },
        )
        val presetName = "test"
        val presetName2 = "test2"

        presetsClass.createPreset(presetName)
        presetsClass.selectedPreset?.let { presetsClass.saveSelectedPreset(it) }
        presetsClass.createPreset(presetName2)
        presetsClass.selectedPreset?.let { presetsClass.saveSelectedPreset(it) }

        val file = File("data/settings/presets/generating/$presetName.json")
        assertTrue(file.exists())

        val file2 = File("data/settings/presets/generating/$presetName2.json")
        assertTrue(file2.exists())

        assertNotNull(presetsClass.selectedPreset)
        assertEquals(presetName2, presetsClass.selectedPresetName)

        presetsClass.deletePreset(presetName2)
        assertNull(presetsClass.selectedPreset)
        assertFalse(file2.exists())

        file.delete()
        file2.delete()
    }
}