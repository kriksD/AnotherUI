package composableFunctions

import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import biggerPadding
import colorBackground
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorText
import colorTextSecond
import normalText
import padding
import roundToStep
import smallText
import kotlin.math.roundToInt

@Composable
fun DescriptionSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float> = 0F..1F,
    intStep: Int? = null,
    enabled: Boolean = true,
    name: String = "",
    description: String = "",
    toolTip: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Row {
            Spacer(modifier = Modifier.width(padding))
            Text(
                "$name: ${intStep?.let { value.roundToInt().roundToStep(intStep) } ?: ((value * 100).toInt() / 100F)}",
                color = if (enabled) colorText else colorTextSecond,
                fontSize = smallText,
                fontWeight = FontWeight.Bold,
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = {
                intStep?.let {
                    onValueChangeFinished.invoke(value.roundToInt().roundToStep(it).toFloat())
                } ?: run {
                    onValueChangeFinished.invoke((value * 100).toInt() / 100F)
                }
            },
            valueRange = valueRange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = colorBackgroundSecond,
                activeTrackColor = colorBackgroundSecondLighter,
                disabledThumbColor = colorBackground
            )
        )

        Text(description, color = colorTextSecond, fontSize = smallText)
    }
}