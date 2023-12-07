package prompt

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import character.ACharacter
import character.chat.newChat.AChat2
import character.chat.newChat.AMessage2
import client.kobold.KoboldAIClient
import client.stablediffusion.ImagePrompt
import client.stablediffusion.StableDiffusionWebUIClient
import client.trimCorrectly
import settings
import showsImageString
import user

class Generator(
    val chat: AChat2,
    val character: ACharacter,
    private val snackbarHostState: SnackbarHostState,
) {
    private val generatingText = "***`Generating...`***"
    private var _isGenerating by mutableStateOf(false)
    var isGenerating
        get() = _isGenerating
        set(value) {
            _isGenerating = value
            generatingSwipeIndex = -1
            isGeneratingUserMessage = false
        }
    var generatingSwipeIndex by mutableStateOf(-1 )
        private set
    var isGeneratingUserMessage by mutableStateOf(false)

    suspend fun generateNewMessage(
        userMessage: String? = null,
        userImage: ImageBitmap? = null,
        withImage: Boolean = false
    ) {
        isGenerating = true

        userMessage?.let { um ->
            val message = chat.addMessage(um, user.name, true)
            userImage?.let { addImage(message, it) }
            chat.save()
        }

        val prompt = PromptBuilder()
            .pattern(settings.promptSettings.pattern)
            .systemPrompt(settings.promptSettings.systemPrompt)
            .character(character)
            .chat(chat)
            .type(settings.promptSettings.type)
            .build()

        val message = chat.addMessage(generatingText, character.jsonData.name, false)

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            chat.removeLast()
            isGenerating = false
            showErrorKobold()
            return
        }

        message.updateSwipe(0, result.trimCorrectly())

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating = false
    }

    /**
     * @throws CouldNotGenerateException if the message could not be generated
     */
    suspend fun generateUserMessage(userMessage: String = ""): String {
        isGenerating = true
        isGeneratingUserMessage = true

        val promptBuilder = PromptBuilder()
            .pattern(settings.promptSettings.pattern)
            .systemPrompt(settings.promptSettings.systemPrompt)
            .character(character)
            .chat(chat)
            .type(settings.promptSettings.type)
            .forUser()
            .complete(userMessage.isNotBlank())

        if (userMessage.isNotBlank()) {
            promptBuilder.addAdditionalMessage(
                AMessage2(
                    name = user.name,
                    isUser = true,
                    swipeId = mutableStateOf(0),
                    swipes = mutableStateListOf(userMessage),
                    sendDate = 0)
            )
        }

        val prompt = promptBuilder.build()

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            isGenerating = false
            showErrorKobold()
            throw CouldNotGenerateException()
        }

        isGenerating = false

        return "$userMessage$result".trimCorrectly()
    }

    suspend fun completeMessage(
        withImage: Boolean = false
    ) {
        isGenerating = true

        val message = chat.messages.lastOrNull() ?: return
        generatingSwipeIndex = message.swipeId.value
        val oldContent = message.content

        val prompt = PromptBuilder()
            .pattern(settings.promptSettings.pattern)
            .systemPrompt(settings.promptSettings.systemPrompt)
            .character(character)
            .chat(chat)
            .type(settings.promptSettings.type)
            .complete()
            .build()

        val generatingContent = "${message.content} $generatingText"
        message.updateSwipe(generatingSwipeIndex, generatingContent)

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            message.updateSwipe(generatingSwipeIndex, oldContent)
            isGenerating = false
            showErrorKobold()
            return
        }

        val newContent = "$oldContent$result"
        message.updateSwipe(generatingSwipeIndex, newContent.trimCorrectly())

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating = false
    }

    suspend fun regenerateMessage(
        withImage: Boolean = false
    ) {
        isGenerating = true

        val message = chat.messages.lastOrNull()?: return
        generatingSwipeIndex = message.swipeId.value
        val oldContent = message.content

        message.updateSwipe(generatingSwipeIndex, generatingText)

        val prompt = PromptBuilder()
            .pattern(settings.promptSettings.pattern)
            .systemPrompt(settings.promptSettings.systemPrompt)
            .character(character)
            .chat(chat)
            .type(settings.promptSettings.type)
            .regenerate()
            .build()

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            message.updateSwipe(generatingSwipeIndex, oldContent)
            isGenerating = false
            showErrorKobold()
            return
        }

        message.updateSwipe(generatingSwipeIndex, result.trimCorrectly())

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating = false
    }

    suspend fun generateNextSwipe(
        withImage: Boolean = false
    ) {
        isGenerating = true

        val message = chat.messages.lastOrNull() ?: return
        message.swipes.add(generatingText)
        message.swipeId.value = message.swipes.lastIndex
        generatingSwipeIndex = message.swipeId.value

        val prompt = PromptBuilder()
            .pattern(settings.promptSettings.pattern)
            .systemPrompt(settings.promptSettings.systemPrompt)
            .character(character)
            .chat(chat)
            .type(settings.promptSettings.type)
            .regenerate()
            .build()

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            message.swipes.removeLast()
            message.swipeId.value = message.swipes.lastIndex
            isGenerating = false
            showErrorKobold()
            return
        }

        message.updateSwipe(generatingSwipeIndex, result.trimCorrectly())

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating = false
    }

    private suspend fun addImage(message: AMessage2, image: ImageBitmap?) {
        if (image == null) return

        val imageDescription = StableDiffusionWebUIClient.interrogate(image) ?: run {
            showErrorStableDiffusionInterrogate()
            return
        }

        val name = chat.saveImage(image)
        message.updateImage(message.swipeId.value, name)

        message.updateSwipe(
            message.swipeId.value,
            "${message.swipes[message.swipeId.value]}\n${imageDescription.let { "$showsImageString $it*" }}"
        )
    }

    private suspend fun generateImage(messageContent: String): ImageBitmap? {
        val prompt = ImagePrompt.createPrompt(messageContent)
        return StableDiffusionWebUIClient.generate(prompt)
            ?: run {
                showErrorStableDiffusion()
                null
            }
    }

    private suspend fun showErrorKobold() {
        snackbarHostState.currentSnackbarData?.dismiss()

        if (!KoboldAIClient.connectionStatus) {
            snackbarHostState.showSnackbar("No connection")
        } else {
            snackbarHostState.showSnackbar("Couldn't generate")
        }
    }

    private suspend fun showErrorStableDiffusion() {
        snackbarHostState.currentSnackbarData?.dismiss()

        if (!StableDiffusionWebUIClient.connectionStatus) {
            snackbarHostState.showSnackbar("No connection")
        } else {
            snackbarHostState.showSnackbar("Couldn't generate")
        }
    }

    private suspend fun showErrorStableDiffusionInterrogate() {
        snackbarHostState.currentSnackbarData?.dismiss()

        if (!StableDiffusionWebUIClient.connectionStatus) {
            snackbarHostState.showSnackbar("No connection")
        } else {
            snackbarHostState.showSnackbar("Couldn't generate")
        }
    }
}

class CouldNotGenerateException : Exception("Couldn't generate")