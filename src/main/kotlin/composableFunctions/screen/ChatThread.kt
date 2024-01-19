package composableFunctions.screen

import MDDocument
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bigText
import character.ACharacter
import chat.newChat.AChat2
import chat.newChat.AMessage2
import client.*
import client.kobold.KoboldAIClient
import client.stablediffusion.StableDiffusionWebUIClient
import colorBackground
import colorBackgroundDelete
import colorBackgroundLighter
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorBorder
import colorCheckConnection
import colorNoConnection
import colorText
import colorTextError
import colorTextSecond
import colorTextSuccess
import composableFunctions.*
import corners
import emptyImageBitmap
import getImageBitmap
import iconSize
import imageHeight
import imageWidth
import kotlinx.coroutines.launch
import longAnimationDuration
import menuWidth
import messageImageHeight
import normalAnimationDuration
import normalText
import org.commonmark.node.Document
import properties.Properties
import org.commonmark.parser.Parser
import padding
import prompt.CouldNotGenerateException
import prompt.Generator
import scrollbarThickness
import settings
import shortAnimationDuration
import showsImageString
import smallBorder
import smallCorners
import smallIconSize
import smallText
import tinyIconSize
import transparency
import transparencyLight
import transparencySecond

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatThread(
    window: ComposeWindow,
    character: ACharacter,
    chat: AChat2,
    snackbarHostState: SnackbarHostState,
    onChatManage: () -> Unit = {},
    onNewChat: () -> Unit = {},
    onGeneratingStatusChange: (Boolean) -> Unit = {},
    onSplit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState(0)
    var isFirstTime by remember { mutableStateOf(true) }

    val generator by remember { mutableStateOf(Generator(chat, character, snackbarHostState)) }

    DisposableEffect(generator.isGenerating) {
        onGeneratingStatusChange(generator.isGenerating)
        onDispose {}
    }

    var isDeleting by remember { mutableStateOf(false) }
    var deletingIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.weight(1F),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1F),
                state = scrollState
            ) {
                itemsIndexed(chat.messages) { index, message ->
                    val messageModifier =
                        if (isDeleting && index >= deletingIndex && index != 0) {
                            Modifier
                                .background(colorBackgroundDelete)
                                .onPointerEvent(PointerEventType.Enter) {
                                    deletingIndex = index
                                }
                                .onPointerEvent(PointerEventType.Release) {  // deleting clicks managing
                                    when (it.button) {
                                        PointerButton.Secondary -> {
                                            isDeleting = false
                                        }

                                        PointerButton.Primary -> {
                                            chat.removeAfterInclusively(deletingIndex)
                                            chat.save()

                                            isDeleting = false
                                        }
                                    }
                                }
                        } else {
                            Modifier
                                .onPointerEvent(PointerEventType.Enter) {
                                    if (isDeleting && index != 0) {
                                        deletingIndex = index
                                    }
                                }
                        }

                    MessageView(
                        character = character,
                        message = message,
                        isEditAvailable = if (index == chat.messages.lastIndex) message.swipeId.value != generator.generatingSwipeIndex && !generator.isGeneratingUserMessage else true,
                        isRegenerateAvailable = index != 0 && !message.isUser && !generator.isGenerating && index == chat.messages.lastIndex,
                        isCompleteAvailable = index != 0 && !message.isUser && !generator.isGenerating && index == chat.messages.lastIndex,
                        isGenerativeSwipeAvailable = index != 0 && !message.isUser && !generator.isGenerating && !message.isUser && index == chat.messages.lastIndex,
                        isSwipesAvailable = if (index == 0) message.swipes.size > 1 else message.swipes.size > 1 || (index == chat.messages.lastIndex && !message.isUser),
                        isSplitAvailable = index != 0 && if (index == chat.messages.lastIndex) message.swipeId.value != generator.generatingSwipeIndex && !generator.isGeneratingUserMessage else true,
                        isAdditionalSwipeAvailable = true,
                        onRegenerate = { coroutineScope.launch { generator.regenerateMessage(
                            StableDiffusionWebUIClient.connectionStatus
                        ) } },
                        onComplete = { coroutineScope.launch { generator.completeMessage(
                            StableDiffusionWebUIClient.connectionStatus
                        ) } },
                        onGenerativeSwipe = { coroutineScope.launch { generator.generateNextSwipe(
                            StableDiffusionWebUIClient.connectionStatus
                        ) } },
                        onSwipe = { chat.save() },
                        onEditEnd = { chat.save() },
                        onSplit = { onSplit(index) },
                        modifier = messageModifier.fillMaxWidth(),
                    )
                }
            }

            val scrollbarStateAdapter = rememberScrollbarAdapter(scrollState)
            AppearDisappearAnimation(
                scrollState.canScrollBackward || scrollState.canScrollForward,
                normalAnimationDuration,
            ) {
                VerticalScrollbar(
                    scrollbarStateAdapter,
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

        if (isFirstTime) {
            isFirstTime = false
            coroutineScope.launch {
                scrollState.scrollToItem(chat.messages.lastIndex)
            }
        }

        val message = remember { mutableStateOf(TextFieldValue(text = "")) }
        var image by remember { mutableStateOf<ImageBitmap?>(null) }
        ChatControls(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(SolidColor(colorBackgroundSecond), alpha = transparency)
                .border(smallBorder, colorBorder),
            message = message,
            image = image,
            isDeleting = isDeleting,
            isGenerating = generator.isGenerating,
            onSendMessage = { mes ->
                coroutineScope.launch {
                    message.value = TextFieldValue(text = "")

                    val withUserMessage = (!chat.messages.last().isUser && mes.isNotEmpty()) || image != null
                    generator.generateNewMessage(
                        if (withUserMessage) mes else null,
                        image,
                        StableDiffusionWebUIClient.connectionStatus,
                    )

                    image = null
                }
            },
            onClearChat = {
                chat.clear()
                chat.save()
            },
            onNewChat = { onNewChat() },
            onMenageChats = { onChatManage.invoke() },
            onDeleteMessages = {
                isDeleting = true
                deletingIndex = chat.messages.lastIndex
            },
            onDeleteCancel = { isDeleting = false },
            onGenerateUserMessage = {
                coroutineScope.launch {
                    try {
                        val result = generator.generateUserMessage(message.value.text)
                        message.value = TextFieldValue(text = result)

                    } catch (e: CouldNotGenerateException) {
                        e.printStackTrace()
                    }
                }
            },
            onLoadImage = {
                val file = openFileDialog(window, "", listOf(""), false).firstOrNull()
                try {
                    file?.let { f -> getImageBitmap(f)?.let { image = it } }

                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Couldn't load image")
                    }
                }
            },
            onRemoveImage = { image = null },
            onEmptyLeft = {
                chat.messages.lastOrNull()?.swipeLeft()
                chat.save()
            },
            onEmptyRight = {
                chat.messages.lastOrNull()?.swipeRight()
                chat.save()
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChatControls(
    message: MutableState<TextFieldValue>,
    image: ImageBitmap? = null,
    isDeleting: Boolean,
    isGenerating: Boolean,
    /**
     * @return true if to clear message field and false if not to.
     */
    onSendMessage: (String) -> Unit,
    onLoadImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onClearChat: () -> Unit,
    onNewChat: () -> Unit,
    onMenageChats: () -> Unit,
    onDeleteMessages: () -> Unit,
    onGenerateUserMessage: () -> Unit,
    onDeleteCancel: () -> Unit,
    onEmptyLeft: () -> Unit,
    onEmptyRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(padding),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(padding)
    ) {
        ControlsOptions(
            isGenerating,
            isDeleting,
            onClearChat,
            onNewChat,
            onMenageChats,
            onDeleteMessages,
            onGenerateUserMessage,
            onDeleteCancel,
        )

        BasicTextField(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1F)
                .background(colorBackgroundSecondLighter, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .padding(padding)
                .onKeyEvent { keyEvent ->
                    if (message.value.text.isEmpty()) {
                        if (keyEvent.key == Key.DirectionLeft) {
                            onEmptyLeft()
                        }

                        if (keyEvent.key == Key.DirectionRight) {
                            onEmptyRight()
                        }
                    }

                    if (keyEvent.type == KeyEventType.KeyUp && keyEvent.isShiftPressed && keyEvent.key == Key.Enter) {
                        message.value = message.value.copy(
                            message.value.text.substring(
                                0,
                                message.value.selection.start
                            ) + "\n" + message.value.text.substring(message.value.selection.end),
                            selection = TextRange(message.value.selection.start + 1)
                        )

                    } else if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                        message.value = message.value.copy(
                            message.value.text.substring(0, message.value.selection.start - 1)
                                    + message.value.text.substring(
                                message.value.selection.start,
                                message.value.selection.end
                            )
                                    + message.value.text.substring(message.value.selection.end),
                            selection = TextRange(message.value.selection.start + 1)
                        )

                        if (isGenerating) return@onKeyEvent false

                        onSendMessage.invoke(message.value.text)
                    }
                    true
                },
            value = message.value,
            keyboardOptions = KeyboardOptions.Default.copy(autoCorrect = true),
            textStyle = TextStyle(
                color = colorText,
                fontSize = normalText,
            ),
            cursorBrush = SolidColor(colorText),
            onValueChange = { message.value = it },
        )

        LoadedImage(
            image,
            onRemoveImage,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
        )

        SendIcons(
            isDeleting,
            isGenerating,
            onSendMessage = { onSendMessage.invoke(message.value.text) },
            onLoadImage = onLoadImage,
        )
    }
}

@Composable
private fun ControlsOptions(
    isGenerating: Boolean,
    isDeleting: Boolean,
    onClearChat: () -> Unit,
    onNewChat: () -> Unit,
    onMenageChats: () -> Unit,
    onDeleteMessages: () -> Unit,
    onGenerateUserMessage: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineContext = rememberCoroutineScope()
    var showOptions by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        ADropdownMenu(
            expanded = showOptions,
            onDismissRequest = { showOptions = false },
            offset = DpOffset((-3).dp, 4.dp),
            modifier = Modifier.width(menuWidth)
                .background(SolidColor(colorBackgroundSecondLighter), RectangleShape, transparency)
                .border(smallBorder, colorBackgroundSecondLighter, RectangleShape),
        ) {
            ADropDownMenuItem(
                "clear this chat",
                onClick = {
                    showOptions = false
                    onClearChat.invoke()
                }
            )
            ADropDownMenuItem(
                "new chat",
                onClick = {
                    showOptions = false
                    onNewChat.invoke()
                }
            )
            ADropDownMenuItem(
                "manage chats",
                onClick = {
                    showOptions = false
                    onMenageChats.invoke()
                }
            )
            ADropDownMenuItem(
                "remove messages",
                onClick = {
                    showOptions = false
                    onDeleteMessages.invoke()
                }
            )
            ADropDownMenuItem(
                "generate user message",
                onClick = {
                    showOptions = false
                    onGenerateUserMessage()
                }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(padding),
        ) {
            Icon(
                Icons.Default.Menu,
                "chat options",
                tint = if (isGenerating) colorTextSecond else colorText,
                modifier = Modifier
                    .width(iconSize)
                    .aspectRatio(1F)
                    .clickable(!isGenerating) { showOptions = true }
            )

            if (isDeleting) {
                IconWithToolTip(
                    Icons.Default.Close,
                    "cancel deleting",
                    toolTip = "cancel deleting",
                    tint = colorTextError,
                    modifier = Modifier
                        .width(iconSize)
                        .aspectRatio(1F)
                        .clickable {
                            coroutineContext.launch {
                                onDeleteCancel.invoke()
                            }
                        }
                )
            }

            var connectionStatus by remember { mutableStateOf(KoboldAIClient.connectionStatus) }
            var checkConnectionExist by remember { mutableStateOf(false) }
            if (!checkConnectionExist) {
                checkConnectionExist = true
                KoboldAIClient.onConnectionChange("chatThread") { connectionStatus = it }
            }

            var sdConnectionStatus by remember { mutableStateOf(StableDiffusionWebUIClient.connectionStatus) }
            var sdCheckConnectionExist by remember { mutableStateOf(false) }
            if (!sdCheckConnectionExist) {
                sdCheckConnectionExist = true
                StableDiffusionWebUIClient.onConnectionChange("chatThread") { sdConnectionStatus = it }
            }

            if (!connectionStatus || (!sdConnectionStatus && settings.stableDiffusionApiEnabled)) {
                var checking by remember { mutableStateOf(false) }
                var text = ""

                if (!connectionStatus) {
                    text += "no connection to KoboldAI\n"
                }

                if (!sdConnectionStatus && settings.stableDiffusionApiEnabled) {
                    text += "no connection to Stable Diffusion WebUI\n"
                }

                text += "(click here to check the connection)"

                IconWithToolTip(
                    if (checking) Icons.Default.Refresh else Icons.Default.Warning,
                    text,
                    toolTip = text,
                    tint = if (checking) colorCheckConnection else colorNoConnection,
                    modifier = Modifier
                        .width(iconSize)
                        .aspectRatio(1F)
                        .clickable {
                            coroutineContext.launch {
                                checking = true

                                if (!connectionStatus) {
                                    KoboldAIClient.checkModelName()
                                    connectionStatus = KoboldAIClient.connectionStatus
                                }

                                if (!sdConnectionStatus && settings.stableDiffusionApiEnabled) {
                                    StableDiffusionWebUIClient.checkModelName()
                                    sdConnectionStatus = StableDiffusionWebUIClient.connectionStatus
                                }

                                checking = false
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun LoadedImage(
    image: ImageBitmap?,
    onRemoveImage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppearDisappearAnimation(
        image != null,
        normalAnimationDuration,
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(corners))
                .background(colorBackgroundSecondLighter, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners)),
        ) {
            Image(
                image ?: emptyImageBitmap,
                "image for send",
                modifier = Modifier.fillMaxSize(),
            )

            Icon(
                Icons.Default.Close,
                "send message",
                tint = colorText,
                modifier = Modifier
                    .width(tinyIconSize)
                    .align(Alignment.TopEnd)
                    .background(colorBackgroundLighter.copy(transparencyLight), RoundedCornerShape(smallCorners))
                    .clickable(onClick = onRemoveImage),
            )
        }
    }
}

@Composable
private fun SendIcons(
    isDeleting: Boolean,
    isGenerating: Boolean,
    /**
     * @return true if to clear message field and false if not to.
     */
    onSendMessage: () -> Unit = {},
    onLoadImage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = isGenerating,
        animationSpec = tween(shortAnimationDuration),
    ) { status ->
        if (!status) {
            Column(
                modifier = modifier,
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    "send message",
                    tint = if (!isDeleting) colorText else colorTextSecond,
                    modifier = Modifier
                        .width(70.dp)
                        .weight(1F)
                        .clickable(enabled = !isDeleting, onClick = onSendMessage),
                )

                if (settings.stableDiffusionApiEnabled) {
                    Icon(
                        painterResource("add_photo.svg"),
                        "load image",
                        tint = if (!isDeleting) colorText else colorTextSecond,
                        modifier = Modifier
                            .width(70.dp)
                            .weight(1F)
                            .clickable(enabled = !isDeleting, onClick = onLoadImage),
                    )
                }
            }

        } else {
            LoadingIcon(
                contentDescription = "send message",
                tint = colorTextSecond,
                modifier = modifier
                    .width(70.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun MessageView(
    character: ACharacter,
    message: AMessage2,
    isEditAvailable: Boolean,
    isRegenerateAvailable: Boolean,
    isCompleteAvailable: Boolean,
    isGenerativeSwipeAvailable: Boolean,
    isSwipesAvailable: Boolean,
    isSplitAvailable: Boolean,
    isAdditionalSwipeAvailable: Boolean,
    onGenerativeSwipe: () -> Unit,
    onRegenerate: () -> Unit,
    onComplete: () -> Unit,
    onSwipe: (Int) -> Unit,
    onEditEnd: () -> Unit,
    onSplit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(padding)
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparencySecond)
            .border(smallBorder, colorBorder, RoundedCornerShape(corners)),
    ) {
        var editMessage by remember { mutableStateOf<TextFieldValue?>(null) }
        var previousIndex by remember { mutableStateOf(message.swipeId.value) }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Crossfade(
                    targetState = if (message.isUser) Properties.getProfileImage()
                        ?: emptyImageBitmap else character.image,
                    animationSpec = tween(longAnimationDuration),
                ) { newImage ->
                    AppearDisappearAnimation(
                        settings.profileImagesEnabled,
                    ) {
                        Image(
                            newImage ?: emptyImageBitmap,
                            contentDescription = "profile image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(imageWidth, imageHeight)
                                .padding(padding)
                                .background(colorBackground, RoundedCornerShape(corners))
                                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                                .clip(RoundedCornerShape(corners)),
                        )
                    }
                }

                if (isSwipesAvailable) {
                    SwipeControls(
                        message.swipeId.value,
                        message.swipes.size,
                        isGenerativeSwipeAvailable,
                        onChange = {
                            previousIndex = message.swipeId.value
                            message.swipeId.value = it
                            onSwipe(it)

                            if (it == message.swipes.size) {
                                onGenerativeSwipe()
                            }
                        },
                    )
                }
            }

            Column {
                Text(
                    message.name,
                    fontSize = bigText,
                    color = colorText,
                    modifier = Modifier.padding(top = padding)
                )

                Swipes(
                    message.swipes,
                    message.swipeId.value,
                    previousIndex
                ) { index, text ->
                    MessageContent(
                        text = text.format(character),
                        image = message.getImage(index),
                        isEditAvailable = isEditAvailable,
                        editMessage = editMessage,
                        onChange = { editMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        ActionButtons(
            isEditAvailable = isEditAvailable,
            isRegenerateAvailable = isRegenerateAvailable,
            isCompleteAvailable = isCompleteAvailable,
            isSplitAvailable = isSplitAvailable,
            isAdditionalSwipeAvailable = isAdditionalSwipeAvailable,
            isEdit = editMessage != null,
            onEditStart = { editMessage = TextFieldValue(message.content) },
            onEditEnd = { accept ->
                if (accept) {
                    editMessage?.text?.let {
                        message.swipes[message.swipeId.value] = it
                        onEditEnd()
                    }

                    editMessage = null
                } else {
                    editMessage = null
                }
            },
            onRegenerate = onRegenerate,
            onComplete = onComplete,
            onSplit = onSplit,
            onCreateEmptySwipe = { message.swipes.add("") },
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun MessageContent(
    text: String,
    image: ImageBitmap?,
    isEditAvailable: Boolean,
    editMessage: TextFieldValue?,
    onChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester by remember { mutableStateOf(FocusRequester()) }
    LaunchedEffect(editMessage != null) {
        if (editMessage != null) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
    ) {
        Crossfade(
            editMessage != null && isEditAvailable,
            animationSpec = tween(shortAnimationDuration),
        ) { ie ->
            if (ie) {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorBackground, RoundedCornerShape(corners))
                        .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                        .padding(padding)
                        .focusRequester(focusRequester),
                    value = editMessage ?: TextFieldValue(""),
                    textStyle = TextStyle(
                        color = colorText,
                        fontSize = normalText,
                    ),
                    cursorBrush = SolidColor(colorText),
                    onValueChange = onChange,
                )
            } else {
                SelectionContainer {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val parser = Parser.builder().build()
                        val mainMessage = text.substringBeforeLast(showsImageString)
                        val root = parser.parse(mainMessage) as Document
                        MDDocument(root)

                        val imageMessageIndex = text.indexOf(showsImageString)
                        if (imageMessageIndex != -1) {
                            val imageMessage = text.substring(imageMessageIndex)
                            Text(
                                imageMessage.replace("*", ""),
                                color = colorTextSecond,
                                fontSize = smallText,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    }
                }
            }
        }

        image?.let { img ->
            Image(
                img,
                "an image, attached to the message",
                modifier = Modifier
                    .height(messageImageHeight)
                    .padding(padding)
                    .background(colorBackground, RoundedCornerShape(corners))
                    .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                    .clip(RoundedCornerShape(corners)),
            )
        }
    }
}

@Composable
private fun SwipeControls(
    currentIndex: Int,
    max: Int,
    isGenerativeSwipeAvailable: Boolean,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLeftAvailable = currentIndex > 0
    val isRightAvailable = isGenerativeSwipeAvailable || currentIndex < (max - 1)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLeftAvailable) {
            Icon(
                Icons.Default.ArrowBack,
                "swipe left",
                tint = colorText,
                modifier = Modifier
                    .height(smallIconSize)
                    .aspectRatio(1F)
                    .clickable(onClick = { onChange(currentIndex - 1) })
            )
        } else {
            Icon(
                Icons.Default.ArrowBack,
                "swipe left is blocked",
                tint = colorTextSecond,
                modifier = Modifier
                    .height(smallIconSize)
                    .aspectRatio(1F)
            )
        }

        Text("${currentIndex + 1}/$max", fontSize = smallText, color = colorText)

        if (isRightAvailable) {
            Icon(
                Icons.Default.ArrowForward,
                "swipe right",
                tint = colorText,
                modifier = Modifier
                    .height(smallIconSize)
                    .aspectRatio(1F)
                    .clickable(onClick = { onChange(currentIndex + 1) })
            )
        } else {
            Icon(
                Icons.Default.ArrowForward,
                "swipe right is blocked",
                tint = colorTextSecond,
                modifier = Modifier
                    .height(smallIconSize)
                    .aspectRatio(1F)
            )
        }
    }
}

@Composable
private fun <T> Swipes(
    items: List<T>,
    currentIndex: Int,
    previousIndex: Int,
    itemContent: @Composable (Int, T) -> Unit,
) {
    Box(
        modifier = Modifier.clipToBounds()
    ) {
        items.forEachIndexed { index, item ->
            AnimatedVisibility(
                visible = index == currentIndex,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = normalAnimationDuration,
                        easing = FastOutLinearInEasing,
                    )
                ) + slideInHorizontally(
                    initialOffsetX = { if (previousIndex < currentIndex) it else -it },
                    animationSpec = tween(
                        durationMillis = normalAnimationDuration,
                        easing = FastOutSlowInEasing,
                    ),
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = normalAnimationDuration,
                        easing = FastOutLinearInEasing,
                    )
                ) + slideOutHorizontally(
                    targetOffsetX = { if (previousIndex < currentIndex) -it else it },
                    animationSpec = tween(
                        durationMillis = normalAnimationDuration,
                        easing = FastOutSlowInEasing,
                    )
                )
            ) {
                itemContent.invoke(index, item)
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isEditAvailable: Boolean,
    isRegenerateAvailable: Boolean,
    isCompleteAvailable: Boolean,
    isSplitAvailable: Boolean,
    isAdditionalSwipeAvailable: Boolean,
    isEdit: Boolean,
    onEditStart: () -> Unit,
    onEditEnd: (Boolean) -> Unit,
    onRegenerate: () -> Unit,
    onComplete: () -> Unit,
    onSplit: () -> Unit,
    onCreateEmptySwipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        isEdit,
        animationSpec = tween(shortAnimationDuration),
        modifier = modifier,
    ) { ie ->
        if (ie) {
            Row {
                Icon(
                    Icons.Default.Check,
                    "save changes",
                    tint = colorTextSuccess,
                    modifier = Modifier
                        .padding(padding)
                        .width(tinyIconSize)
                        .aspectRatio(1F)
                        .clickable(onClick = { onEditEnd(true) })
                )

                Icon(
                    Icons.Default.Clear,
                    "cancel changes",
                    tint = colorTextError,
                    modifier = Modifier
                        .padding(padding)
                        .width(tinyIconSize)
                        .aspectRatio(1F)
                        .clickable(onClick = { onEditEnd(false) })
                )
            }

        } else {
            Row {
                AppearDisappearAnimation(
                    isCompleteAvailable,
                    shortAnimationDuration,
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        "complete this message",
                        tint = colorText,
                        modifier = Modifier
                            .padding(padding)
                            .width(tinyIconSize)
                            .aspectRatio(1F)
                            .clickable(onClick = onComplete)
                    )
                }

                AppearDisappearAnimation(
                    isRegenerateAvailable,
                    shortAnimationDuration,
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        "regenerate this message",
                        tint = colorText,
                        modifier = Modifier
                            .padding(padding)
                            .width(tinyIconSize)
                            .aspectRatio(1F)
                            .clickable(onClick = onRegenerate)
                    )
                }

                AppearDisappearAnimation(
                    isSplitAvailable,
                    shortAnimationDuration,
                ) {
                    Icon(
                        painterResource("alt_route.svg"),
                        "split the chat at this message",
                        tint = colorText,
                        modifier = Modifier
                            .padding(padding)
                            .width(tinyIconSize)
                            .aspectRatio(1F)
                            .clickable(onClick = onSplit)
                    )
                }

                AppearDisappearAnimation(
                    isEditAvailable,
                    shortAnimationDuration,
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "edit this message",
                        tint = colorText,
                        modifier = Modifier
                            .padding(padding)
                            .width(tinyIconSize)
                            .aspectRatio(1F)
                            .clickable(onClick = onEditStart)
                    )
                }

                AppearDisappearAnimation(
                    isAdditionalSwipeAvailable,
                    shortAnimationDuration,
                ) {
                    Icon(
                        Icons.Default.Add,
                        "create new empty swipe for this message",
                        tint = colorText,
                        modifier = Modifier
                            .padding(padding)
                            .width(tinyIconSize)
                            .aspectRatio(1F)
                            .clickable(onClick = onCreateEmptySwipe)
                    )
                }
            }
        }
    }
}