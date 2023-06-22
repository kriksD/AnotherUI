package client

import character.ACharacter
import character.chat.Chat
import gpt2Tokenizer.GlobalTokenizer
import settings
import kotlin.math.max

class PromptManager(
    val character: ACharacter,
    val chat: Chat,
) {
    var prompt: Prompt = Prompt.createPrompt(character, chat)
        private set

    private var currentLastMessageIndex: Int = 0

    fun update() {
        val promptData = Prompt.createPromptWithByproduct(
            character,
            chat,
            if (settings.changeable_context.enabled) currentLastMessageIndex else 0,
        )
        prompt = promptData.first

        println("Buffer index: $currentLastMessageIndex")
        println("New index: ${promptData.second.lastMessageIndex}")
        println("Biggest index: ${chat.messages.lastIndex}")
        println("Context: ${GlobalTokenizer.countTokens(prompt.prompt)}")

        val isContextFull = currentLastMessageIndex != promptData.second.lastMessageIndex
        currentLastMessageIndex = promptData.second.lastMessageIndex

        if (settings.changeable_context.enabled && isContextFull) {
            currentLastMessageIndex = promptData.second.lastIndexWithRemovedBuffer(settings.changeable_context.buffer)
            println("/TRIGERED\\")
        }
    }
}