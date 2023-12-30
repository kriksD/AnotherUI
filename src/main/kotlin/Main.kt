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
import character.chat.newChat.ChatLoader
import client.kobold.KoboldAIClient
import client.stablediffusion.StableDiffusionWebUIClient
import composableFunctions.AppearDisappearAnimation
import composableFunctions.LoadingIcon
import composableFunctions.screen.CharacterList
import composableFunctions.screen.ChatThread
import composableFunctions.window.ChatManagementWindow
import composableFunctions.window.CreateCharacterWindow
import composableFunctions.window.DeleteCharacterWindow
import composableFunctions.window.SettingsWindow
import games.GameArea
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
    Settings, ChatsManaging, CreateCharacter, DeleteCharacter,
}

enum class ViewType {
    List, Grid,
}

fun main() = application {
    var firstTimeSettings by remember { mutableStateOf(true) }
    if (firstTimeSettings) {
        Properties.loadStyle()
        Properties.loadSettings()
        Properties.loadLanguage()
        Properties.loadUser()

        firstTimeSettings = false
    }

    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        KoboldAIClient.checkModelName()
        if (settings.stableDiffusionApiEnabled) StableDiffusionWebUIClient.checkModelName()
    }

    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }

    val characters = remember { mutableStateListOf<ACharacter>() }
    var currentCharacter by remember { mutableStateOf<ACharacter?>(null) }
    var isCharacterRedacted by remember { mutableStateOf(false) }
    var characterToDelete by remember { mutableStateOf<ACharacter?>(null) }

    val chats = remember { ChatLoader() }
    var reloadChatThread by remember { mutableStateOf(false) }

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
    var isDeleteCharacterOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    var firstTime by remember { mutableStateOf(true) }
    if (firstTime) {
        LaunchedEffect(true) {
            withContext(Dispatchers.IO) {
                loadAllCharacters()
            }?.let { characters.addAll(it) }

            val chatLoaderTemp = ChatLoader()
            characters.forEach { character ->
                chatLoaderTemp.load(character)
            }

            firstTime = false
            screen = Screen.Characters
        }

        timer(initialDelay = 5000, period = 5000) {
            coroutineScope.launch {
                KoboldAIClient.checkModelName()
                if (settings.stableDiffusionApiEnabled) StableDiffusionWebUIClient.checkModelName()
            }
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

                    chats.selected?.let { selected ->
                        if (selected.messages.size == 1) {
                            val firstMessage = selected.messages.first()

                            if (!firstMessage.isUser) {
                                firstMessage.updateSwipe(0, char.jsonData.first_mes)
                                selected.save()
                            }
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
                    isDeleteCharacterOpen = win == OpenWindow.DeleteCharacter
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
                    isDeleteCharacterOpen = false
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
                                    chats.load(it)
                                    chats.selectIfNotSelected(it)
                                    screen = Screen.Chat
                                },
                                onNewCharacter = {
                                    openWindow(OpenWindow.CreateCharacter)
                                },
                                onDelete = { character ->
                                    characterToDelete = character
                                    openWindow(OpenWindow.DeleteCharacter)
                                }
                            )
                        }
                        Screen.Chat -> {
                            if (!reloadChatThread && chats.selected != null && currentCharacter != null) {
                                ChatThread(
                                    modifier = Modifier.fillMaxWidth(0.5F).weight(1F),
                                    window = window,
                                    character = currentCharacter ?: ACharacter("", CharacterInfo(name = ""), CharacterMetaData()),
                                    chat = chats.selected ?: throw Exception("Chat not found"),
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
                                    onNewChat = {
                                        currentCharacter?.let { chats.createChat(it) }
                                        reloadChatThread = true
                                    },
                                    onSplit = { messageIndex ->
                                        currentCharacter?.let { char ->
                                            chats.selected?.let { selectedChat ->
                                                val newChat = chats.createChat(char)
                                                newChat.messages.clear()
                                                newChat.messages.addAll(
                                                    selectedChat.messages.subList(0, messageIndex + 1).map { it.copy() }
                                                )
                                                newChat.save()

                                                chats.selectChat(newChat, char)
                                                reloadChatThread = true
                                            }
                                        }
                                    }
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
                        onBackgroundChange = { bg -> background = bg },
                        onClose = { closeWindow() },
                        onForceSaving = { saveSettings() },
                        onCharacterRedacted = { isCharacterRedacted = true }
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
                            chats = chats.chats,
                            onChatSelected = { chat ->
                                coroutineScope.launch { closeWindow() }
                                currentCharacter?.jsonData?.chat = chat.fileName
                                currentCharacter?.save()
                                currentCharacter?.let { chats.selectChat(chat, it) }
                                reloadChatThread = true
                            },
                            onDelete = { chat ->
                                chat.delete()
                                chats.removeChat(chat)
                                currentCharacter?.let { chats.selectIfNotSelected(it) }
                                reloadChatThread = true
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
                        onClose = { closeWindow() },
                        onDone = { newCharacter ->
                            characters.add(0, newCharacter)

                            saveSettings()
                            closeWindow()
                        },
                        onDoneMultiple = {
                            it.forEach { newCharacter ->
                                characters.add(0, newCharacter)
                            }

                            saveSettings()
                            closeWindow()
                        },
                        onLoadFail = {
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("Couldn't load character")
                            }
                        },
                        onLoadMultipleFail = {
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                var listString = ""
                                it.forEach { listString += it + "\n" }
                                snackbarHostState.showSnackbar("Couldn't load multiple characters:\n$listString")
                            }
                        }
                    )
                }
            }

            AppearDisappearAnimation(
                isDeleteCharacterOpen,
                normalAnimationDuration,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorBackground.copy(transparencyLight))
                ) {
                    DeleteCharacterWindow(
                        modifier = Modifier
                            .fillMaxWidth(0.3F)
                            .fillMaxHeight(0.4F)
                            .align(Alignment.Center),
                        onCancel = { closeWindow() },
                        onAccept = { withChats ->
                            if (withChats) {
                                characterToDelete?.let {
                                    File("data/chats/${it.fileName}").deleteRecursively()
                                }
                            }

                            characterToDelete?.delete()
                            characters.remove(characterToDelete)
                            characterToDelete = null

                            closeWindow()
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

            GameArea(
                modifier = Modifier.align(Alignment.CenterStart)
            )
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