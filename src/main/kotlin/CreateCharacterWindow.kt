import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import java.io.File
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.text.TextStyle
import character.*
import composableFunctions.CloseLine
import composableFunctions.UsualDivider
import composableFunctions.openFileDialog

@Composable
fun CreateCharacterWindow(
    window: ComposeWindow,
    onDone: (ACharacter) -> Unit = {},
    onClose: () -> Unit = {},
    onLoadFail: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparency)
            .border(border, colorBorder, RoundedCornerShape(corners))
            .padding(biggerPadding),
        verticalArrangement = Arrangement.spacedBy(padding),
    ) {
        CloseLine(
            modifier = Modifier.fillMaxWidth(),
            name = "New character",
            description = "cancel character creation",
            onClose = onClose
        )
        UsualDivider()
        LoadArea(
            window = window,
            modifier = Modifier
                .fillMaxWidth()
                .weight(8F),
            onLoadFail = onLoadFail,
            onDone = onDone,
        )
        Text("OR", color = colorText, fontSize = bigText, modifier = Modifier.align(Alignment.CenterHorizontally))
        CreateArea(
            modifier = Modifier
                .fillMaxWidth(0.8F)
                .align(Alignment.CenterHorizontally),
            onDone = onDone,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LoadArea(
    window: ComposeWindow,
    onDone: (ACharacter) -> Unit = {},
    onLoadFail: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isDroppable by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(if (isDroppable) colorBackgroundSelection else colorBackground, RoundedCornerShape(corners))
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .clickable {
                val file = openFileDialog(window, "", listOf(".webp", ".json"), false).firstOrNull()

                file?.let { f ->
                    loadNewCharacter(f)?.let {
                        onDone.invoke(it)

                    } ?: onLoadFail.invoke()
                } ?: onLoadFail.invoke()
            }
            .onExternalDrag(
                onDragStart = { externalDragValue ->
                    isDroppable = externalDragValue.dragData is androidx.compose.ui.DragData.FilesList
                },
                onDragExit = {
                    isDroppable = false
                },
                onDrop = { externalDragValue ->
                    isDroppable = false
                    val dragData = externalDragValue.dragData
                    if (dragData is androidx.compose.ui.DragData.FilesList) {
                        val paths = dragData.readFiles()
                        loadNewCharacter(File(paths.first().removePrefix("file:/")))?.let { nc ->
                            onDone.invoke(nc)
                        } ?: onLoadFail.invoke()
                    }
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("drag and drop", color = colorText, fontSize = smallText)
            Text("OR", color = colorText, fontSize = smallText)
            Text("click to load character", color = colorText, fontSize = smallText)
        }
    }
}

@Composable
private fun CreateArea(
    onDone: (ACharacter) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var characterName by remember { mutableStateOf("") }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(padding)
    ) {
        BasicTextField(
            modifier = Modifier
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .padding(padding)
                .weight(1F),
            value = characterName,
            decorationBox = { innerTextField ->
                if (characterName.isEmpty()) {
                    Text("Character name...", color = colorTextSecond, fontSize = normalText)
                }

                innerTextField()
            },
            textStyle = TextStyle(
                color = colorText,
                fontSize = normalText,
            ),
            cursorBrush = SolidColor(colorText),
            onValueChange = {
                characterName = it
            },
            singleLine = true,
            maxLines = 1,
        )

        Button(
            onClick = {
                if (characterName.isNotEmpty()) {
                    onDone.invoke(createNewCharacter(characterName))
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorBackgroundSecondLighter),
        ) {
            Text("create new", color = colorText, fontSize = normalText)
        }
    }
}