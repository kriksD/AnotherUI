import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
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
import androidx.compose.ui.res.painterResource
import composableFunctions.AppearDisappearAnimation
import properties.Properties

@Composable
fun CharacterList(
    modifier: Modifier = Modifier,
    list: MutableList<ACharacter>,
    onCharacterClick: (ACharacter) -> Unit,
    onNewCharacter: () -> Unit,
) {
    var view by remember { mutableStateOf(settings.characters_list_view) }

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onNewCharacter,
                colors = ButtonDefaults.buttonColors(colorBackgroundSecondLighter),
            ) {
                Text("+ add character", color = colorText, fontSize = normalText)
            }

            ViewControls(
                view = view,
                onChange = {
                    view = it
                    settings.characters_list_view = it
                    Properties.saveSettings()
                }
            )
        }

        Crossfade(view) { v ->
            when (v) {
                ViewType.List -> {
                    Row {
                        val scrollState = rememberLazyListState()
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier
                                .weight(1F),
                        ) {
                            items(list) {
                                CharacterCardList(
                                    it,
                                    Modifier.clickable {
                                        onCharacterClick.invoke(it)
                                    },
                                )
                            }
                        }

                        AppearDisappearAnimation(scrollState.canScrollBackward || scrollState.canScrollForward) {
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
                        val scrollState = rememberLazyGridState()
                        LazyVerticalGrid(
                            GridCells.Fixed(4),
                            state = scrollState,
                            modifier = Modifier.weight(1F),
                        ) {
                            if (list.isEmpty()) {
                                item {
                                    CreateCharacterCardGrid(
                                        Modifier.clickable {
                                            onNewCharacter.invoke()
                                        }
                                    )
                                }
                            }
                            items(list) {
                                CharacterCardGrid(
                                    it,
                                    Modifier.clickable {
                                        onCharacterClick.invoke(it)
                                    },
                                )
                            }
                        }

                        AppearDisappearAnimation(scrollState.canScrollBackward || scrollState.canScrollForward) {
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
            contentDescription = "profile image of ${character.jsonData}",
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
            character.image,
            contentDescription = "profile image of ${character.jsonData}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageWidth / imageHeight)
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .clip(RoundedCornerShape(corners)),
        )
        Text(character.jsonData.name, fontSize = bigText, color = colorText)
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