package properties.settings

import kotlinx.serialization.Serializable

@Serializable
data class GeneratingSettings(
    var max_context_length: Int = 1600,
    var max_length: Int = 200,
    var rep_pen: Float = 1.08F,
    var rep_pen_range: Int = 1024,
    var rep_pen_slope: Float = 0.9F,
    var temperature: Float = 0.65F,
    var tfs: Float = 0.65F,
    var top_a: Float = 0.9F,
    var top_k: Int = 0,
    var top_p: Float = 0.9F,
    var typical: Float = 1F,
)
