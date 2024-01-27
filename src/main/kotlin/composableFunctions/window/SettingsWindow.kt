package composableFunctions.window

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bigIconSize
import bigText
import biggerPadding
import border
import character.ACharacter
import client.kobold.KoboldAIClient
import client.stablediffusion.StableDiffusionWebUIClient
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorBorder
import colorCheckConnection
import colorConnection
import colorNoConnection
import colorText
import colorTextError
import colorTextSecond
import composableFunctions.*
import copyAndGetImage
import corners
import emptyImageBitmap
import getImageBitmap
import imageHeight
import imageWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loadAllImages
import longAnimationDuration
import normalAnimationDuration
import normalText
import padding
import prompt.PromptType
import prompt.samplerName
import properties.Properties
import properties.settings.preset.Presets
import settings
import smallBorder
import smallText
import style
import tinyIconSize
import toState
import transparency
import transparencyLight
import user
import java.io.File
import java.net.URI

private enum class SettingsScreen {
    AI, StableDiffusion, Appearance, User, Character;

    override fun toString(): String = when (this) {
        AI -> "AI"
        StableDiffusion -> "stable diffusion"
        Appearance -> "appearance"
        User -> "user"
        Character -> "character"
    }
}

@Composable
fun SettingsWindow(
    window: ComposeWindow,
    character: ACharacter? = null,
    onBackgroundChange: (ImageBitmap) -> Unit = {},
    onClose: () -> Unit = {},
    onForceSaving: () -> Unit = {},
    onCharacterRedacted: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var screen by remember { mutableStateOf(SettingsScreen.AI) }

    Column(
        modifier = modifier
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparency)
            .border(border, colorBorder, RoundedCornerShape(corners))
            .padding(biggerPadding),
    ) {
        CloseLine(
            modifier = Modifier.fillMaxWidth(),
            name = "Settings",
            description = "close settings",
            onClose = onClose
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            ScreenControls(
                screen,
                isCharacter = character != null,
                onChange = {
                    screen = it
                },
            )
        }
        UsualDivider()
        Crossfade(
            screen,
            animationSpec = tween(normalAnimationDuration),
        ) { screen2 ->
            when (screen2) {
                SettingsScreen.AI -> AIScreen(
                    modifier = Modifier.weight(1F),
                )

                SettingsScreen.StableDiffusion -> StableDiffusionScreen(
                    modifier = Modifier.weight(1F),
                )

                SettingsScreen.Character -> CharacterScreen(
                    window = window,
                    character = character,
                    onForceSaving = onForceSaving,
                    onCharacterRedacted = onCharacterRedacted,
                    modifier = Modifier.weight(1F),
                )

                SettingsScreen.Appearance -> AppearanceSettings(
                    window = window,
                    onBackgroundChange = onBackgroundChange,
                    modifier = Modifier.weight(1F),
                )

                SettingsScreen.User -> UserSettings(
                    window = window,
                    modifier = Modifier.weight(1F),
                )
            }
        }
    }
}

