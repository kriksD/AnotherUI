package composableFunctions.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import biggerPadding
import border
import colorBackgroundLighter
import colorBackgroundSecondLighter
import colorBorder
import colorText
import composableFunctions.CheckboxText
import composableFunctions.CloseLine
import composableFunctions.UsualDivider
import corners
import normalText
import padding
import transparency

@Composable
fun DeleteCharacterWindow(
    characterName: String,
    onAccept: (Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparency)
            .border(border, colorBorder, RoundedCornerShape(corners))
            .padding(biggerPadding),
        verticalArrangement = Arrangement.spacedBy(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CloseLine(
            modifier = Modifier.fillMaxWidth(),
            name = "Delete \"$characterName\" character?",
            description = "cancel the character deletion",
            onClose = onCancel
        )

        UsualDivider()

        Text(
            text = "Are you sure you want to delete \"$characterName\" character?",
            color = colorText,
            fontSize = normalText,
        )

        var withChats by remember { mutableStateOf(false) }

        CheckboxText(
            "Delete chats",
            withChats,
            onChange = { withChats = it },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(colorBackgroundSecondLighter),
            ) {
                Text("Cancel", color = colorText, fontSize = normalText)
            }

            Button(
                onClick = { onAccept(withChats) },
                colors = ButtonDefaults.buttonColors(colorBackgroundSecondLighter),
            ) {
                Text("Delete", color = colorText, fontSize = normalText)
            }
        }
    }
}