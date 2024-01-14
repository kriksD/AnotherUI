package prompt

import character.ACharacter
import character.chat.newChat.AChat2
import character.chat.newChat.AMessage2
import client.kobold.KoboldAIClient
import client.format
import settings
import user

class PromptBuilder {
    private var type: PromptType? = null
    private var complete: Boolean = false
    private var regenerate: Boolean = false
    private var forUser: Boolean = false
    private var character: ACharacter? = null
    private var chat: AChat2? = null
    private var systemPrompt: String? = null
    private var additionalMessages: MutableList<AMessage2> = mutableListOf()
    private var pattern: String = """
        {{systemPrompt}}
        {{persona}}
        {{personality}}
        {{scenario}}
        {{messageExample}}
        {{chat}}
    """.trimIndent()

    fun type(type: PromptType): PromptBuilder {
        this.type = type
        return this
    }

    fun complete(complete: Boolean = true): PromptBuilder {
        this.complete = complete
        return this
    }

    fun regenerate(regenerate: Boolean = true): PromptBuilder {
        this.regenerate = regenerate
        return this
    }

    fun forUser(forUser: Boolean = true): PromptBuilder {
        this.forUser = forUser
        return this
    }

    fun character(character: ACharacter): PromptBuilder {
        this.character = character
        return this
    }

    fun chat(chat: AChat2): PromptBuilder {
        this.chat = chat
        return this
    }

    fun systemPrompt(systemPrompt: String): PromptBuilder {
        this.systemPrompt = systemPrompt
        return this
    }

    fun addAdditionalMessage(message: AMessage2): PromptBuilder {
        this.additionalMessages.add(message)
        return this
    }

    fun pattern(pattern: String): PromptBuilder {
        this.pattern = pattern
        return this
    }

    suspend fun build(): Prompt? {
        val prompt = when (type) {
            PromptType.Instruct -> makeInstructPrompt(character, chat)
            PromptType.Chat -> makeChatPrompt(character, chat)
            null -> null
        } ?: return null

        return Prompt(
            prompt = prompt,
            stop_sequence = settings.promptSettings.splitStopSequence,
            use_story = false,
            use_memory = false,
            use_authors_note = false,
            use_world_info = false,
            max_context_length = settings.generating.maxContextLength,
            max_length = settings.generating.maxLength,
            rep_pen = settings.generating.repPen,
            rep_pen_range = settings.generating.repPenRange,
            rep_pen_slope = settings.generating.repPenSlope,
            temperature = settings.generating.temperature,
            tfs = settings.generating.tfs,
            top_a = settings.generating.topA,
            top_k = settings.generating.topK,
            top_p = settings.generating.topP,
            min_p = settings.generating.minP,
            typical = settings.generating.typical,
            sampler_order = settings.generating.samplerOrder,
            use_default_badwordsids = false,
            seed = settings.generating.seed,
        )
    }

