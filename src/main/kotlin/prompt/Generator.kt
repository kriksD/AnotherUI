package prompt

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import character.ACharacter
import character.chat.newChat.AChat2
import character.chat.newChat.AMessage2
import client.kobold.KoboldAIClient
import client.stablediffusion.ImagePrompt
import client.stablediffusion.StableDiffusionWebUIClient
import showsImageString
import user

class Generator(
    val chat: AChat2,
    val character: ACharacter,
    private val snackbarHostState: SnackbarHostState,
) {
    private val generatingText = "***`Generating...`***"
    var isGenerating = mutableStateOf(false)

    suspend fun generateNewMessage(
        userMessage: String? = null,
        userImage: ImageBitmap? = null,
        withImage: Boolean = false
    ) {
        isGenerating.value = true

        userMessage?.let { um ->
            val message = chat.addMessage(um, user.name, true)
            userImage?.let { addImage(message, it) }
        }

        val prompt = PromptBuilder()
            .systemPrompt("Enter RP mode. You shall reply to {{user}} while staying in character. Your responses must be detailed, creative, immersive, and drive the scenario forward. You will follow {{char}}'s persona.")
            .character(character)
            .chat(chat)
            .type(PromptType.Instruct)
            .build()

        val message = chat.addMessage(generatingText, character.jsonData.name, false)

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            chat.removeLast()
            isGenerating.value = false
            showErrorKobold()
            return
        }

        message.updateSwipe(0, result)

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating.value = false
    }

    /**
     * @throws CouldNotGenerateException if the message could not be generated
     */
    suspend fun generateUserMessage(): String {
        isGenerating.value = true

        val prompt = PromptBuilder()
            .systemPrompt("Enter RP mode. You shall reply to {{user}} while staying in character. Your responses must be detailed, creative, immersive, and drive the scenario forward. You will follow {{char}}'s persona.")
            .character(character)
            .chat(chat)
            .type(PromptType.Instruct)
            .forUser()
            .build()

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            isGenerating.value = false
            showErrorKobold()
            throw CouldNotGenerateException()
        }

        isGenerating.value = false

        return result
    }

    suspend fun completeMessage(
        withImage: Boolean = false
    ) {
        isGenerating.value = true

        val message = chat.messages.lastOrNull() ?: return
        val swipeID = message.swipeId.value
        val oldContent = message.content

        val prompt = PromptBuilder()
            .systemPrompt("Enter RP mode. You shall reply to {{user}} while staying in character. Your responses must be detailed, creative, immersive, and drive the scenario forward. You will follow {{char}}'s persona.")
            .character(character)
            .chat(chat)
            .type(PromptType.Instruct)
            .complete()
            .build()

        val generatingContent = "${message.content} $generatingText"
        message.updateSwipe(swipeID, generatingContent)

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            message.updateSwipe(swipeID, oldContent)
            isGenerating.value = false
            showErrorKobold()
            return
        }

        val newContent = "$oldContent$result"
        message.updateSwipe(swipeID, newContent)

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating.value = false
    }

    suspend fun regenerateMessage(
        withImage: Boolean = false
    ) {
        isGenerating.value = true

        val message = chat.messages.lastOrNull()?: return
        val swipeID = message.swipeId.value
        val oldContent = message.content

        message.updateSwipe(swipeID, generatingText)

        val prompt = PromptBuilder()
            .systemPrompt("Enter RP mode. You shall reply to {{user}} while staying in character. Your responses must be detailed, creative, immersive, and drive the scenario forward. You will follow {{char}}'s persona.")
            .character(character)
            .chat(chat)
            .type(PromptType.Instruct)
            .regenerate()
            .build()

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            message.updateSwipe(swipeID, oldContent)
            isGenerating.value = false
            showErrorKobold()
            return
        }

        message.updateSwipe(swipeID, result)

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating.value = false
    }

    suspend fun generateNextSwipe(
        withImage: Boolean = false
    ) {
        isGenerating.value = true

        val message = chat.messages.lastOrNull() ?: return
        message.swipes.add(generatingText)
        message.swipeId.value = message.swipes.lastIndex
        val swipeID = message.swipeId.value

        val prompt = PromptBuilder()
            .systemPrompt("Enter RP mode. You shall reply to {{user}} while staying in character. Your responses must be detailed, creative, immersive, and drive the scenario forward. You will follow {{char}}'s persona.")
            .character(character)
            .chat(chat)
            .type(PromptType.Instruct)
            .regenerate()
            .build()

        val result = prompt?.let { KoboldAIClient.generate(it, character) } ?: run {
            message.swipes.removeLast()
            message.swipeId.value = message.swipes.lastIndex
            isGenerating.value = false
            showErrorKobold()
            return
        }

        message.updateSwipe(swipeID, result)

        if (withImage) {
            val image = generateImage(result)
            addImage(message, image)
        }

        chat.save()

        isGenerating.value = false
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