package composableFunctions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import biggerPadding
import border
import colorBorder
import padding

@Composable
fun UsualDivider(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(padding),
) {
    Divider(
        modifier = modifier
            .clip(RoundedCornerShape(100)),
        color = colorBorder,
        thickness = border,
    )
    Spacer(modifier = Modifier.height(biggerPadding))
}