@Composable
private fun ScreenControls(
    screen: SettingsScreen,
    isCharacter: Boolean,
    onChange: (SettingsScreen) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(biggerPadding)
    ) {
        SettingsScreen.values().forEach {
            if (it != SettingsScreen.Character || isCharacter) {
                ScreenButton(
                    it,
                    screen,
                    onClick = {
                        onChange.invoke(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun ScreenButton(
    screenFor: SettingsScreen,
    screen: SettingsScreen,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Text(
        screenFor.toString(),
        color = if (screen != screenFor) colorText else colorTextSecond,
        fontSize = bigText,
        textDecoration = TextDecoration.Underline,
        modifier = modifier
            .background(
                SolidColor(colorBackgroundSecondLighter),
                RoundedCornerShape(corners),
                if (screen != screenFor) 0F else 0.5F,
            )
            .padding(padding)
            .clickable(screen != screenFor, onClick = onClick),
    )
}

@Composable
private fun AIScreen(
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        state = scrollState,
        modifier = modifier
    ) {
        item { ConnectionCheck() }
        item { UsualDivider() }
        item { PresetControls(
            name = "Generating settings presets:",
            presets = Properties.getPresets().generatingPresets,
            valueToSave = settings.generating,
        ) }
        item { UsualDivider() }
        item { Generating() }
        item { UsualDivider() }
        item { PresetControls(
            name = "Prompt presets:",
            presets = Properties.getPresets().promptPresets,
            valueToSave = settings.promptSettings,
        ) }
        item { UsualDivider() }
        item { Prompt() }
        item { UsualDivider() }
    }
}

@Composable
private fun ConnectionCheck() {
    val coroutineScope = rememberCoroutineScope()

    Column {
        var connectionText by remember { mutableStateOf(KoboldAIClient.connectionStatusString) }
        var connectionColor by remember { mutableStateOf(KoboldAIClient.connectionStatusColor) }
        var modelName by remember {
            mutableStateOf(
                if (KoboldAIClient.connectionStatus) KoboldAIClient.modelName ?: "" else ""
            )
        }
        val connectionLambda by remember {
            mutableStateOf({
                coroutineScope.launch {
                    if (KoboldAIClient.connectionStatus) {
                        modelName = KoboldAIClient.modelName ?: ""
                        connectionText = "connection exists"
                        connectionColor = colorConnection

                    } else {
                        modelName = ""
                        connectionText = "no connection"
                        connectionColor = colorNoConnection
                    }
                }
            })
        }

        var checkConnectionExist by remember { mutableStateOf(false) }
        if (!checkConnectionExist) {
            checkConnectionExist = true
            KoboldAIClient.onConnectionChange("settingsWindow") {
                connectionLambda.invoke()
            }
        }

        Text(
            "KoboldAI link:",
            modifier = Modifier.fillMaxWidth(),
            color = colorText,
            fontSize = normalText,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            OutlinedTextField(
                settings.link,
                onValueChange = { settings.link = it },
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                modifier = Modifier.weight(1F),
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = colorText,
                    focusedIndicatorColor = colorBackgroundSecond,
                ),
                placeholder = { Text("Type KoboldAI link...", color = colorTextSecond) },
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        modelName = ""
                        connectionText = "checking connection..."
                        connectionColor = colorCheckConnection

                        KoboldAIClient.checkModelName()
                        connectionLambda.invoke()
                    }
                },
                colors = ButtonDefaults.buttonColors(colorBackgroundSecondLighter),
            ) {
                Text("connect", fontSize = smallText, color = colorText)
            }
        }
        Text(
            connectionText,
            modifier = Modifier.fillMaxWidth(),
            color = connectionColor,
            fontSize = smallText,
        )
        Text(
            modelName,
            modifier = Modifier.fillMaxWidth(),
            color = connectionColor,
            fontSize = smallText,
        )
    }
}

@Composable
private fun <T> PresetControls(
    name: String,
    presets: Presets<T>,
    valueToSave: T,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(padding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            color = colorText,
            fontSize = normalText,
        )

        var editedPresetName by remember { mutableStateOf(presets.selectedPresetNameForDisplay) }

        Menu(
            presets.selectedPresetNameForDisplay,
            listOf("<custom>") + presets.presetNames,
            onSelect = {
                if (it == "<custom>") {
                    presets.selectPreset(null)
                    editedPresetName = "<custom>"
                    return@Menu
                }

                presets.selectPreset(it as String)
                editedPresetName = it
            },
            itemContent = { item, type ->
                if (type == MenuItemType.Main) {
                    BasicTextField(
                        editedPresetName,
                        onValueChange = {
                            editedPresetName = it
                        },
                        textStyle = TextStyle(
                            color = colorText,
                            fontSize = normalText,
                        ),
                        cursorBrush = SolidColor(colorText),
                        modifier = Modifier
                            .weight(1F)
                            .background(colorBackground, RoundedCornerShape(corners))
                            .padding(padding),
                    )

                } else {
                    Text(
                        item as String,
                        color = if (item == presets.selectedPresetName || (presets.selectedPresetName == null && item == "<custom>")) colorTextSecond else colorText,
                        fontSize = normalText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorBackground)
                            .padding(padding),
                    )
                }
            },
            modifier = Modifier,
        )

        IconWithToolTip(
            painterResource("save.svg"),
            contentDescription = "Save preset",
            toolTip = "Save preset",
            tint = colorText,
            modifier = Modifier.size(tinyIconSize).clickable {
                if (editedPresetName != presets.selectedPresetName) {
                    presets.createPreset(editedPresetName)
                } else {
                    presets.saveSelectedPreset(valueToSave)
                    editedPresetName = presets.selectedPresetNameForDisplay
                }
            },
        )

        IconWithToolTip(
            Icons.Default.Delete,
            contentDescription = "Delete preset",
            toolTip = "Delete preset",
            tint = colorText,
            modifier = Modifier.size(tinyIconSize).clickable {
                presets.deletePreset(editedPresetName)
                editedPresetName = presets.selectedPresetNameForDisplay
            },
        )
    }
}

