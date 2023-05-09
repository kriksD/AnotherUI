package properties

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class AColor(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int,
)

fun AColor.toColor() = Color(r, g, b, a)
fun Color.toAColor() = AColor((255 * red).toInt(), (255 * green).toInt(), (255 * blue).toInt(), (255 * alpha).toInt())