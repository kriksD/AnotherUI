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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import gpt2Tokenizer.GlobalTokenizer
import imageHeight
import imageWidth
import kotlinx.coroutines.launch
import loadAllImages
import longAnimationDuration
import normalAnimationDuration
import normalText
import padding
import prompt.PromptType
import properties.Properties
import settings
import smallBorder
import smallText
import style
import toState
import transparency
import transparencyLight
import user
import java.io.File
import java.net.URI

private enum class SettingsScreen {
    AI, StableDiffusion, Appearance, User, Experimental, Character;

    override fun toString(): String = when (this) {
        AI -> "AI"
        StableDiffusion -> "stable diffusion"
        Appearance -> "appearance"
        User -> "user"
        Experimental -> "experimental"
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

                SettingsScreen.Experimental -> ExperimentalSettings(
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
        item { Generating() }
        item { UsualDivider() }
        item { Prompt() }
        item { UsualDivider() }
    }
}

@Composable
private fun ConnectionCheck() {
    val coroutineScope = rememberCoroutineScope()

    Column {
        var linkText by remember { mutableStateOf(settings.link) }
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
                linkText,
                onValueChange = {
                    settings.link = it
                    linkText = settings.link
                },
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
private fun Generating() {
    Column(
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            var temperature by remember { mutableStateOf(settings.generating.temperature) }
            DescriptionSlider(
                name = "Temperature",
                value = temperature,
                onValueChange = {
                    temperature = it
                },
                onValueChangeFinished = {
                    temperature = it
                    settings.generating.temperature = temperature
                },
                valueRange = 0.1F..2F,
                modifier = Modifier.weight(1F),
            )

            var maxLength by remember { mutableStateOf(settings.generating.max_length.toFloat()) }
            DescriptionSlider(
                name = "Amount of generation",
                value = maxLength,
                onValueChange = {
                    maxLength = it
                },
                onValueChangeFinished = {
                    maxLength = it
                    settings.generating.max_length = maxLength.toInt()
                },
                valueRange = 16F..512F,
                intStep = 8,
                modifier = Modifier.weight(1F),
            )

            var maxContextLength by remember { mutableStateOf(settings.generating.max_context_length.toFloat()) }
            DescriptionSlider(
                name = "Max Context Length",
                value = maxContextLength,
                onValueChange = {
                    maxContextLength = it
                },
                onValueChangeFinished = {
                    maxContextLength = it
                    settings.generating.max_context_length = maxContextLength.toInt()
                },
                valueRange = 512F..16384F,
                intStep = 64,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            var repPen by remember { mutableStateOf(settings.generating.rep_pen) }
            DescriptionSlider(
                name = "Repetition Penalty",
                value = repPen,
                onValueChange = {
                    repPen = it
                },
                onValueChangeFinished = {
                    repPen = it
                    settings.generating.rep_pen = repPen
                },
                valueRange = 1F..1.5F,
                modifier = Modifier.weight(1F),
            )

            var repPenRange by remember { mutableStateOf(settings.generating.rep_pen_range.toFloat()) }
            DescriptionSlider(
                name = "Repetition Penalty Range",
                value = repPenRange,
                onValueChange = {
                    repPenRange = it
                },
                onValueChangeFinished = {
                    repPenRange = it
                    settings.generating.rep_pen_range = repPenRange.toInt()
                },
                valueRange = 0F..16384F,
                intStep = 64,
                modifier = Modifier.weight(1F),
            )

            var repPenSlope by remember { mutableStateOf(settings.generating.rep_pen_slope) }
            DescriptionSlider(
                name = "Rep Pen Slope",
                value = repPenSlope,
                onValueChange = {
                    repPenSlope = it
                },
                onValueChangeFinished = {
                    repPenSlope = it
                    settings.generating.rep_pen_slope = repPenSlope
                },
                valueRange = 0.1F..10F,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            var minP by remember { mutableStateOf(settings.generating.min_p) }
            DescriptionSlider(
                name = "Min P Sampling",
                value = minP,
                onValueChange = {
                    minP = it
                },
                onValueChangeFinished = {
                    minP = it
                    settings.generating.min_p = minP
                },
                modifier = Modifier.weight(1F),
            )

            var topP by remember { mutableStateOf(settings.generating.top_p) }
            DescriptionSlider(
                name = "Top P Sampling",
                value = topP,
                onValueChange = {
                    topP = it
                },
                onValueChangeFinished = {
                    topP = it
                    settings.generating.top_p = topP
                },
                modifier = Modifier.weight(1F),
            )

            var topK by remember { mutableStateOf(settings.generating.top_k.toFloat()) }
            DescriptionSlider(
                name = "Top K Sampling",
                value = topK,
                onValueChange = {
                    topK = it
                },
                onValueChangeFinished = {
                    topK = it
                    settings.generating.top_k = topK.toInt()
                },
                valueRange = 0F..100F,
                intStep = 1,
                modifier = Modifier.weight(1F)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            var topA by remember { mutableStateOf(settings.generating.top_a) }
            DescriptionSlider(
                name = "Top A Sampling",
                value = topA,
                onValueChange = {
                    topA = it
                },
                onValueChangeFinished = {
                    topA = it
                    settings.generating.top_a = topA
                },
                modifier = Modifier.weight(1F),
            )

            var typical by remember { mutableStateOf(settings.generating.typical) }
            DescriptionSlider(
                name = "Typical Sampling",
                value = typical,
                onValueChange = {
                    typical = it
                },
                onValueChangeFinished = {
                    typical = it
                    settings.generating.typical = typical
                },
                modifier = Modifier.weight(1F),
            )

            var tfs by remember { mutableStateOf(settings.generating.tfs) }
            DescriptionSlider(
                name = "Tail Free Sampling",
                value = tfs,
                onValueChange = {
                    tfs = it
                },
                onValueChangeFinished = {
                    tfs = it
                    settings.generating.tfs = tfs
                },
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            Text(
                text = "Seed:",
                color = colorText,
                fontSize = normalText,
            )

            var seed by remember { mutableStateOf<Int?>(settings.generating.seed) }
            BasicTextField(
                modifier = Modifier
                    .background(colorBackground, RoundedCornerShape(corners))
                    .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                    .padding(padding),
                value = if (seed == null) "" else seed.toString(),
                textStyle = TextStyle(color = colorText, fontSize = normalText),
                cursorBrush = SolidColor(colorText),
                onValueChange = { value ->
                    if (value == "-") {
                        seed = -1
                    }

                    if (value.isBlank()) {
                        seed = null
                        settings.generating.seed = -1
                    }

                    value.toIntOrNull()?.let {
                        seed = it
                        settings.generating.seed = it
                    }
                },
                singleLine = true,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun Prompt() {
    var promptType by remember { mutableStateOf(settings.prompt_settings.type) }
    var pattern by remember { mutableStateOf(settings.prompt_settings.pattern) }
    var systemPrompt by remember { mutableStateOf(settings.prompt_settings.system_prompt) }
    var userInstructPrefix by remember { mutableStateOf(settings.prompt_settings.user_instruct_prefix) }
    var modelInstructPrefix by remember { mutableStateOf(settings.prompt_settings.model_instruct_prefix) }
    var endTokens by remember { mutableStateOf(settings.prompt_settings.stop_sequence) }

    CheckboxText(
        "instruct",
        promptType == PromptType.Instruct,
        onChange = {
            promptType = if (it) PromptType.Instruct else PromptType.Chat
            settings.prompt_settings.type = promptType
        }
    )

    TextFieldWithTokens(
        pattern,
        "Pattern",
        onValueChange = {
            pattern = it
            settings.prompt_settings.pattern = it
        },
        showTokens = false,
        modifier = Modifier
            .height(140.dp),
    )

    TextFieldWithTokens(
        systemPrompt,
        "System Prompt",
        onValueChange = {
            systemPrompt = it
            settings.prompt_settings.system_prompt = it
        },
        singleLine = true,
    )

    TextFieldWithTokens(
        userInstructPrefix,
        "User Instruction Prefix",
        onValueChange = {
            userInstructPrefix = it
            settings.prompt_settings.user_instruct_prefix = it
        },
        showTokens = false,
        singleLine = true,
    )

    TextFieldWithTokens(
        modelInstructPrefix,
        "Model Instruction Prefix",
        onValueChange = {
            modelInstructPrefix = it
            settings.prompt_settings.model_instruct_prefix = it
        },
        showTokens = false,
        singleLine = true,
    )

    TextFieldWithTokens(
        endTokens,
        "End Tokens",
        onValueChange = {
            endTokens = it
            settings.prompt_settings.stop_sequence = it
        },
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
        item { SDGenerating() }
        item { UsualDivider() }
    }
}

@Composable
private fun SDConnectionCheck() {
    val coroutineScope = rememberCoroutineScope()

    Column {
        var connectionEnabled by remember { mutableStateOf(settings.stable_diffusion_api_enabled) }
        var linkText by remember { mutableStateOf(settings.stable_diffusion_link) }
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
            connectionEnabled,
            onChange = {
                connectionEnabled = it
                settings.stable_diffusion_api_enabled = it

                if (!connectionEnabled) {
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
                linkText,
                onValueChange = {
                    settings.stable_diffusion_link = it
                    linkText = settings.stable_diffusion_link
                },
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

                        if (settings.stable_diffusion_api_enabled) StableDiffusionWebUIClient.checkModelName()
                        updateConnection()
                    }
                },
                enabled = settings.stable_diffusion_api_enabled,
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
            var sfgScale by remember { mutableStateOf(settings.image_generating.cfg_scale.toFloat()) }
            DescriptionSlider(
                name = "CFG Scale",
                value = sfgScale,
                onValueChange = {
                    sfgScale = it
                },
                onValueChangeFinished = {
                    sfgScale = it
                    settings.image_generating.cfg_scale = sfgScale.toInt()
                },
                valueRange = 1F..30F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )

            var steps by remember { mutableStateOf(settings.image_generating.steps.toFloat()) }
            DescriptionSlider(
                name = "Steps",
                value = steps,
                onValueChange = {
                    steps = it
                },
                onValueChangeFinished = {
                    steps = it
                    settings.image_generating.steps = steps.toInt()
                },
                valueRange = 1F..150F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            var width by remember { mutableStateOf(settings.image_generating.width.toFloat()) }
            DescriptionSlider(
                name = "Image Width",
                value = width,
                onValueChange = {
                    width = it
                },
                onValueChangeFinished = {
                    width = it
                    settings.image_generating.width = width.toInt()
                },
                valueRange = 64F..2048F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )

            var height by remember { mutableStateOf(settings.image_generating.height.toFloat()) }
            DescriptionSlider(
                name = "Image Height",
                value = height,
                onValueChange = {
                    height = it
                },
                onValueChangeFinished = {
                    height = it
                    settings.image_generating.height = height.toInt()
                },
                valueRange = 64F..2048F,
                intStep = 1,
                modifier = Modifier.weight(1F),
            )
        }

        var style by remember { mutableStateOf(settings.image_generating.style) }
        TextFieldWithTokens(
            text = style,
            name = "Image Style",
            singleLine = true,
            onValueChange = {
                style = it
                settings.image_generating.style = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        var negativePrompt by remember { mutableStateOf(settings.image_generating.negative_prompt) }
        TextFieldWithTokens(
            text = negativePrompt,
            name = "Negative Prompt",
            singleLine = true,
            onValueChange = {
                negativePrompt = it
                settings.image_generating.negative_prompt = it
            },
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
            var profileImageEnabled by remember { mutableStateOf(settings.profile_images_enabled) }

            CheckboxText(
                "profile images enabled",
                profileImageEnabled,
                onChange = {
                    profileImageEnabled = it
                    settings.profile_images_enabled = it
                }
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
            var selected by remember { mutableStateOf(settings.background) }
            Text("Backgrounds:", color = colorText, fontSize = bigText)
            BackgroundImages(
                list,
                selected,
                onSelected = { name ->
                    selected = name
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
                                selected = img.first
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
    var selected by remember { mutableStateOf(user.profile_image_file) }

    LazyColumn(
        modifier = modifier
    ) {
        item {
            User(
                image = image,
                onLoadProfileImage = {
                    val file = openFileDialog(window, "", listOf(""), false).firstOrNull()
                    file?.let {
                        copyAndGetImage(it, profileImagesFolder)?.let { img ->
                            list[img.first] = img.second
                            selected = img.first
                            user.profile_image_file = img.first
                        }
                    }
                }
            )
        }

        item {
            UsualDivider()
        }

        item {
            Text("All profile images:", color = colorText, fontSize = bigText)
            ProfileImages(
                list,
                selected,
                onSelected = { name ->
                    selected = name
                    user.profile_image_file = name
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
            var yourName by remember { mutableStateOf(user.name) }

            Text("Your name:", color = colorText, fontSize = normalText)
            OutlinedTextField(
                yourName,
                onValueChange = {
                    yourName = it
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

@Composable
private fun ExperimentalSettings(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        item { MultiGen() }
        item { UsualDivider() }
        item { ChangeableContext() }
        item { UsualDivider() }
    }
}

@Composable
private fun MultiGen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Text(
            "Multigen (experimental):",
            modifier = Modifier.fillMaxWidth(),
            color = colorText,
            fontSize = bigText,
        )

        var checked by remember { mutableStateOf(settings.multi_gen.enabled) }
        CheckboxText(
            "enable",
            checked,
            onChange = {
                checked = it
                settings.multi_gen.enabled = it
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            var tokensPerStep by remember { mutableStateOf(settings.multi_gen.tokens_per_step.toFloat()) }
            DescriptionSlider(
                tokensPerStep,
                name = "Tokens Per Step",
                onValueChange = {
                    tokensPerStep = it
                },
                onValueChangeFinished = {
                    tokensPerStep = it
                    settings.multi_gen.tokens_per_step = tokensPerStep.toInt()
                },
                valueRange = 8F..64F,
                intStep = 8,
                enabled = checked,
                modifier = Modifier.weight(1F),
            )

            var tries by remember { mutableStateOf(settings.multi_gen.tries.toFloat()) }
            DescriptionSlider(
                tries,
                name = "Tries",
                onValueChange = {
                    tries = it
                },
                onValueChangeFinished = {
                    tries = it
                    settings.multi_gen.tries = tries.toInt()
                },
                valueRange = 1F..5F,
                intStep = 1,
                enabled = checked,
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@Composable
private fun ChangeableContext() {
    Column(
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Text(
            "Changeable Context (experimental):",
            modifier = Modifier.fillMaxWidth(),
            color = colorText,
            fontSize = bigText,
        )

        var checked by remember { mutableStateOf(settings.changeable_context.enabled) }
        CheckboxText(
            "enable",
            checked,
            onChange = {
                checked = it
                settings.changeable_context.enabled = it
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            var tokensPerStep by remember { mutableStateOf(settings.changeable_context.buffer.toFloat()) }
            DescriptionSlider(
                tokensPerStep,
                name = "Buffer",
                onValueChange = {
                    tokensPerStep = it
                },
                onValueChangeFinished = {
                    tokensPerStep = it
                    settings.changeable_context.buffer = tokensPerStep.toInt()
                },
                valueRange = 256F..1024F,
                intStep = 8,
                enabled = checked,
                modifier = Modifier.weight(1F),
            )
        }
    }
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
        var greeting by remember { mutableStateOf(char.jsonData.first_mes) }
        var description by remember { mutableStateOf(char.jsonData.description) }
        var scenario by remember { mutableStateOf(char.jsonData.scenario) }
        var chatExamples by remember { mutableStateOf(char.jsonData.mes_example) }
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
                fun countTokens(): Int {
                    return GlobalTokenizer.countTokens(description) +
                            GlobalTokenizer.countTokens(scenario) +
                            GlobalTokenizer.countTokens(chatExamples) +
                            GlobalTokenizer.countTokens(personality)
                }

                var tokens by remember { mutableStateOf(countTokens()) }

                Row {
                    var toolTip by remember { mutableStateOf(false) }
                    var isDroppable by remember { mutableStateOf(false) }

                    Crossfade(targetState = char.image, animationSpec = tween(500)) { newImage ->
                        Image(
                            newImage,
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
                                                char.saveWithImage()
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
                        char.jsonData.first_mes = it
                        tokens = countTokens()
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
                        tokens = countTokens()
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
                        tokens = countTokens()
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
                        tokens = countTokens()
                        onCharacterRedacted.invoke()
                    },
                    singleLine = true,
                )

                TextFieldWithTokens(
                    chatExamples,
                    "Chat Examples",
                    onValueChange = {
                        chatExamples = it
                        char.jsonData.mes_example = it
                        tokens = countTokens()
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
                    Text(" ${GlobalTokenizer.countTokens(text)} Tokens", fontSize = smallText, color = colorTextSecond)
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