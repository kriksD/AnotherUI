package composableFunctions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AppearDisappearAnimation(
    visible: Boolean,
    timeIn: Int = 300,
    timeOut: Int = 300,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = timeIn,
            ),
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = timeOut,
            ),
        ),
    ) {
        content()
    }
}