package composableFunctions

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import colorText

@Composable
fun LoadingIcon(
    tint: Color = colorText,
    contentDescription: String = "loading",
    modifier: Modifier = Modifier,
) {
    val animationState = rememberInfiniteTransition()
    val rotation by animationState.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Icon(
        Icons.Default.Refresh,
        contentDescription,
        tint = tint,
        modifier = modifier.rotate(rotation)
    )
}