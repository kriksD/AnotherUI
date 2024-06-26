package properties

import androidx.compose.ui.graphics.ImageBitmap
import properties.settings.Settings
import properties.settings.SettingsContainer
import properties.settings.preset.Presets
import properties.settings.preset.SettingsPresets

object Properties {
    const val version = "0.0.0 alpha"

    private val userContainer: UserContainer = UserContainer()
    fun user(): User = userContainer.user
    fun getProfileImage(): ImageBitmap? = userContainer.getProfileImage()
    fun loadUser() { userContainer.load() }
    fun saveUser() { userContainer.save() }
    fun getUserPreset(): Presets<User> = userContainer.presets

    private val languageContainer: LanguageContainer = LanguageContainer()
    fun language(): Language = languageContainer.language
    fun loadLanguage(lang: String = "en") { languageContainer.load(lang) }

    private val settingsContainer: SettingsContainer = SettingsContainer()
    fun settings(): Settings = settingsContainer.settings
    fun loadSettings() { settingsContainer.load() }
    fun saveSettings() { settingsContainer.save() }
    fun getPresets(): SettingsPresets = settingsContainer.presets

    private val styleContainer: StyleContainer = StyleContainer()
    fun style(): Style = styleContainer.style
    fun loadStyle() { styleContainer.load() }
    fun saveStyle() { styleContainer.save() }
}
