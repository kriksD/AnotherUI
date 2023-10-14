// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import character.*
import character.chat.Chat
import character.chat.ChatInfo
import character.chat.legacyChat.LegacyChat
import client.KoboldAIClient
import client.StableDiffusionWebUIClient
import composableFunctions.AppearDisappearAnimation
import composableFunctions.LoadingIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import properties.Properties
import java.io.File
import kotlin.concurrent.timer

enum class Screen {
    Characters, Chat, Loading,
}

enum class OpenWindow {
    Settings, ChatsManaging, CreateCharacter,
}

enum class ViewType {
    List, Grid,
}

fun main() = application {
    Properties.loadStyle()
    Properties.loadSettings()
    Properties.loadLanguage()
    Properties.loadUser()

    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        KoboldAIClient.checkModelName()
        if (settings.stable_diffusion_api_enabled) StableDiffusionWebUIClient.checkModelName()
    }

    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }

    val characters = remember { mutableStateListOf<ACharacter>() }
    var currentCharacter by remember { mutableStateOf<ACharacter?>(null) }
    var isCharacterRedacted by remember { mutableStateOf(false) }
    val chats = remember { mutableStateListOf<Chat>() }
    var currentChat by remember { mutableStateOf<Chat?>(null) }
    var reloadChatThread by remember { mutableStateOf(false) }

    fun loadChats() {
        if (currentCharacter == null) return

        loadAllChats(File("data/chats/${currentCharacter?.fileName}"))?.let {
            chats.clear()
            chats.addAll(it)

            val chat = chats.find { c -> c.fileName == currentCharacter!!.jsonData.chat }
            currentChat = chat ?: chats.lastOrNull() ?: run {
                val newChat = createNewChat(currentCharacter!!)
                newChat.save()
                chats.add(newChat)
                newChat
            }

            currentChat?.fileName?.let { fn -> currentCharacter!!.jsonData.chat = fn }

        } ?: run {
            currentCharacter?.let {
                val newChat = createNewChat(it)
                newChat.save()
                chats.add(newChat)
            }
        }
    }

    fun addChat(chat: Chat) {
        chats.add(chat)
        currentChat = chat
    }

    var background by remember {
        mutableStateOf(
            getImageBitmap(File("data/backgrounds/${settings.background}")) ?: emptyImageBitmap
        )
    }
    var headerText by remember { mutableStateOf("Characters") }
    var screen by remember { mutableStateOf(Screen.Loading) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isChatManagementOpen by remember { mutableStateOf(false) }
    var isCreateCharacterOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    var firstTime by remember { mutableStateOf(true) }
    if (firstTime) {
        timer(initialDelay = 5000, period = 5000) {
            coroutineScope.launch {
                KoboldAIClient.checkModelName()
                if (settings.stable_diffusion_api_enabled) StableDiffusionWebUIClient.checkModelName()
            }
        }
        LaunchedEffect(true) {
            withContext(Dispatchers.IO) {
                loadAllCharacters()
            }?.let { characters.addAll(it) }

            firstTime = false
            screen = Screen.Characters
        }
    }

    fun saveSettings() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                saving = true

                Properties.saveUser()
                Properties.saveStyle()
                Properties.saveSettings()
                currentCharacter?.let { char ->
                    if (isCharacterRedacted) {
                        char.save()
                        isCharacterRedacted = false
                    }

                    chats.find { it.fileName == char.jsonData.chat }?.let { chat ->
                        if (chat.messages.size == 1) {
                            chat.messages.first().mes = char.jsonData.first_mes
                            chat.save()
                        }
                    }
                }

                saving = false
            }
        }
    }

    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
        position = WindowPosition(Alignment.Center),
        width = 1500.dp,
        height = 800.dp
    )

    Window(
        onCloseRequest = {
            coroutineScope.launch {
                saveSettings()
                exitApplication()
            }
        },
        state = windowState,
        title = "AnotherUI ${Properties.version}"
    ) {
        fun openWindow(win: OpenWindow) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    if (isSettingsOpen || isChatManagementOpen) saveSettings()

                    isSettingsOpen = win == OpenWindow.Settings
                    isChatManagementOpen = win == OpenWindow.ChatsManaging
                    isCreateCharacterOpen = win == OpenWindow.CreateCharacter
                }
            }
        }

        fun closeWindow() {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    if (isSettingsOpen || isChatManagementOpen) saveSettings()

                    if (isSettingsOpen) {
                        KoboldAIClient.removeConnectionChangeCheck("settingsWindow")
                        StableDiffusionWebUIClient.removeConnectionChangeCheck("settingsWindow")
                    }

                    isSettingsOpen = false
                    isChatManagementOpen = false
                    isCreateCharacterOpen = false
                }
            }
        }

        Crossfade(targetState = background, animationSpec = tween(1000)) { newBackground ->
            Image(
                bitmap = newBackground,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().background(colorBackground.copy(transparency))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppearDisappearAnimation(
                    !firstTime,
                    normalAnimationDuration,
                ) {
                    ToolBar(
                        headerText,
                        screen,
                        onBack = {
                            if (isSettingsOpen || isChatManagementOpen || isCreateCharacterOpen) {
                                closeWindow()

                            } else {
                                headerText = "Characters"
                                currentCharacter = null
                                chats.clear()
                                currentChat = null
                                KoboldAIClient.removeConnectionChangeCheck("chatThread")
                                StableDiffusionWebUIClient.removeConnectionChangeCheck("chatThread")
                                screen = Screen.Characters
                            }
                        },
                        onSettings = {
                            if (isSettingsOpen) {
                                closeWindow()
                            } else {
                                openWindow(OpenWindow.Settings)
                            }
                        },
                        backEnabled = !isGenerating || isSettingsOpen || isChatManagementOpen || isCreateCharacterOpen,
                        modifier = Modifier
                            .fillMaxWidth(0.5F)
                            .background(colorBackgroundSecond.copy(transparency))
                            .border(smallBorder, colorBorder)
                            .padding(8.dp),
                    )
                }

                Crossfade(
                    screen,
                    animationSpec = tween(normalAnimationDuration),
                ) { screen2 ->
                    when (screen2) {
                        Screen.Characters -> {
                            CharacterList(
                                modifier = Modifier.fillMaxWidth(0.5F).weight(1F),
                                characters,
                                onCharacterClick = {
                                    headerText = it.jsonData.name
                                    currentCharacter = it
                                    loadChats()
                                    screen = Screen.Chat
                                },
                                onNewCharacter = {
                                    openWindow(OpenWindow.CreateCharacter)
                                },
                            )
                        }
                        Screen.Chat -> {
                            if (!reloadChatThread && currentChat != null && currentCharacter != null) {
                                ChatThread(
                                    modifier = Modifier.fillMaxWidth(0.5F).weight(1F),
                                    window = window,
                                    character = currentCharacter ?: ACharacter("", CharacterInfo(name = "")),
                                    chat = currentChat
                                        ?: LegacyChat("", "", ChatInfo("", "", 0), mutableListOf()),
                                    snackbarHostState = snackbarHostState,
                                    onChatManage = {
                                        openWindow(OpenWindow.ChatsManaging)
                                    },
                                    onGeneratingStatusChange = {
                                        isGenerating = it
                                        if (isGenerating && isChatManagementOpen) {
                                            isChatManagementOpen = false
                                        }
                                    },
                                    onNewChat = { addChat(it) },
                                )
                            } else {
                                reloadChatThread = false
                            }
                        }
                        Screen.Loading -> {
                            LoadScreen(modifier = Modifier.fillMaxWidth(0.5F))
                        }
                    }
                }
            }

            AppearDisappearAnimation(
                isSettingsOpen,
                normalAnimationDuration,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorBackground.copy(transparencyLight))
                ) {
                    SettingsWindow(
                        modifier = Modifier
                            .fillMaxWidth(0.45F)
                            .fillMaxHeight(0.7F)
                            .align(Alignment.Center),
                        character = currentCharacter,
                        window = this@Window.window,
                        onBackgroundChange = { bg ->
                            background = bg
                        },
                        onClose = {
                            closeWindow()
                        },
                        onForceSaving = {
                            saveSettings()
                        },
                        onCharacterRedacted = {
                            isCharacterRedacted = true
                        }
                    )
                }
            }

            AppearDisappearAnimation(
                isChatManagementOpen,
                normalAnimationDuration,
            ) {
                currentCharacter?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorBackground.copy(transparencyLight))
                    ) {
                        ChatManagementWindow(
                            modifier = Modifier
                                .fillMaxWidth(0.45F)
                                .fillMaxHeight(0.7F)
                                .align(Alignment.Center),
                            character = it,
                            chats = chats,
                            onChatSelected = { chat ->
                                coroutineScope.launch { closeWindow() }
                                currentCharacter?.jsonData?.chat = chat.fileName
                                currentCharacter?.save()
                                currentChat = chat
                                reloadChatThread = true
                            },
                            onDelete = { chat ->
                                if (chat == currentChat) {
                                    currentChat = null
                                }
                                chat.delete()
                                chats.remove(chat)
                                loadChats()
                            },
                            onClose = {
                                coroutineScope.launch { closeWindow() }
                                reloadChatThread = true
                            }
                        )
                    }
                }
            }

            AppearDisappearAnimation(
                isCreateCharacterOpen,
                normalAnimationDuration,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorBackground.copy(transparencyLight))
                ) {
                    CreateCharacterWindow(
                        modifier = Modifier
                            .fillMaxWidth(0.3F)
                            .fillMaxHeight(0.4F)
                            .align(Alignment.Center),
                        window = window,
                        onClose = {
                            closeWindow()
                        },
                        onDone = { newCharacter ->
                            characters.add(0, newCharacter)

                            saveSettings()
                            closeWindow()
                        },
                        onLoadFail = {
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("Couldn't load character")
                            }
                        }
                    )
                }
            }

            SavingIndicator(modifier = Modifier.align(Alignment.Center), saving)

            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.align(Alignment.TopEnd)
            ) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(biggerPadding)
                        .background(colorBackgroundSecondLighter, RoundedCornerShape(corners))
                        .border(smallBorder, colorBorder, RoundedCornerShape(corners))
                        .fillMaxWidth(0.24F),
                    backgroundColor = colorBackgroundSecondLighter,
                    shape = RoundedCornerShape(corners),
                    elevation = 0.dp,
                    contentColor = colorText,
                ) {
                    Text(data.message, color = colorText, fontSize = normalText)
                }
            }
        }
    }
}