@Composable
private fun Generating() {
    Column(
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Amount of generation",
                value = settings.generating.maxLength.toFloat(),
                onValueChange = { settings.generating.maxLength = it.toInt() },
                onValueChangeFinished = { settings.generating.maxLength = it.toInt() },
                valueRange = settings.generating.maxLengthValueRange,
                intStep = 8,
                modifier = Modifier.weight(1F),
            )

            DescriptionSlider(
                name = "Max Context Length",
                value = settings.generating.maxContextLength.toFloat(),
                onValueChange = { settings.generating.maxContextLength = it.toInt() },
                onValueChangeFinished = { settings.generating.maxContextLength = it.toInt() },
                valueRange = settings.generating.maxContextLengthValueRange,
                intStep = 64,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Temperature",
                value = settings.generating.temperature,
                onValueChange = { settings.generating.temperature = it },
                onValueChangeFinished = { settings.generating.temperature = it },
                valueRange = settings.generating.temperatureValueRange,
                modifier = Modifier.weight(1F),
            )

            DescriptionSlider(
                name = "Dynamic Temperature Range",
                value = settings.generating.dynatempRange,
                onValueChange = { settings.generating.dynatempRange = it },
                onValueChangeFinished = { settings.generating.dynatempRange = it },
                valueRange = settings.generating.dynatempRangeValueRange,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Repetition Penalty",
                value = settings.generating.repPen,
                onValueChange = { settings.generating.repPen = it },
                onValueChangeFinished = { settings.generating.repPen = it },
                valueRange = settings.generating.repPenValueRange,
                modifier = Modifier.weight(1F),
            )

            DescriptionSlider(
                name = "Repetition Penalty Range",
                value = settings.generating.repPenRange.toFloat(),
                onValueChange = { settings.generating.repPenRange = it.toInt() },
                onValueChangeFinished = { settings.generating.repPenRange = it.toInt() },
                valueRange = settings.generating.repPenRangeValueRange,
                intStep = 64,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Rep Pen Slope",
                value = settings.generating.repPenSlope,
                onValueChange = { settings.generating.repPenSlope = it },
                onValueChangeFinished = { settings.generating.repPenSlope = it },
                valueRange = settings.generating.repPenSlopeValueRange,
                modifier = Modifier.weight(1F),
            )

            Spacer(Modifier.weight(1F))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Min P Sampling",
                value = settings.generating.minP,
                onValueChange = { settings.generating.minP = it },
                onValueChangeFinished = { settings.generating.minP = it },
                valueRange = settings.generating.minPValueRange,
                modifier = Modifier.weight(1F),
            )

            DescriptionSlider(
                name = "Top P Sampling",
                value = settings.generating.topP,
                onValueChange = { settings.generating.topP = it },
                onValueChangeFinished = { settings.generating.topP = it },
                valueRange = settings.generating.topPValueRange,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Top K Sampling",
                value = settings.generating.topK.toFloat(),
                onValueChange = { settings.generating.topK = it.toInt() },
                onValueChangeFinished = { settings.generating.topK = it.toInt() },
                valueRange = settings.generating.topKValueRange,
                intStep = 1,
                modifier = Modifier.weight(1F)
            )

            DescriptionSlider(
                name = "Top A Sampling",
                value = settings.generating.topA,
                onValueChange = { settings.generating.topA = it },
                onValueChangeFinished = { settings.generating.topA = it },
                valueRange = settings.generating.topAValueRange,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Typical Sampling",
                value = settings.generating.typical,
                onValueChange = { settings.generating.typical = it },
                onValueChangeFinished = { settings.generating.typical = it },
                valueRange = settings.generating.typicalValueRange,
                modifier = Modifier.weight(1F),
            )

            DescriptionSlider(
                name = "Tail Free Sampling",
                value = settings.generating.tfs,
                onValueChange = { settings.generating.tfs = it },
                onValueChangeFinished = { settings.generating.tfs = it },
                valueRange = settings.generating.tfsValueRange,
                modifier = Modifier.weight(1F),
            )
        }

        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(padding)
            ) {
                Text(
                    text = "Seed:",
                    color = colorText,
                    fontSize = normalText,
                )

                BasicTextField(
                    modifier = Modifier
                        .background(colorBackground, RoundedCornerShape(corners))
                        .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                        .padding(padding),
                    value = settings.generating.seed.toString(),
                    textStyle = TextStyle(color = colorText, fontSize = normalText),
                    cursorBrush = SolidColor(colorText),
                    onValueChange = { value ->
                        if (value == "-" || value.isBlank()) {
                            settings.generating.seed = -1
                        }

                        value.toIntOrNull()?.let {
                            settings.generating.seed = it
                        }
                    },
                    singleLine = true,
                    maxLines = 1,
                )
            }

            Column {
                Text(
                    text = "Sampler Order:",
                    color = colorText,
                    fontSize = normalText,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(padding),
                    modifier = Modifier.width(IntrinsicSize.Max),
                ) {
                    settings.generating.samplerOrder.forEachIndexed { index, sampler ->
                        DraggableSamplerLine(
                            number = sampler,
                            name = samplerName(sampler),
                            onUp = {
                                settings.generating.samplerOrder.removeAt(index)
                                settings.generating.samplerOrder.add(index - 1, sampler)
                            },
                            onDown = {
                                settings.generating.samplerOrder.removeAt(index)
                                settings.generating.samplerOrder.add(index + 1, sampler)
                            },
                            upAvailable = index > 0,
                            downAvailable = index < settings.generating.samplerOrder.lastIndex,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableSamplerLine(
    number: Int,
    name: String,
    onUp: () -> Unit,
    onDown: () -> Unit,
    upAvailable: Boolean,
    downAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(colorBackground, RoundedCornerShape(corners))
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .padding(padding),
    ) {
        Column {
            if (upAvailable) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Up",
                    tint = colorText,
                    modifier = Modifier
                        .size(tinyIconSize)
                        .clickable { onUp() },
                )
            }

            if (downAvailable) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Down",
                    tint = colorText,
                    modifier = Modifier
                        .size(tinyIconSize)
                        .clickable { onDown() },
                )
            }
        }

        Text(
            text = "$number - $name",
            color = colorText,
            fontSize = normalText,
        )
    }
}