    private suspend fun makeInstructPrompt(character: ACharacter?, chat: AChat2?): String? {
        character ?: return null
        chat ?: return null

        var prompt = pattern

        systemPrompt?.let { spr ->
            val systemInstruct = settings.promptSettings.systemInstruct.replace("\\n", "\n")

            prompt = prompt.replace(
                "{{systemPrompt}}",
                if (spr.isNotEmpty())
                    if (systemInstruct.contains("{{prompt}}"))
                        systemInstruct.replace("{{prompt}}", spr.format(character))
                    else
                        "${systemInstruct}${spr.format(character)}"
                else ""
            )
        } ?: run { prompt.replace("{{systemPrompt}}", "") }

        character.jsonData.description.let { des ->
            prompt = prompt.replace(
                "{{persona}}",
                if (des.isNotEmpty()) "${character.jsonData.name}'s Persona: ${des.format(character)}" else ""
            )
        }

        character.jsonData.personality.let { per ->
            prompt = prompt.replace(
                "{{personality}}",
                if (per.isNotEmpty()) "Personality: ${per.format(character)}" else ""
            )
        }

        character.jsonData.scenario.let { sce ->
            prompt = prompt.replace(
                "{{scenario}}",
                if (sce.isNotEmpty()) "Scenario: ${sce.format(character)}" else ""
            )
        }

        character.jsonData.mes_example.let { mex ->
            prompt = prompt.replace(
                "{{messageExample}}",
                if (mex.isNotEmpty()) mex.format(character) else ""
            )
        }

        val mergedMessages = chat.messages + additionalMessages
        val maxContextLength = settings.generating.maxContextLength
        val currentTokenCount = KoboldAIClient.countTokens(prompt)
        if (currentTokenCount == -1) return null
        var tokensLeft = maxContextLength - currentTokenCount - 32
        println("tokensLeft: $tokensLeft")
        var messagesPrompt = ""
        val lastIndex = if (regenerate) mergedMessages.lastIndex - 1 else mergedMessages.lastIndex

        for (messageIndex in lastIndex downTo 0) {
            val message = mergedMessages[messageIndex]

            val messageTokenCount = KoboldAIClient.countTokens(message.content)
            if (messageTokenCount == -1) return null

            tokensLeft -= messageTokenCount
            if (tokensLeft < 0) break

            val instructMessage = if (messageIndex == lastIndex && complete) {
                message.lastStringInstruct.format(character)
            } else {
                message.stringInstruct.format(character)
            }
            messagesPrompt = "$instructMessage\n$messagesPrompt"
        }

        messagesPrompt.let { mpr ->
            prompt = prompt.replace(
                "{{chat}}",
                if (mpr.isNotEmpty()) mpr.dropLast(1).format(character) else ""
            )
        }

        if (!complete) {
            val instruct = (if (forUser) settings.promptSettings.userInstruct else settings.promptSettings.modelInstruct)
                .replace("\\n", "\n")
                .substringBefore("{{prompt}}")
                .trimEnd { it == '\n' || it == ' ' }
                .format(character)

            prompt += "\n$instruct"
        }

        return prompt
    }

    private suspend fun makeChatPrompt(character: ACharacter?, chat: AChat2?): String? {
        character ?: return null
        chat ?: return null

        var prompt = pattern.replace("{{systemPrompt}}", "")

        character.jsonData.description.let { des ->
            prompt = prompt.replace(
                "{{persona}}",
                if (des.isNotEmpty()) "${character.jsonData.name}'s Persona: ${des.format(character)}" else ""
            )
        }

        character.jsonData.personality.let { per ->
            prompt = prompt.replace(
                "{{personality}}",
                if (per.isNotEmpty()) "Personality: ${per.format(character)}" else ""
            )
        }

        character.jsonData.scenario.let { sce ->
            prompt = prompt.replace(
                "{{scenario}}",
                if (sce.isNotEmpty()) "Scenario: ${sce.format(character)}" else ""
            )
        }

        character.jsonData.mes_example.let { mex ->
            prompt = prompt.replace(
                "{{messageExample}}",
                if (mex.isNotEmpty()) mex.format(character) else ""
            )
        }

        val mergedMessages = chat.messages + additionalMessages
        val maxContextLength = settings.generating.maxContextLength
        val currentTokenCount = KoboldAIClient.countTokens(prompt)
        if (currentTokenCount == -1) return null
        var tokensLeft = maxContextLength - currentTokenCount - 32
        println("tokensLeft: $tokensLeft")
        var messagesPrompt = ""
        val lastIndex = if (regenerate) mergedMessages.lastIndex - 1 else mergedMessages.lastIndex

        for (messageIndex in lastIndex downTo 0) {
            val message = mergedMessages[messageIndex]

            val messageTokenCount = KoboldAIClient.countTokens(message.content)
            if (messageTokenCount == -1) return null

            tokensLeft -= messageTokenCount
            if (tokensLeft < 0) break

            messagesPrompt = "${message.string.format(character)}\n$messagesPrompt"
        }

        messagesPrompt.let { mpr ->
            prompt = prompt.replace(
                "{{chat}}",
                if (mpr.isNotEmpty()) mpr.format(character) else ""
            )
        }

        if (!complete) {
            prompt += "\n${if (forUser) user.name else character.jsonData.name}:"
        }

        return prompt
    }
}