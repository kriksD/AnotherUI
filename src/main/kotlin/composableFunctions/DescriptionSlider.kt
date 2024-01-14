package composableFunctions

import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import colorBackground
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorText
import colorTextSecond
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
    var sliderValue by remember { mutableStateOf(value) }

    Column(
        modifier = modifier
    ) {
        Row {
            Spacer(modifier = Modifier.width(padding))
            Text(
                "$name: ${intStep?.let { sliderValue.roundToInt().roundToStep(intStep) } ?: ((sliderValue * 100).toInt() / 100F)}",
                color = if (enabled) colorText else colorTextSecond,
                fontSize = smallText,
                fontWeight = FontWeight.Bold,
            )
        }

        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                onValueChange(it)
            },
            onValueChangeFinished = {
                intStep?.let {
                    onValueChangeFinished.invoke(sliderValue.roundToInt().roundToStep(it).toFloat())
                } ?: run {
                    onValueChangeFinished.invoke((sliderValue * 100).toInt() / 100F)
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