@Composable
private fun Prompt() {
    CheckboxText(
        "instruct",
        settings.promptSettings.type == PromptType.Instruct,
        onChange = { settings.promptSettings.type = if (it) PromptType.Instruct else PromptType.Chat },
    )

    TextFieldWithTokens(
        settings.promptSettings.pattern,
        "Pattern",
        onValueChange = { settings.promptSettings.pattern = it },
        showTokens = false,
        modifier = Modifier.height(140.dp),
    )

    TextFieldWithTokens(
        settings.promptSettings.systemPrompt,
        "System Prompt",
        onValueChange = { settings.promptSettings.systemPrompt = it },
        singleLine = true,
    )

    TextFieldWithTokens(
        settings.promptSettings.systemInstruct,
        "System Instruction",
        onValueChange = { settings.promptSettings.systemInstruct = it },
        showTokens = false,
        singleLine = true,
    )

    TextFieldWithTokens(
        settings.promptSettings.userInstruct,
        "User Instruction",
        onValueChange = { settings.promptSettings.userInstruct = it },
        showTokens = false,
        singleLine = true,
    )

    TextFieldWithTokens(
        settings.promptSettings.modelInstruct,
        "Model Instruction",
        onValueChange = { settings.promptSettings.modelInstruct = it },
        showTokens = false,
        singleLine = true,
    )

    TextFieldWithTokens(
        settings.promptSettings.stopSequence,
        "End Tokens",
        onValueChange = { settings.promptSettings.stopSequence = it },
        showTokens = false,
        singleLine = true,
    )
}

@Composable
private fun StableDiffusionScreen(
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        state = scrollState,
        modifier = modifier
    ) {
        item { SDConnectionCheck() }
        item { UsualDivider() }
        item { PresetControls(
            name = "Image generating settings presets:",
            presets = Properties.getPresets().imageGeneratingPresets,
            valueToSave = settings.imageGenerating,
        ) }
        item { UsualDivider() }
        item { SDGenerating() }
        item { UsualDivider() }
    }
}

