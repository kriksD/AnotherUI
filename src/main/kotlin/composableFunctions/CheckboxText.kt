package composableFunctions

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorText
import normalText

@Composable
fun CheckboxText(
    text: String,
    enabled: Boolean,
    onChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            enabled,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = colorBackgroundSecond,
                uncheckedColor = colorBackgroundSecondLighter,
                checkmarkColor = colorText,
            )
        )
        Text(text, color = colorText, fontSize = normalText)
    }
}