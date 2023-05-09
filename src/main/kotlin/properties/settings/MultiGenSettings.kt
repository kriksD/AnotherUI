package properties.settings

import kotlinx.serialization.Serializable

@Serializable
data class MultiGenSettings(
    var enabled: Boolean = false,
    var tokens_per_step: Int = 32,
    var tries: Int = 1,
)