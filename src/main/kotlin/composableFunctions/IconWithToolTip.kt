package composableFunctions

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import colorText
import smallIconSize

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IconWithToolTip(
    icon: ImageVector,
    contentDescription: String?,
    toolTip: String = "",
    tint: Color = colorText,
    modifier: Modifier = Modifier,
) {
    var showToolTip by remember { mutableStateOf(false) }

    Box {
        Icon(
            icon,
            contentDescription,
            tint = tint,
            modifier = modifier
                .onPointerEvent(PointerEventType.Enter) {
                    showToolTip = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    showToolTip = false
                },
        )

        ATooltip(
            showToolTip,
            toolTip,
            onDismissRequest = {
                showToolTip = false
            },
            offset = DpOffset(smallIconSize + 12.dp, -(smallIconSize + smallIconSize / 2 - 4.dp))
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IconWithToolTip(
    painter: Painter,
    contentDescription: String?,
    toolTip: String = "",
    tint: Color = colorText,
    modifier: Modifier = Modifier,
) {
    var showToolTip by remember { mutableStateOf(false) }

    Box {
        Icon(
            painter,
            contentDescription,
            tint = tint,
            modifier = modifier
                .onPointerEvent(PointerEventType.Enter) {
                    showToolTip = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    showToolTip = false
                },
        )

        ATooltip(
            showToolTip,
            toolTip,
            onDismissRequest = {
                showToolTip = false
            },
            offset = DpOffset(smallIconSize + 12.dp, -(smallIconSize + smallIconSize / 2 - 4.dp))
        )
    }
}