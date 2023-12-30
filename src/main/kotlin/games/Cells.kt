package games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import gameCellSize
import padding

@Composable
fun Cells(
    width: Int,
    height: Int,
    onClick: (x: Int, y: Int) -> Unit,
    isEnable: (x: Int, y: Int) -> Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (x: Int, y: Int) -> Unit,
) {
    Row(
        modifier = modifier
    ) {
        repeat(width) { x ->
            Column {
                repeat(height) { y ->
                    Box(
                        modifier = Modifier
                            .size(gameCellSize + padding)
                            .padding(start = padding, top = padding)
                            .clickable(enabled = isEnable(x, y), onClick = { onClick(x, y) })
                    ) {
                        content(x, y)
                    }
                }
            }
        }
    }
}