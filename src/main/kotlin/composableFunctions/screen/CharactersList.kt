package composableFunctions.screen

import ViewType
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import character.ACharacter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bigText
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorBorder
import colorText
import colorTextSecond
import composableFunctions.ADropDownMenuItem
import composableFunctions.ADropdownMenu
import composableFunctions.AppearDisappearAnimation
import composableFunctions.Grid
import corners
import imageHeight
import imageWidth
import menuWidth
import normalAnimationDuration
import normalText
import padding
import properties.Properties
import scrollbarThickness
import settings
import smallBorder
import smallIconSize
import tinyIconSize
import transparency
import transparencySecond

@Composable
fun CharacterList(
    modifier: Modifier = Modifier,
    list: List<ACharacter>,
    onCharacterClick: (ACharacter) -> Unit,
    onNewCharacter: () -> Unit,
    onDelete: (ACharacter) -> Unit,
) {
    var view by remember { mutableStateOf(settings.characters_list_view) }
    var searchSequence by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(padding),
            ) {
                Button(
                    onClick = onNewCharacter,
                    colors = ButtonDefaults.buttonColors(colorBackgroundSecondLighter),
                ) {
                    Text("+ add character", color = colorText, fontSize = normalText)
                }

                Button(
                    onClick = {
                        list.randomOrNull()?.let { onCharacterClick(it) }
                    },
                    colors = ButtonDefaults.buttonColors(colorBackgroundSecondLighter),
                ) {
                    Text("random character", color = colorText, fontSize = normalText)
                }

                BasicTextField(
                    modifier = Modifier
                        .background(colorBackground, RoundedCornerShape(corners))
                        .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                        .padding(padding),
                    value = searchSequence,
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(tinyIconSize),
                                tint = colorText
                            )

                            Box {
                                if (searchSequence.isEmpty()) {
                                    Text("Type to search...", color = colorTextSecond, fontSize = normalText)
                                }

                                innerTextField()
                            }
                        }
                    },
                    textStyle = TextStyle(color = colorText, fontSize = normalText),
                    cursorBrush = SolidColor(colorText),
                    onValueChange = { if (it.length <= 25) searchSequence = it },
                    singleLine = true,
                    maxLines = 1,
                )
            }

            ViewControls(
                view = view,
                onChange = {
                    view = it
                    settings.characters_list_view = it
                    Properties.saveSettings()
                },
            )
        }

        Crossfade(
            view,
            animationSpec = tween(normalAnimationDuration),
        ) { v ->
            when (v) {
                ViewType.List -> {
                    Row {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .weight(1F)
                                .verticalScroll(scrollState),
                        ) {
                            list.filter { it.jsonData.name.contains(searchSequence, ignoreCase = true) }.forEach {
                                CharacterCardList(
                                    it,
                                    Modifier.clickable {
                                        onCharacterClick.invoke(it)
                                    },
                                )
                            }
                        }

                        AppearDisappearAnimation(
                            scrollState.canScrollBackward || scrollState.canScrollForward,
                            normalAnimationDuration,
                        ) {
                            VerticalScrollbar(
                                ScrollbarAdapter(scrollState),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(colorBackground, LocalScrollbarStyle.current.shape),
                                style = LocalScrollbarStyle.current.copy(
                                    thickness = scrollbarThickness,
                                    unhoverColor = colorBackgroundSecondLighter,
                                    hoverColor = colorBackgroundSecond,
                                ),
                            )
                        }
                    }
                }
                ViewType.Grid -> {
                    Row {
                        val scrollState = rememberScrollState()
                        Grid(
                            columns = 4,
                            items = list
                                .filter { it.jsonData.name.contains(searchSequence, ignoreCase = true) }
                                .ifEmpty { listOf(0) },
                            modifier = Modifier
                                .weight(1F)
                                .verticalScroll(scrollState),
                        ) {
                            if (list.isEmpty()) {
                                CreateCharacterCardGrid(
                                    Modifier.clickable {
                                        onNewCharacter.invoke()
                                    }
                                )
                            }

                            if (it is ACharacter) {
                                CharacterCardGrid(
                                    character =  it,
                                    onDelete = onDelete,
                                    modifier = Modifier.clickable {
                                        onCharacterClick.invoke(it)
                                    },
                                )
                            }
                        }

                        AppearDisappearAnimation(
                            scrollState.canScrollBackward || scrollState.canScrollForward,
                            normalAnimationDuration,
                        ) {
                            VerticalScrollbar(
                                rememberScrollbarAdapter(scrollState),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(colorBackground, LocalScrollbarStyle.current.shape),
                                style = LocalScrollbarStyle.current.copy(
                                    thickness = scrollbarThickness,
                                    unhoverColor = colorBackgroundSecondLighter,
                                    hoverColor = colorBackgroundSecond,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewControls(
    view: ViewType,
    onChange: (ViewType) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(padding),
    ) {
        Icon(
            painterResource("dehaze.svg"),
            "show characters in list",
            tint = if (view == ViewType.List) colorTextSecond else colorText,
            modifier = Modifier
                .padding(padding)
                .width(smallIconSize)
                .aspectRatio(1F)
                .clickable(view != ViewType.List) {
                    onChange.invoke(ViewType.List)
                }
        )

        Icon(
            painterResource("view_cozy2.svg"),
            "show characters in grid",
            tint = if (view == ViewType.Grid) colorTextSecond else colorText,
            modifier = Modifier
                .padding(padding)
                .width(smallIconSize)
                .aspectRatio(1F)
                .clickable(view != ViewType.Grid) {
                    onChange.invoke(ViewType.Grid)
                }
        )
    }
}

@Composable
private fun CharacterCardList(
    character: ACharacter,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(padding)
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparencySecond)
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .fillMaxWidth(),
    ) {
        Image(
            character.image,
            contentDescription = "profile image of ${character.jsonData.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imageWidth, imageHeight)
                .padding(padding)
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .clip(RoundedCornerShape(corners)),
        )
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(character.jsonData.name, fontSize = bigText, color = colorText)
            Text(character.jsonData.personality, fontSize = normalText, color = colorText)
        }
    }
}

@Composable
private fun CharacterCardGrid(
    character: ACharacter,
    onDelete: (ACharacter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOptions by remember { mutableStateOf(false) }

    ADropdownMenu(
        expanded = showOptions,
        onDismissRequest = { showOptions = false },
        offset = DpOffset((-3).dp, 4.dp),
        modifier = Modifier.width(menuWidth)
            .background(SolidColor(colorBackgroundSecondLighter), RectangleShape, transparency)
            .border(smallBorder, colorBackgroundSecondLighter, RectangleShape),
    ) {
        ADropDownMenuItem(
            "delete",
            onClick = {
                showOptions = false
                onDelete(character)
            }
        )
    }

    Column(
        modifier = modifier
            .padding(padding)
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparencySecond)
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .fillMaxWidth()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Image(
            character.image,
            contentDescription = "profile image of ${character.jsonData.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageWidth / imageHeight)
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .clip(RoundedCornerShape(corners)),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(character.jsonData.name, fontSize = bigText, color = colorText)

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "character options",
                tint = colorText,
                modifier = Modifier
                    .size(smallIconSize)
                    .clickable {
                        showOptions = true
                    }
            )
        }
    }
}

@Composable
private fun CreateCharacterCardGrid(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(padding)
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparencySecond)
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .fillMaxWidth()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Image(
            painterResource("createCharacter.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageWidth / imageHeight)
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .clip(RoundedCornerShape(corners)),
        )
        Text("add character", fontSize = bigText, color = colorText)
    }
}