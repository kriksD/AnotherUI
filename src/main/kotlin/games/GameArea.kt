package games

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import biggerPadding
import colorText
import composableFunctions.CheckManager
import composableFunctions.CheckValue
import games.five.FiveGame
import games.land.LandGame
import games.line.LineGame
import games.pair.PairGame
import hugeText
import padding
import tinyIconSize

@Composable
fun GameArea(
    modifier: Modifier = Modifier,
) {
    var isSettingsOpen by remember { mutableStateOf(false) }
    val game = remember { CheckManager(
        CheckValue(0, true),
        CheckValue(1, false),
        CheckValue(2, false),
        CheckValue(3, false),
        CheckValue(4, false),
    ) }

    Column(
        modifier = modifier
            .padding(start = biggerPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Text(
                text = "▶",
                color = colorText,
                fontSize = hugeText,
                modifier = Modifier.clickable {
                    val checked = game.getChecked() ?: return@clickable

                    if (checked < 4) game.check(checked + 1) else game.check(0)
                }
            )

            Icon(
                Icons.Default.Settings,
                "game settings",
                tint = colorText,
                modifier = Modifier
                    .size(tinyIconSize)
                    .clickable { isSettingsOpen = !isSettingsOpen }
            )
        }

        Crossfade(
            game.getChecked(),
        ) { checked ->
            when (checked) {
                1 -> LineGame(isSettingsOpen = isSettingsOpen)
                2 -> PairGame(isSettingsOpen = isSettingsOpen)
                3 -> FiveGame(isSettingsOpen = isSettingsOpen)
                4 -> LandGame(isSettingsOpen = isSettingsOpen)
            }
        }
    }
}