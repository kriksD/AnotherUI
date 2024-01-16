package composableFunctions.screen

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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import character.ACharacter
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bigText
import biggerPadding
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorBorder
import colorText
import colorTextSecond
import composableFunctions.*
import corners
import emptyImageBitmap
import iconSize
import imageHeight
import imageWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import menuWidth
import normalAnimationDuration
import normalText
import padding
import scrollbarThickness
import shortAnimationDuration
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
    var searchSequence by remember { mutableStateOf("") }
    var favoriteFilter by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
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

                Crossfade(
                    favoriteFilter,
                    animationSpec = tween(shortAnimationDuration),
                ) { fav ->
                    Icon(
                        if (fav) painterResource("favorite.svg") else painterResource("favorite_o.svg"),
                        contentDescription = null,
                        tint = colorText,
                        modifier = Modifier
                            .padding(biggerPadding)
                            .size(iconSize)
                            .clickable { favoriteFilter = !favoriteFilter }
                    )
                }
            }
        }

        Row {
            val scrollState = rememberScrollState()
            var parentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

            Grid(
                columns = 4,
                items = list
                    .filter {
                        it.jsonData.name.contains(searchSequence, ignoreCase = true)
                                && if (favoriteFilter) it.metaData.favorite else true
                    }
                    .ifEmpty { listOf(0) },
                modifier = Modifier
                    .weight(1F)
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { c ->
                        parentCoordinates = c
                    },
            ) { char ->
                if (list.isEmpty()) {
                    CreateCharacterCardGrid(
                        Modifier.clickable {
                            onNewCharacter.invoke()
                        }
                    )
                }

                if (char is ACharacter) {
                    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
                    var isLoadingImage by remember { mutableStateOf(false) }

                    LaunchedEffect(scrollState.value, favoriteFilter, searchSequence) {
                        val isOnScreen = coordinates?.boundsInWindow()?.overlaps(
                            parentCoordinates?.boundsInWindow() ?: return@LaunchedEffect
                        ) ?: false

                        withContext(Dispatchers.IO) {
                            if (isOnScreen) {
                                isLoadingImage = true
                                char.loadImage()
                                isLoadingImage = false

                            } else {
                                char.unloadImage()
                            }
                        }
                    }

                    CharacterCardGrid(
                        character = char,
                        isLoadingImage = isLoadingImage,
                        onDelete = onDelete,
                        modifier = Modifier
                            .clickable {
                                onCharacterClick.invoke(char)
                            }
                            .onGloballyPositioned { c ->
                                coordinates = c
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

@Composable
private fun CharacterCardGrid(
    character: ACharacter,
    isLoadingImage: Boolean,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageWidth / imageHeight)
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .clip(RoundedCornerShape(corners)),
        ) {
            if (isLoadingImage) {
                LoadingIcon(
                    contentDescription = "loading profile image of ${character.jsonData.name}",
                    modifier = Modifier.size(iconSize).align(Alignment.Center)
                )
            } else {
                Image(
                    character.image ?: emptyImageBitmap,
                    contentDescription = "profile image of ${character.jsonData.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            var favorite by remember { mutableStateOf(character.metaData.favorite) }
            Crossfade(
                favorite,
                animationSpec = tween(shortAnimationDuration),
                modifier = Modifier.align(Alignment.TopEnd)
            ) { fav ->
                Icon(
                    if (fav) painterResource("favorite.svg") else painterResource("favorite_o.svg"),
                    contentDescription = null,
                    tint = colorBackgroundSecond,
                    modifier = Modifier
                        .padding(biggerPadding)
                        .size(iconSize)
                        .clickable {
                            favorite = !favorite
                            character.metaData.favorite = favorite
                            character.saveMeta()
                        }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(character.jsonData.name, fontSize = bigText, color = colorText, modifier = Modifier.weight(1F))

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