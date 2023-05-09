import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import character.ACharacter
import character.chat.legacyChat.LegacyChat
import androidx.compose.ui.graphics.SolidColor
import character.chat.Chat
import character.chat.aChat.AChat
import composableFunctions.CloseLine
import composableFunctions.UsualDivider

@Composable
fun ChatManagementWindow(
    character: ACharacter,
    chats: List<Chat>,
    onChatSelected: (Chat) -> Unit = {},
    onDelete: (Chat) -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(SolidColor(colorBackgroundLighter), RoundedCornerShape(corners), transparency)
            .border(border, colorBorder, RoundedCornerShape(corners))
            .padding(biggerPadding)
    ) {
        CloseLine(
            modifier = Modifier.fillMaxWidth(),
            name = "Chats with ${character.jsonData.name}",
            description = "close chat management",
            onClose = onClose
        )
        UsualDivider()
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.spacedBy(padding)
        ) {
            items(chats) { chat ->
                ChatView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onChatSelected.invoke(chat)
                        },
                    chat = chat,
                    selected = character.jsonData.chat == chat.fileName,
                    onDelete = {
                        onDelete.invoke(chat)
                    },
                )
            }
        }
    }
}

@Composable
fun ChatView(
    chat: Chat,
    selected: Boolean = false,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(colorBackgroundSecond, RoundedCornerShape(corners))
            .border(smallBorder, colorBorder, RoundedCornerShape(corners))
            .padding(padding),
    ) {
        Column {
            if (selected) {
                Text("✓selected", color = colorTextSuccess, fontSize = normalText)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(biggerPadding)
            ) {
                Text("${chat.fileName} ${if (chat is AChat) "(AChat)" else "(legacy)"}", color = colorText, fontSize = bigText)
                Text(chat.chatInfo.create_date.toTimeString(), color = colorTextSecond, fontSize = normalText)
            }

            if (chat.messages.size >= 1) {
                Text(
                    chat.messages.last().toString(),
                    color = colorText,
                    fontSize = normalText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (chat.messages.size >= 2) {
                Text(
                    chat.messages[chat.messages.lastIndex - 1].toString(),
                    color = colorText,
                    fontSize = normalText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (chat.messages.size > 2) {
                Text(
                    "And ${chat.messages.size - 2} more messages...",
                    color = colorTextSecond,
                    fontSize = normalText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Icon(
            Icons.Default.Delete,
            "delete this chat",
            tint = colorText,
            modifier = Modifier
                .width(iconSize)
                .aspectRatio(1F)
                .clickable {
                    onDelete.invoke()
                }
                .align(Alignment.TopEnd)
        )
    }
}