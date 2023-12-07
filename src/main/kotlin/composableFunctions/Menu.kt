package composableFunctions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import colorBackground
import colorBorder
import colorText
import corners
import smallBorder
import tinyIconSize

@Composable
fun Menu(
    selectedItem: Any,
    items: List<Any>,
    onSelect: (Any) -> Unit,
    itemContent: @Composable (Any, MenuItemType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(colorBackground, RoundedCornerShape(corners))
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .width(IntrinsicSize.Max)
    ) {
        ADropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.align(Alignment.BottomStart).background(colorBackground).width(IntrinsicSize.Max),
        ) {
            items.forEach {item ->
                Box(
                    modifier = Modifier
                        .clickable {
                            onSelect(item)
                            expanded = false
                        }
                        .fillMaxWidth()
                ) {
                    itemContent(item, MenuItemType.List)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemContent(selectedItem, MenuItemType.Main)

            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colorText,
                modifier = Modifier
                    .size(tinyIconSize)
                    .clickable { if (items.isNotEmpty()) expanded = true },
            )
        }
    }
}

enum class MenuItemType{
    Main, List;
}