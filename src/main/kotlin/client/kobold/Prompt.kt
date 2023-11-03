package client.kobold

import kotlinx.serialization.Serializable

@Serializable
data class PromptResult(
    val results: List<LineResult>,
)

@Serializable
data class LineResult(
    val text: String,
)

@Serializable
data class Prompt(
    val prompt: String,
    val use_story: Boolean,
    val use_memory: Boolean,
    val use_authors_note: Boolean,
    val use_world_info: Boolean,
    val max_context_length: Int,
    val max_length: Int,
    val rep_pen: Float,
    val rep_pen_range: Int,
    val rep_pen_slope: Float,
    val temperature: Float,
    val tfs: Float,
    val top_a: Float,
    val top_k: Int,
    val top_p: Float,
    val typical: Float,
    val sampler_order: List<Int>,
)