package prompt

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import character.ACharacter
import character.CharacterInfo
import character.CharacterMetaData
import chat.Chat
import chat.Message
import properties.settings.prompt.PromptSettings

class PromptBuilderTest {

    val promptSettings = PromptSettings(
        type = PromptType.Instruct,
        pattern = """
                {{systemPrompt}}
                {{persona}}
                {{personality}}
                {{scenario}}
                {{userDescription}}
                {{messageExample}}
                {{chat}}
            """.trimIndent(),
        systemPrompt = "test system prompt",
        systemInstruct = "<|system|>{{prompt}}",
        modelInstruct = "<model>{{prompt}}",
        userInstruct = "<|user|>{{prompt}}",
        stopSequence = "<|model|>,<|user|>",
    )

    val chat = Chat(
        fileName = "test",
        folderName = "test",
        createDate = 111,
        messages = mutableStateListOf(
            Message(
                name = "test_model",
                isUser = false,
                sendDate = 222,
                swipeId = mutableStateOf(0),
                swipes = mutableStateListOf("test_message"),
            ),
            Message(
                name = "test_user",
                isUser = true,
                sendDate = 333,
                swipeId = mutableStateOf(0),
                swipes = mutableStateListOf("test_message2"),
            ),
            Message(
                name = "test_model",
                isUser = false,
                sendDate = 444,
                swipeId = mutableStateOf(0),
                swipes = mutableStateListOf("test_message3"),
            ),
        )
    )

    val character = ACharacter(
        fileName = "test_model",
        jsonData = CharacterInfo(
            name = "test_model",
            description = "test_description",
            personality = "test_personality",
            firstMessage = "test_first_mes",
            messageExample = "test_mes_example",
            scenario = "test_scenario",
        ),
        metaData = CharacterMetaData(
            favorite = false,
        ),
    )

    /*@Test
    fun `test instruct build`() {
        val expectedPrompt = ""

        val prompt = PromptBuilder()
            .pattern(settings.promptSettings.pattern)
            .systemPrompt(settings.promptSettings.systemPrompt)
            .character(character)
            .chat(chat)
            .type(settings.promptSettings.type)
            .build()
    }*/
}