@Composable
private fun ToolBar(
    headerText: String,
    screen: Screen,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    backEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(biggerPadding)
        ) {
            if (screen == Screen.Chat) {
                Icon(
                    Icons.Default.ArrowBack,
                    "back to characters",
                    tint = if (backEnabled) colorText else colorTextSecond,
                    modifier = Modifier
                        .height(iconSize)
                        .aspectRatio(1F)
                        .clickable(backEnabled, onClick = onBack),
                )
            }
            Text("AnotherUI - $headerText", color = colorText, fontSize = 34.sp)
        }
        Icon(
            Icons.Default.Settings,
            "settings",
            tint = colorText,
            modifier = Modifier
                .height(iconSize)
                .aspectRatio(1F)
                .clickable(onClick = onSettings)
        )
    }
}

@Composable
private fun LoadScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LoadingIcon(
            modifier = Modifier
                .size(iconSize * 2)
                .align(Alignment.Center),
        )
    }
}

@Composable
private fun SavingIndicator(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
) {
    var visible by remember { mutableStateOf(isVisible) }

    LaunchedEffect(isVisible) {
        visible = isVisible
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = shortAnimationDuration,
                easing = FastOutLinearInEasing
            )
        ) + slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(
                durationMillis = shortAnimationDuration,
                easing = LinearEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = shortAnimationDuration,
                easing = FastOutLinearInEasing
            )
        ) + slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                durationMillis = shortAnimationDuration,
                easing = LinearEasing
            )
        )
    ) {
        Row(
            modifier = Modifier
                .background(SolidColor(colorBackground), RoundedCornerShape(corners), transparencySecond)
                .padding(biggerPadding + biggerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            LoadingIcon(
                modifier = Modifier
                    .size(tinyIconSize)
            )

            Text(
                "Saving...",
                color = colorText,
                fontSize = bigText,
            )
        }
    }
}