@Composable
private fun SDConnectionCheck() {
    val coroutineScope = rememberCoroutineScope()

    Column {
        var connectionText by remember { mutableStateOf(StableDiffusionWebUIClient.connectionStatusString) }
        var connectionColor by remember { mutableStateOf(StableDiffusionWebUIClient.connectionStatusColor) }
        var modelName by remember {
            mutableStateOf(
                if (StableDiffusionWebUIClient.connectionStatus) StableDiffusionWebUIClient.modelName ?: "" else ""
            )
        }

        fun updateConnection() {
            coroutineScope.launch {
                if (StableDiffusionWebUIClient.connectionStatus) {
                    modelName = StableDiffusionWebUIClient.modelName ?: ""
                    connectionText = "connection exists"
                    connectionColor = colorConnection

                } else {
                    modelName = ""
                    connectionText = "no connection"
                    connectionColor = colorNoConnection
                }
            }
        }

        CheckboxText(
            "connect to stable diffusion webui api",
            settings.stableDiffusionApiEnabled,
            onChange = {
                settings.stableDiffusionApiEnabled = it

                if (!settings.stableDiffusionApiEnabled) {
                    StableDiffusionWebUIClient.disconnect()
                    updateConnection()
                } else {
                    coroutineScope.launch {
                        StableDiffusionWebUIClient.checkModelName()
                    }
                }
            }
        )

        var checkConnectionExist by remember { mutableStateOf(false) }
        if (!checkConnectionExist) {
            checkConnectionExist = true
            StableDiffusionWebUIClient.onConnectionChange("settingsWindow") {
                updateConnection()
            }
        }

        Text(
            "Stable Diffusion webui link:",
            modifier = Modifier.fillMaxWidth(),
            color = colorText,
            fontSize = normalText,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            OutlinedTextField(
                settings.stableDiffusionLink,
                onValueChange = { settings.stableDiffusionLink = it },
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                modifier = Modifier.weight(1F),
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = colorText,
                    focusedIndicatorColor = colorBackgroundSecond,
                ),
                placeholder = { Text("Type KoboldAI link...", color = colorTextSecond) },
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        modelName = ""
                        connectionText = "checking connection..."
                        connectionColor = colorCheckConnection

                        if (settings.stableDiffusionApiEnabled) StableDiffusionWebUIClient.checkModelName()
                        updateConnection()
                    }
                },
                enabled = settings.stableDiffusionApiEnabled,
                colors = ButtonDefaults.buttonColors(
                    colorBackgroundSecondLighter,
                    disabledBackgroundColor = colorBackground
                ),
            ) {
                Text("connect", fontSize = smallText, color = colorText)
            }
        }
        Text(
            connectionText,
            modifier = Modifier.fillMaxWidth(),
            color = connectionColor,
            fontSize = smallText,
        )
        Text(
            modelName,
            modifier = Modifier.fillMaxWidth(),
            color = connectionColor,
            fontSize = smallText,
        )
    }
}

