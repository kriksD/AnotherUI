package composableFunctions

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Grid(
    items: List<Any>,
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable (item: Any) -> Unit
) {
    val rows = items.chunked(columns)
    Column(modifier = modifier) {
        for (rowItems in rows) {
            Row {
                var remainingItems = columns
                for (item in rowItems) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        content(item)
                    }
                    remainingItems--
                }

                if (remainingItems > 0) {
                    Spacer(
                        modifier = Modifier.weight(remainingItems.toFloat())
                    )
                }
            }
        }
    }
}