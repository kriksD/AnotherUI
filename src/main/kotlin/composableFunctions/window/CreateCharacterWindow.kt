package composableFunctions.window

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
import bigText
import biggerPadding
import border
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecondLighter
import colorBackgroundSelection
import colorBorder
import colorText
import colorTextSecond
import composableFunctions.CloseLine
import composableFunctions.UsualDivider
import composableFunctions.openFileDialog
import corners
import kotlinx.coroutines.launch
import normalText
import padding
import smallBorder
import smallText
import transparency
import java.net.URI

@Composable
fun CreateCharacterWindow(
    window: ComposeWindow,
    onCreate: (String) -> Unit,
    onLoad: (File?) -> Unit,
    onLoadMultiple: (List<File?>) -> Unit,
    onClose: () -> Unit,
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
            onLoad = onLoad,
            onLoadMultiple = onLoadMultiple,
        )
        Text("OR", color = colorText, fontSize = bigText, modifier = Modifier.align(Alignment.CenterHorizontally))
        CreateArea(
            modifier = Modifier
                .fillMaxWidth(0.8F)
                .align(Alignment.CenterHorizontally),
            onCreate = onCreate,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LoadArea(
    window: ComposeWindow,
    onLoad: (File?) -> Unit,
    onLoadMultiple: (List<File?>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var isDroppable by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(if (isDroppable) colorBackgroundSelection else colorBackground, RoundedCornerShape(corners))
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .clickable {
                val file = openFileDialog(window, "", listOf(".webp", ".json"), false).firstOrNull()
                onLoad.invoke(file)
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

                        coroutineScope.launch {
                            if (paths.size == 1) {
                                onLoad(File(URI(paths.first())))
                            }

                            if (paths.size > 1) {
                                val files = mutableListOf<File>()

                                paths.forEach { p ->
                                    files.add(File(URI(p)))
                                }

                                onLoadMultiple(files)
                            }
                        }
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
    onCreate: (String) -> Unit,
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
                if (characterName.isNotBlank()) {
                    onCreate.invoke(characterName)
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorBackgroundSecondLighter),
        ) {
            Text("create new", color = colorText, fontSize = normalText)
        }
    }
}