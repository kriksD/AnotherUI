package composableFunctions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AppearDisappearAnimation(
    visible: Boolean,
    duration: Int = 300,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = duration,
            ),
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = duration,
            ),
        ),
    ) {
        content()
    }
}