@Composable
private fun SDGenerating() {
    Column(
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "CFG Scale",
                value = settings.imageGenerating.cfgScale.toFloat(),
                onValueChange = { settings.imageGenerating.cfgScale = it.toInt() },
                onValueChangeFinished = { settings.imageGenerating.cfgScale = it.toInt() },
                valueRange = 1F..30F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )

            DescriptionSlider(
                name = "Steps",
                value = settings.imageGenerating.steps.toFloat(),
                onValueChange = { settings.imageGenerating.steps = it.toInt() },
                onValueChangeFinished = { settings.imageGenerating.steps = it.toInt() },
                valueRange = 1F..150F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            DescriptionSlider(
                name = "Image Width",
                value = settings.imageGenerating.width.toFloat(),
                onValueChange = { settings.imageGenerating.width = it.toInt() },
                onValueChangeFinished = { settings.imageGenerating.width = it.toInt() },
                valueRange = 64F..2048F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )

            DescriptionSlider(
                name = "Image Height",
                value = settings.imageGenerating.height.toFloat(),
                onValueChange = { settings.imageGenerating.height = it.toInt() },
                onValueChangeFinished = { settings.imageGenerating.height = it.toInt() },
                valueRange = 64F..2048F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )
        }

        TextFieldWithTokens(
            text = settings.imageGenerating.style,
            name = "Image Style",
            singleLine = true,
            onValueChange = { settings.imageGenerating.style = it },
            modifier = Modifier.fillMaxWidth()
        )

        TextFieldWithTokens(
            text = settings.imageGenerating.negativePrompt,
            name = "Negative Prompt",
            singleLine = true,
            onValueChange = { settings.imageGenerating.negativePrompt = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

enum class ImageShape {
    Square, Tall, None;

    companion object {
        fun makeImageShape() =
            if (style.image_width == 160 && style.image_height == 160)
                Square
            else if (style.image_width == 120 && style.image_height == 160)
                Tall
            else
                None
    }

    fun changeStyle() {
        when (this) {
            Square -> {
                style.image_width = 160
                style.image_height = 160
            }

            Tall -> {
                style.image_width = 120
                style.image_height = 160
            }

            None -> {}
        }
    }
}

@Composable
private fun AppearanceSettings(
    window: ComposeWindow,
    onBackgroundChange: (ImageBitmap) -> Unit = { },
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(padding),
            ) {
                CheckboxText(
                    "Token streaming",
                    settings.tokenStreaming,
                    onChange = { settings.tokenStreaming = it }
                )

                DescriptionSlider(
                    name = "Duration",
                    value = settings.tokenStreamingDuration.toFloat(),
                    onValueChange = { settings.tokenStreamingDuration = it.toInt() },
                    onValueChangeFinished = { settings.tokenStreamingDuration = it.toInt() },
                    valueRange = settings.tokenStreamingDurationValueRange,
                    intStep = 100,
                    modifier = Modifier.fillMaxWidth(0.4F),
                )
            }

            CheckboxText(
                "Profile images",
                settings.profileImagesEnabled,
                onChange = { settings.profileImagesEnabled = it }
            )

            CheckboxText(
                "Normalize results",
                settings.normalizeResults,
                onChange = { settings.normalizeResults = it }
            )

            var shapeSelected by remember { mutableStateOf(ImageShape.makeImageShape()) }

            ProfileImageShape(
                shapeSelected,
                onSelected = { shape ->
                    shapeSelected = shape
                    shape.changeStyle()
                },
                modifier = Modifier.padding(biggerPadding)
            )
        }

        item { UsualDivider() }

        item {
            val backgroundsFolder by remember { mutableStateOf(File("data/backgrounds")) }
            val list = remember { backgroundsFolder.loadAllImages().toState() }
            Text("Backgrounds:", color = colorText, fontSize = bigText)
            BackgroundImages(
                list,
                settings.background,
                onSelected = { name ->
                    settings.background = name
                    getImageBitmap("${backgroundsFolder.path}/$name")?.let { img ->
                        onBackgroundChange.invoke(img)
                    }
                },
                onLoad = {
                    val file = openFileDialog(window, "", listOf(""), false).firstOrNull()
                    file?.let {
                        try {
                            copyAndGetImage(it, backgroundsFolder)?.let { img ->
                                list[img.first] = img.second
                                onBackgroundChange.invoke(img.second)
                                settings.background = img.first
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun ProfileImageShape(
    selected: ImageShape,
    onSelected: (ImageShape) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(biggerPadding)
    ) {
        Text("Profile image shape:", color = colorText, fontSize = bigText)
        Row(
            horizontalArrangement = Arrangement.spacedBy(biggerPadding),
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(colorBackground.copy(alpha = transparencyLight), RoundedCornerShape(corners))
                    .dashedBorder(border, colorBackgroundSecond, corners)
                    .clickable { onSelected(ImageShape.Square) }
            ) {
                if (selected == ImageShape.Square) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "square shape is selected",
                        tint = colorText,
                        modifier = Modifier
                            .size(bigIconSize)
                            .align(Alignment.Center)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(120.dp, 160.dp)
                    .background(colorBackground.copy(alpha = transparencyLight), RoundedCornerShape(corners))
                    .dashedBorder(border, colorBackgroundSecond, corners)
                    .clickable { onSelected(ImageShape.Tall) }
            ) {
                if (selected == ImageShape.Tall) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "tall shape is selected",
                        tint = colorText,
                        modifier = Modifier
                            .size(bigIconSize)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundImages(
    list: Map<String, ImageBitmap>,
    selected: String,
    onSelected: (String) -> Unit = {},
    onLoad: () -> Unit = {},
) {
    LazyVerticalGrid(
        GridCells.Fixed(3),
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.78F * 3 / (list.size / 3 + 1) - 0.1F),
    ) {
        item { BackgroundImageLoadCard(onLoad = onLoad) }
        items(list.toList()) { pi ->
            BackgroundImageCard(
                pi.second,
                selected = selected == pi.first,
                onSelected = { onSelected.invoke(pi.first) },
            )
        }
    }
}

@Composable
private fun BackgroundImageCard(
    image: ImageBitmap,
    selected: Boolean = false,
    onSelected: () -> Unit = {},
) {
    Column {
        Image(
            image,
            contentDescription = "background image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(1.78F)
                .padding(padding)
                .background(colorBackground, RoundedCornerShape(corners))
                .border(
                    if (selected) border else smallBorder,
                    if (selected) colorBackgroundSecond else colorBorder,
                    RoundedCornerShape(corners)
                )
                .clip(RoundedCornerShape(corners))
                .clickable(onClick = onSelected),
        )
    }
}

@Composable
private fun BackgroundImageLoadCard(
    onLoad: () -> Unit = {},
) {
    Column {
        Icon(
            Icons.Default.Add,
            contentDescription = "background image",
            modifier = Modifier
                .aspectRatio(1.78F)
                .padding(padding)
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .clip(RoundedCornerShape(corners))
                .clickable(onClick = onLoad),
            tint = colorText,
        )
    }
}

@Composable
private fun UserSettings(
    window: ComposeWindow,
    modifier: Modifier = Modifier,
) {
    var image by remember { mutableStateOf(Properties.getProfileImage() ?: emptyImageBitmap) }
    val profileImagesFolder by remember { mutableStateOf(File("data/user/profile images")) }
    val list = remember { profileImagesFolder.loadAllImages().toState() }
    var selected by remember { mutableStateOf(user.profileImageFile) }

    LazyColumn(
        modifier = modifier
    ) {
        item { PresetControls(
            name = "User presets:",
            presets = Properties.getUserPreset(),
            valueToSave = user,
        ) }

        item { UsualDivider() }

        item {
            User(
                image = image,
                onLoadProfileImage = {
                    val file = openFileDialog(window, "", listOf(""), false).firstOrNull()
                    file?.let {
                        copyAndGetImage(it, profileImagesFolder)?.let { img ->
                            list[img.first] = img.second
                            selected = img.first
                            user.profileImageFile = img.first
                        }
                    }
                }
            )
        }

        item { UsualDivider() }

        item {
            Text("All profile images:", color = colorText, fontSize = bigText)
            ProfileImages(
                list,
                selected,
                onSelected = { name ->
                    selected = name
                    user.profileImageFile = name
                    Properties.getProfileImage()?.let { img ->
                        image = img
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun User(
    image: ImageBitmap,
    onLoadProfileImage: () -> Unit,
) {
    Row {
        var toolTip by remember { mutableStateOf(false) }

        Crossfade(
            targetState = image,
            animationSpec = tween(longAnimationDuration),
        ) { newImage ->
            Image(
                newImage,
                contentDescription = "your profile image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(imageWidth, imageHeight)
                    .padding(padding)
                    .background(colorBackground, RoundedCornerShape(corners))
                    .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                    .clip(RoundedCornerShape(corners))
                    .clickable(onClick = onLoadProfileImage)
                    .onPointerEvent(PointerEventType.Enter) { toolTip = true }
                    .onPointerEvent(PointerEventType.Exit) { toolTip = false },
            )
        }

        ATooltip(
            toolTip,
            "click to load a profile image",
            onDismissRequest = { toolTip = false },
            offset = DpOffset(imageWidth, -(imageWidth / 2)),
        )

        Column {
            Text("Your name:", color = colorText, fontSize = normalText)
            OutlinedTextField(
                user.name,
                onValueChange = {
                    user.name = it
                },
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = colorText,
                    focusedIndicatorColor = colorBackgroundSecond,
                ),
                placeholder = { Text("Type yor name...", color = colorTextSecond) },
                singleLine = true,
                maxLines = 1,
            )

            TextFieldWithTokens(
                name = "Your description:",
                text = user.description,
                onValueChange = {
                    user.description = it
                },
                modifier = Modifier.height(80.dp),
            )
        }
    }
}

@Composable
private fun ProfileImages(
    list: Map<String, ImageBitmap>,
    selected: String,
    onSelected: (String) -> Unit = {},
) {
    LazyVerticalGrid(
        GridCells.Adaptive(imageWidth),
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(imageHeight / imageWidth),
    ) {
        items(list.toList()) { pi ->
            ProfileImageCard(
                pi.second,
                selected = selected == pi.first,
                onSelected = { onSelected.invoke(pi.first) },
            )
        }
    }
}

@Composable
private fun ProfileImageCard(
    image: ImageBitmap,
    selected: Boolean = false,
    onSelected: () -> Unit = {},
) {
    Image(
        image,
        contentDescription = "profile image",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(imageWidth, imageHeight)
            .padding(padding)
            .background(colorBackground, RoundedCornerShape(corners))
            .border(
                if (selected) border else smallBorder,
                if (selected) colorBackgroundSecond else colorBorder,
                RoundedCornerShape(corners)
            )
            .clip(RoundedCornerShape(corners))
            .clickable(onClick = onSelected),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CharacterScreen(
    character: ACharacter?,
    window: ComposeWindow,
    onForceSaving: () -> Unit = {},
    onCharacterRedacted: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    character?.let { char ->
        var greeting by remember { mutableStateOf(char.jsonData.firstMessage) }
        var description by remember { mutableStateOf(char.jsonData.description) }
        var scenario by remember { mutableStateOf(char.jsonData.scenario) }
        var chatExamples by remember { mutableStateOf(char.jsonData.messageExample) }
        var personality by remember { mutableStateOf(char.jsonData.personality) }

        val scrollState = rememberScrollState()

        Row(
            modifier = modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = modifier
                    .weight(1F)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(padding),
            ) {
                var tokens by remember { mutableStateOf(0) }

                LaunchedEffect(description, scenario, chatExamples, personality) {
                    withContext(Dispatchers.IO) {
                        tokens = KoboldAIClient.countTokens(description) +
                                KoboldAIClient.countTokens(scenario) +
                                KoboldAIClient.countTokens(chatExamples) +
                                KoboldAIClient.countTokens(personality)
                    }
                }

                Row {
                    var toolTip by remember { mutableStateOf(false) }
                    var isDroppable by remember { mutableStateOf(false) }

                    Crossfade(targetState = char.image, animationSpec = tween(500)) { newImage ->
                        Image(
                            newImage ?: emptyImageBitmap,
                            contentDescription = "profile image of ${char.jsonData.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(imageWidth, imageHeight)
                                .padding(padding)
                                .background(colorBackground, RoundedCornerShape(corners))
                                .alpha(if (isDroppable) transparencyLight else 1F)
                                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                                .clip(RoundedCornerShape(corners))
                                .clickable {
                                    val file = openFileDialog(window, "", listOf(""), false).firstOrNull()
                                    file?.let { f ->
                                        val image = getImageBitmap(f)
                                        image?.let { img ->
                                            //val newImg = img.cropImageWithRatio(2.0, 3.0)
                                            char.image = img
                                            char.saveWithImage()
                                            onForceSaving.invoke()
                                        }
                                    }
                                }
                                .onPointerEvent(PointerEventType.Enter) {
                                    toolTip = true
                                }
                                .onPointerEvent(PointerEventType.Exit) {
                                    toolTip = false
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
                                            val image = getImageBitmap(URI(paths.first()).path)
                                            image?.let { img ->
                                                //val newImg = img.cropImageWithRatio(2.0, 3.0)
                                                char.image = img
                                                char.saveWithImage(img)
                                                onForceSaving.invoke()
                                            }
                                        }
                                    },
                                ),
                        )
                    }

                    ATooltip(
                        toolTip,
                        "click to load a profile image",
                        onDismissRequest = { toolTip = false },
                        offset = DpOffset(imageWidth, -(imageWidth / 2)),
                    )

                    SelectionContainer {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(padding)
                        ) {
                            Text(char.jsonData.name, fontSize = bigText, color = colorText)
                            Text("$tokens tokens in total", fontSize = smallText, color = colorTextSecond)
                        }
                    }
                }

                TextFieldWithTokens(
                    greeting,
                    "Greeting",
                    onValueChange = {
                        greeting = it
                        char.jsonData.firstMessage = it
                        onCharacterRedacted.invoke()
                    },
                    showTokens = false,
                    modifier = Modifier
                        .height(96.dp),
                )

                TextFieldWithTokens(
                    description,
                    "Description",
                    onValueChange = {
                        description = it
                        char.jsonData.description = it
                        onCharacterRedacted.invoke()
                    },
                    modifier = Modifier
                        .height(218.dp),
                )

                TextFieldWithTokens(
                    personality,
                    "Personality",
                    onValueChange = {
                        personality = it
                        char.jsonData.personality = it
                        onCharacterRedacted.invoke()
                    },
                    singleLine = true,
                )

                TextFieldWithTokens(
                    scenario,
                    "Scenario",
                    onValueChange = {
                        scenario = it
                        char.jsonData.scenario = it
                        onCharacterRedacted.invoke()
                    },
                    singleLine = true,
                )

                TextFieldWithTokens(
                    chatExamples,
                    "Chat Examples",
                    onValueChange = {
                        chatExamples = it
                        char.jsonData.messageExample = it
                        onCharacterRedacted.invoke()
                    },
                    modifier = Modifier
                        .height(300.dp),
                )
            }


        }

    } ?: run {
        Text("No character selected", color = colorTextError, fontSize = normalText)
    }
}

@Composable
private fun TextFieldWithTokens(
    text: String,
    name: String = "",
    onValueChange: (String) -> Unit = {},
    singleLine: Boolean = false,
    showTokens: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        SelectionContainer {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(padding)
            ) {
                Text(name, fontSize = normalText, color = colorText)
                Text(
                    "${text.length} Chars",
                    fontSize = smallText,
                    color = colorTextSecond
                )
                if (showTokens) {
                    var tokens by remember { mutableStateOf(-1) }

                    LaunchedEffect(text) {
                        withContext(Dispatchers.IO) {
                            tokens = KoboldAIClient.countTokens(text)
                        }
                    }

                    Text(" ${if (tokens == -1) "<couldn't count>" else tokens.toString()} Tokens", fontSize = smallText, color = colorTextSecond)
                }
            }
        }

        BasicTextField(
            modifier = Modifier
                .fillMaxWidth(0.9F)
                .fillMaxHeight()
                .background(colorBackground, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .padding(padding),
            value = text,
            textStyle = TextStyle(
                color = colorText,
                fontSize = smallText,
            ),
            cursorBrush = SolidColor(colorText),
            onValueChange = {
                onValueChange.invoke(it)
            },
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        )
    }
}