import SwipeState.Companion.generatingText
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import character.ACharacter
import character.chat.Chat
import character.chat.Message
import character.createNewChat
import client.*
import composableFunctions.*
import gpt2Tokenizer.GlobalTokenizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.commonmark.node.Document
import properties.Properties
import org.commonmark.parser.Parser

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatThread(
    window: ComposeWindow,
    character: ACharacter,
    chat: Chat,
    snackbarHostState: SnackbarHostState,
    onChatManage: () -> Unit = {},
    onNewChat: (Chat) -> Unit = {},
    onGeneratingStatusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
    ) {
        var isFirstTime by remember { mutableStateOf(true) }
        var generatingStatus by remember { mutableStateOf(GeneratingStatus(onChange = onGeneratingStatusChange)) }
        var deletingStatus by remember { mutableStateOf(DeletingStatus()) }

        val messages = remember { mutableStateListOf<Message>() }
        if (messages.isEmpty()) {
            messages.addAll(chat.messages)
        }
        val messagesState = rememberLazyListState(0)
        var messageWidth by remember { mutableStateOf(0.dp) }

        val swipes = remember { mutableStateListOf<String>() }
        if (swipes.isEmpty()) {
            chat.messages.last().swipes?.let {
                swipes.addAll(it)
            } ?: kotlin.run {
                swipes.add(chat.messages.last().mes)
            }
        }
        var previousSwipeIndex by remember { mutableStateOf(SwipeState()) }
        var swipeIndexState by remember {
            mutableStateOf(SwipeState(
                chat.messages.last().swipe_id ?: 0,
                amount = swipes.size,
                onChange = {
                    previousSwipeIndex = it
                }
            ))
        }
        var swipeOccurred by remember { mutableStateOf(false) }

        val promptManager by remember { mutableStateOf(PromptManager(character, chat)) }

        suspend fun generateCurrentSwipe(): String? {
            generatingStatus = generatingStatus.new(true)

            val newChat = createNewChat(character)
            newChat.messages.addAll(messages.subList(0, messages.lastIndex))
            promptManager.update()
            //val prompt = Prompt.createPrompt(character, newChat)
            val result = KoboldAIClient.generate(promptManager.prompt, character)

            generatingStatus = generatingStatus.new(false)

            if (result == null) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    if (!KoboldAIClient.connectionStatus) {
                        snackbarHostState.showSnackbar("no connection")
                    } else {
                        snackbarHostState.showSnackbar("couldn't generate")
                    }
                }
            }

            return result
        }

        suspend fun generateImageForCurrentSwipe(): ImageBitmap? {
            generatingStatus = generatingStatus.new(true)

            val imageResult = if (StableDiffusionWebUIClient.connectionStatus) {
                StableDiffusionWebUIClient.generate(
                    ImagePrompt.createPrompt(messages[messages.lastIndex - 1].mes, swipes[swipeIndexState.index])
                )
            } else null

            generatingStatus = generatingStatus.new(false)

            if (imageResult == null) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    if (!StableDiffusionWebUIClient.connectionStatus) {
                        snackbarHostState.showSnackbar("no connection to stable diffusion webui")
                    } else {
                        snackbarHostState.showSnackbar("couldn't generate image")
                    }
                }
            }

            return imageResult
        }

        LaunchedEffect(swipeOccurred) {
            if (!swipeOccurred) {
                delay(1000)
                chat.save()
            }

            swipeOccurred = false
        }

        Row(
            modifier = Modifier.weight(1F),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1F),
                state = messagesState
            ) {
                itemsIndexed(messages) { i, mes ->
                    val messageModifier =
                        if (deletingStatus.status && i >= deletingStatus.index && deletingStatus.index > 0 && i != 0) {
                            Modifier
                                .background(colorBackgroundDelete)
                                .onPointerEvent(PointerEventType.Enter) {
                                    deletingStatus = deletingStatus.new(i)
                                }
                                .onPointerEvent(PointerEventType.Release) {  // deleting clicks managing
                                    when (it.button) {
                                        PointerButton.Secondary -> {
                                            deletingStatus = deletingStatus.disabled()
                                        }
                                        PointerButton.Primary -> {
                                            chat.removeAfterInclusively(deletingStatus.index)
                                            chat.save()
                                            messages.clear()
                                            messages.addAll(chat.messages)

                                            messages.last().swipes?.let { mesList ->
                                                swipes.clear()
                                                swipes.addAll(mesList)

                                            } ?: kotlin.run {
                                                swipes.clear()
                                            }

                                            swipeIndexState = swipeIndexState.reset()
                                            deletingStatus = deletingStatus.disabled()
                                        }
                                    }
                                }
                        } else {
                            Modifier
                                .onPointerEvent(PointerEventType.Enter) {
                                    if (deletingStatus.status && i != 0) {
                                        deletingStatus = deletingStatus.new(i)
                                    }
                                }
                        }

                    if (i < messages.lastIndex || messages.last().is_user || messages.size == 1) {
                        MessageView(
                            character = character,
                            message = mes,
                            image = chat.image(i),
                            modifier = messageModifier
                                .fillMaxWidth()
                                .onGloballyPositioned { size ->
                                    messageWidth = size.size.width.dp
                                },
                            onEditEnd = { edited ->
                                edited?.let {
                                    mes.mes = it
                                    chat.messages[i].mes = it
                                    chat.save()
                                }
                            },
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.End,
                        ) {
                            SwipeControls(
                                state = swipeIndexState,
                                onSwipeLeft = {
                                    coroutineScope.launch {
                                        swipeIndexState = swipeIndexState.left()
                                        chat.swipeLeft()
                                        swipeOccurred = true
                                    }
                                },
                                onSwipeRight = {
                                    coroutineScope.launch {
                                        swipeIndexState = swipeIndexState.right()

                                        if (!generatingStatus.status && swipeIndexState.generatingIndex != null) {
                                            swipes.add(generatingText)

                                            val result = generateCurrentSwipe()

                                            if (result == null) {
                                                swipes.removeLast()
                                                swipeIndexState = swipeIndexState.failedNewSwipeGenerating()
                                            } else {
                                                swipeIndexState.generatingIndex?.let {
                                                    swipes[it] = result
                                                    chat.updateSwipe(it, result)
                                                }

                                                if (settings.stable_diffusion_api_enabled) {
                                                    val imageResult = generateImageForCurrentSwipe()
                                                    imageResult?.let { img ->
                                                        chat.updateImage(
                                                            img,
                                                            chat.messages.lastIndex,
                                                            swipeIndexState.generatingIndex ?: 0
                                                        )
                                                    }
                                                }
                                                chat.save()

                                                swipeIndexState = swipeIndexState.stoppedGenerating()
                                            }
                                        }

                                        chat.swipeRight()
                                        swipeOccurred = true
                                    }
                                },
                            )

                            Swipes(
                                swipes,
                                swipeIndexState.index,
                                previousSwipeIndex.index,
                            ) { i, swipeMessage ->
                                MessageView(
                                    character,
                                    chat.createNewMessage(swipeMessage as String),
                                    image = chat.image(chat.messages.lastIndex, i),
                                    isEditAvailable = swipeIndexState.generatingIndex != i,
                                    isRegenerateAvailable = swipeIndexState.generatingIndex != i,
                                    modifier = messageModifier.width(messageWidth),
                                    onEditEnd = { edited ->
                                        edited?.let { ed ->
                                            swipes[i] = ed
                                            chat.messages.last().swipes?.set(i, ed)
                                            chat.messages.last().mes = ed
                                            chat.save()
                                        }
                                    },
                                    onRegenerate = {
                                        swipeIndexState = swipeIndexState.generating()
                                        val genIndex = swipeIndexState.generatingIndex!!
                                        if (generatingStatus.status) return@MessageView

                                        coroutineScope.launch {
                                            swipes[genIndex] = generatingText
                                            val result = generateCurrentSwipe()

                                            if (result == null) {
                                                swipes[genIndex] = chat.messages.last().swipes?.get(genIndex)
                                                    ?: "`***Error: Sorry... :(***`"
                                                swipeIndexState = swipeIndexState.stoppedGenerating()
                                            } else {
                                                swipes[genIndex] = result
                                                chat.updateSwipe(genIndex, result)

                                                if (settings.stable_diffusion_api_enabled) {
                                                    val imageResult = generateImageForCurrentSwipe()
                                                    imageResult?.let { img ->
                                                        chat.updateImage(
                                                            img,
                                                            chat.messages.lastIndex,
                                                            swipeIndexState.generatingIndex ?: 0
                                                        )
                                                    }
                                                }

                                                chat.save()
                                                swipeIndexState = swipeIndexState.stoppedGenerating()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            val scrollbarState = rememberScrollbarAdapter(messagesState)
            AppearDisappearAnimation(
                messagesState.canScrollBackward || messagesState.canScrollForward,
                normalAnimationDuration,
            ) {
                VerticalScrollbar(
                    scrollbarState,
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
                messagesState.scrollToItem(messages.lastIndex)
            }
        }

        var image by remember { mutableStateOf<ImageBitmap?>(null) }
        ChatControls(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(SolidColor(colorBackgroundSecond), alpha = transparency)
                .border(smallBorder, colorBorder),
            image = image,
            deletingStatus = deletingStatus,
            generatingStatus = generatingStatus,
            onSendMessage = { message ->
                var toClear = false
                val createUserMessage = (!messages.last().is_user && message.isNotEmpty()) || image != null
                if (createUserMessage) {
                    messages.add(chat.addMessage(message, is_user = true))
                    image?.let { chat.updateImage(it, chat.messages.lastIndex, swipeIndexState.index) }
                    chat.save()
                    toClear = true
                }

                swipes.clear()
                swipes.add(generatingText)
                swipeIndexState = swipeIndexState.reset()
                messages.add(chat.addMessage(generatingText))

                coroutineScope.launch {
                    swipeIndexState = swipeIndexState.generating()
                    messagesState.animateScrollToItem(messages.size)

                    if (settings.stable_diffusion_api_enabled && createUserMessage) {
                        image?.let {
                            val result = StableDiffusionWebUIClient.interrogate(it)
                            if (result != null) {
                                val message1 = chat.messages[chat.messages.lastIndex - 1]
                                message1.mes += "\n*Shows image: $result*"

                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar("Couldn't interrogate image")
                                }
                            }

                            image = null
                        }
                    }

                    val result = generateCurrentSwipe()

                    if (result == null) {
                        messages.removeLast()
                        chat.removeLast()
                        swipes.clear()
                        swipes.add(chat.messages.last().mes)

                        swipeIndexState = swipeIndexState.reset()

                    } else {
                        swipes[0] = result
                        chat.messages.last().swipes?.set(0, result)
                        messages.last().mes = result
                        chat.messages.last().mes = result

                        if (settings.stable_diffusion_api_enabled) {
                            val imageResult = generateImageForCurrentSwipe()
                            imageResult?.let { img ->
                                chat.updateImage(img, chat.messages.lastIndex, swipeIndexState.generatingIndex ?: 0)
                            }
                        }

                        swipeIndexState = swipeIndexState.stoppedGenerating()
                    }

                    chat.save()
                }

                toClear
            },
            onClearChat = {
                chat.clear()
                messages.clear()
                messages.addAll(chat.messages)
                swipeIndexState = swipeIndexState.reset()
                chat.save()
            },
            onNewChat = {
                val newChat = createNewChat(character)
                newChat.save()
                character.jsonData.chat = newChat.fileName
                messages.clear()
                messages.addAll(newChat.messages)
                onNewChat.invoke(newChat)
            },
            onMenageChats = { onChatManage.invoke() },
            onDeleteMessages = { deletingStatus = deletingStatus.enabled() },
            onDeleteCancel = { deletingStatus = deletingStatus.disabled() },
            onLoadImage = {
                val file = openFileDialog(window, "", listOf(""), false).firstOrNull()
                try {
                    file?.let { getImageBitmap(it)?.let { img ->
                        image = img
                    }}

                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Couldn't load image")
                    }
                }
            },
            onRemoveImage = { image = null },
            onEmptyLeft = {
                coroutineScope.launch {
                    swipeIndexState = swipeIndexState.left()
                    chat.swipeLeft()
                    swipeOccurred = true
                }
            },
            onEmptyRight = {
                coroutineScope.launch {
                    swipeIndexState = swipeIndexState.right()

                    if (!generatingStatus.status && swipeIndexState.generatingIndex != null) {
                        swipes.add(generatingText)

                        val result = generateCurrentSwipe()

                        if (result == null) {
                            swipes.removeLast()
                            swipeIndexState = swipeIndexState.failedNewSwipeGenerating()
                        } else {
                            swipeIndexState.generatingIndex?.let {
                                swipes[it] = result
                                chat.updateSwipe(it, result)
                            }

                            if (settings.stable_diffusion_api_enabled) {
                                val imageResult = generateImageForCurrentSwipe()
                                imageResult?.let { img ->
                                    chat.updateImage(
                                        img,
                                        chat.messages.lastIndex,
                                        swipeIndexState.generatingIndex ?: 0
                                    )
                                }
                            }
                            chat.save()

                            swipeIndexState = swipeIndexState.stoppedGenerating()
                        }
                    }

                    chat.swipeRight()
                    swipeOccurred = true
                }
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChatControls(
    image: ImageBitmap? = null,
    deletingStatus: DeletingStatus = DeletingStatus(),
    generatingStatus: GeneratingStatus = GeneratingStatus(),
    /**
     * @return true if to clear message field and false if to not.
     */
    onSendMessage: (String) -> Boolean = { true },
    onLoadImage: () -> Unit = {},
    onRemoveImage: () -> Unit = {},
    onClearChat: () -> Unit = {},
    onNewChat: () -> Unit = {},
    onMenageChats: () -> Unit = {},
    onDeleteMessages: () -> Unit = {},
    onDeleteCancel: () -> Unit = {},
    onEmptyLeft: () -> Unit = {},
    onEmptyRight: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val coroutineContext = rememberCoroutineScope()

    Row(
        modifier = modifier.padding(padding),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(padding)
    ) {
        var message by remember { mutableStateOf(TextFieldValue(text = "")) }
        var showOptions by remember { mutableStateOf(false) }

        Column {
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
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(padding),
            ) {
                Icon(
                    Icons.Default.Menu,
                    "chat options",
                    tint = if (generatingStatus.status) colorTextSecond else colorText,
                    modifier = Modifier
                        .width(iconSize)
                        .aspectRatio(1F)
                        .clickable(!generatingStatus.status) { showOptions = true }
                )

                if (deletingStatus.status) {
                    IconWithToolTip(
                        Icons.Default.Close,
                        "cancel removing",
                        toolTip = "cancel removing",
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

                if (!connectionStatus || (!sdConnectionStatus && settings.stable_diffusion_api_enabled)) {
                    var checking by remember { mutableStateOf(false) }
                    var text = ""

                    if (!connectionStatus) {
                        text += "no connection to KoboldAI\n"
                    }

                    if (!sdConnectionStatus && settings.stable_diffusion_api_enabled) {
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

                                    if (!sdConnectionStatus && settings.stable_diffusion_api_enabled) {
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

        BasicTextField(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1F)
                .background(colorBackgroundSecondLighter, RoundedCornerShape(corners))
                .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                .padding(padding)
                .onKeyEvent { keyEvent ->
                    if (message.text.isEmpty()) {
                        if (keyEvent.key == Key.DirectionLeft) {
                            onEmptyLeft()
                        }

                        if (keyEvent.key == Key.DirectionRight) {
                            onEmptyRight()
                        }
                    }

                    if (keyEvent.type == KeyEventType.KeyUp && keyEvent.isShiftPressed && keyEvent.key == Key.Enter) {
                        message = message.copy(
                            message.text.substring(
                                0,
                                message.selection.start
                            ) + "\n" + message.text.substring(message.selection.end),
                            selection = TextRange(message.selection.start + 1)
                        )

                    } else if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                        message = message.copy(
                            message.text.substring(0, message.selection.start - 1)
                                    + message.text.substring(message.selection.start, message.selection.end)
                                    + message.text.substring(message.selection.end),
                            selection = TextRange(message.selection.start + 1)
                        )

                        if (generatingStatus.status) return@onKeyEvent false

                        val toClear = onSendMessage.invoke(message.text)
                        if (toClear) {
                            message = message.copy("")
                        }
                    }
                    true
                },
            value = message,
            keyboardOptions = KeyboardOptions.Default.copy(autoCorrect = true),
            textStyle = TextStyle(
                color = colorText,
                fontSize = normalText,
            ),
            cursorBrush = SolidColor(colorText),
            onValueChange = {
                message = it
            },
        )

        LoadedImage(
            image,
            onRemoveImage,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
        )

        SendIcons(
            deletingStatus,
            generatingStatus,
            onSendMessage = {
                val toClear = onSendMessage.invoke(message.text)
                if (toClear) {
                    message = message.copy("")
                }
            },
            onLoadImage = onLoadImage,
        )
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
    deletingStatus: DeletingStatus = DeletingStatus(),
    generatingStatus: GeneratingStatus = GeneratingStatus(),
    /**
     * @return true if to clear message field and false if to not.
     */
    onSendMessage: () -> Unit = {},
    onLoadImage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = generatingStatus.status,
        animationSpec = tween(shortAnimationDuration),
    ) { status ->
        if (!status) {
            Column(
                modifier = modifier,
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    "send message",
                    tint = if (!deletingStatus.status) colorText else colorTextSecond,
                    modifier = Modifier
                        .width(70.dp)
                        .weight(1F)
                        .clickable(enabled = !deletingStatus.status, onClick = onSendMessage),
                )

                if (settings.stable_diffusion_api_enabled) {
                    Icon(
                        painterResource("add_photo.svg"),
                        "load image",
                        tint = if (!deletingStatus.status) colorText else colorTextSecond,
                        modifier = Modifier
                            .width(70.dp)
                            .weight(1F)
                            .clickable(enabled = !deletingStatus.status, onClick = onLoadImage),
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
    message: Message,
    image: ImageBitmap?,
    isEditAvailable: Boolean = true,
    isRegenerateAvailable: Boolean = false,
    onEditStart: () -> Unit = {},
    onEditEnd: (String?) -> Unit = {},
    onRegenerate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isEdit by remember { mutableStateOf(false) }
    var editMessage by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester by remember { mutableStateOf(FocusRequester()) }
    LaunchedEffect(isEdit) {
        if (isEdit) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .padding(padding)
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparencySecond)
            .border(smallBorder, colorBorder, RoundedCornerShape(corners)),
    ) {
        Row {
            Crossfade(
                targetState = if (message.is_user) Properties.getProfileImage()
                    ?: emptyImageBitmap else character.image,
                animationSpec = tween(longAnimationDuration),
            ) { newImage ->
                Image(
                    newImage,
                    contentDescription = "profile image of ${character.jsonData.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(imageWidth, imageHeight)
                        .padding(padding)
                        .background(colorBackground, RoundedCornerShape(corners))
                        .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                        .clip(RoundedCornerShape(corners)),
                )
            }

            SelectionContainer {
                Column(
                    modifier = Modifier.padding(padding)
                ) {
                    Text(if (message.is_user) user.name else message.name, fontSize = bigText, color = colorText)

                    Crossfade(
                        isEdit && isEditAvailable,
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
                                value = editMessage,
                                textStyle = TextStyle(
                                    color = colorText,
                                    fontSize = normalText,
                                ),
                                cursorBrush = SolidColor(colorText),
                                onValueChange = {
                                    editMessage = it
                                },
                            )
                        } else {
                            val fullMessage = message.mes.formatAnything(character)

                            val parser = Parser.builder().build()
                            val mainMessage = fullMessage.substringBeforeLast(showsImageString)
                            val root = parser.parse(mainMessage) as Document
                            MDDocument(root)

                            val imageMessageIndex = fullMessage.indexOf(showsImageString)
                            if (imageMessageIndex != -1) {
                                val imageMessage = fullMessage.substring(imageMessageIndex)
                                Text(
                                    imageMessage.replace("*", ""),
                                    color = colorTextSecond,
                                    fontSize = smallText,
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                        }
                    }

                    image?.let { img ->
                        Image(
                            img,
                            "generated image",
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
        }

        Crossfade(
            isEdit,
            animationSpec = tween(shortAnimationDuration),
            modifier = Modifier.align(Alignment.TopEnd),
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
                            .clickable {
                                onEditEnd.invoke(editMessage.text)
                                isEdit = false
                            }
                    )

                    Icon(
                        Icons.Default.Clear,
                        "cancel changes",
                        tint = colorTextError,
                        modifier = Modifier
                            .padding(padding)
                            .width(tinyIconSize)
                            .aspectRatio(1F)
                            .clickable {
                                onEditEnd.invoke(null)
                                isEdit = false
                            }
                    )
                }

            } else {
                Row {
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
                                .clickable {
                                    onEditStart.invoke()
                                    isEdit = true
                                    editMessage =
                                        TextFieldValue(message.mes, selection = TextRange(message.mes.length))
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeControls(
    state: SwipeState = SwipeState(),
    onSwipeRight: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isLeftAvailable) {
            Icon(
                Icons.Default.ArrowBack,
                "swipe left",
                tint = colorText,
                modifier = Modifier
                    .height(smallIconSize)
                    .aspectRatio(1F)
                    .clickable(onClick = onSwipeLeft)
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

        Text(state.toString(), fontSize = smallText, color = colorText)

        if (state.isRightAvailable) {
            Icon(
                Icons.Default.ArrowForward,
                "swipe right",
                tint = colorText,
                modifier = Modifier
                    .height(smallIconSize)
                    .aspectRatio(1F)
                    .clickable(onClick = onSwipeRight)
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
private fun Swipes(
    items: List<Any>,
    currentIndex: Int,
    previousIndex: Int,
    itemContent: @Composable (Int, Any) -> Unit,
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

private data class SwipeState(
    val index: Int = 0,
    val amount: Int = 1,
    val generatingIndex: Int? = null,
    private val onChange: (SwipeState) -> Unit = {},
) {
    fun right(): SwipeState {
        if (!isRightAvailable) return this
        return if (index + 1 == amount) {
            SwipeState(index + 1, amount + 1, amount, onChange)
        } else {
            SwipeState(index + 1, amount, generatingIndex, onChange)
        }.also { onChange.invoke(this) }
    }

    fun left(): SwipeState {
        if (!isLeftAvailable) return this
        return SwipeState(index - 1, amount, generatingIndex, onChange).also { onChange.invoke(this) }
    }

    fun generating(): SwipeState = SwipeState(
        index,
        amount,
        index,
        onChange,
    ).also { onChange.invoke(this) }

    fun stoppedGenerating(): SwipeState = SwipeState(
        index,
        amount,
        null,
        onChange,
    ).also { onChange.invoke(this) }

    fun failedNewSwipeGenerating(): SwipeState {
        return if (index == generatingIndex && generatingIndex == amount - 1) {
            SwipeState(index - 1, amount - 1, null, onChange)
        } else {
            SwipeState(index, amount, null, onChange)
        }.also { onChange.invoke(this) }
    }

    fun reset(): SwipeState = SwipeState(onChange = onChange).also { onChange.invoke(this) }

    val isLeftAvailable: Boolean get() = index > 0
    val isRightAvailable: Boolean get() = generatingIndex == null || index < (amount - 1)

    override fun toString(): String = "${index + 1}/$amount"

    companion object {
        const val generatingText = "***`Generating...`***"
    }
}

private data class GeneratingStatus(
    val status: Boolean = false,
    private val onChange: (Boolean) -> Unit = {},
) {
    fun new(newStatus: Boolean = this.status): GeneratingStatus {
        onChange.invoke(newStatus)
        return GeneratingStatus(newStatus, onChange)
    }
}

private data class DeletingStatus(
    val status: Boolean = false,
    val index: Int = -1,
    private val onChange: (Boolean) -> Unit = {},
) {
    fun new(index: Int = this.index): DeletingStatus {
        onChange.invoke(status)
        return DeletingStatus(status, index, onChange)
    }

    fun new(status: Boolean = this.status, index: Int = this.index): DeletingStatus {
        onChange.invoke(status)
        return DeletingStatus(status, index, onChange)
    }

    fun disabled(): DeletingStatus {
        onChange.invoke(status)
        return DeletingStatus(false, -1, onChange)
    }

    fun enabled(): DeletingStatus {
        onChange.invoke(status)
        return DeletingStatus(true, -1, onChange)
    }
}