package ui

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.*
import kotlin.system.exitProcess

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmulatorWindow(
    state: MyWindowState,
    playing: Boolean,
    paused: Boolean,
    showDebugger: Boolean,
    soundEnabled: Boolean,
    onStop: () -> Unit,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onMute: () -> Unit,
    onDebugger: () -> Unit,
    content: @Composable WindowScope.() -> Unit
) = Window(title = state.title, state = rememberWindowState(size = state.size)) {
    MenuBar {
        Menu("Emulator") {
            if (playing) {
                Item(if (paused) "Resume Game" else "Pause Game", onClick = onPause)
                Item("Stop Game", onClick = onStop)
            } else {
                Menu("Play Game") {
                    gameRooms.forEach { (k, v) ->
                        Item(k, onClick = {
                            onPlay(v)
                        })
                    }
                }
            }
            Separator()
            Item("Exit", onClick = {
                exitProcess(-1)
            })
        }
        Menu("Options") {
            Item( if (showDebugger) "Hide Debugger" else "Show Debugger", onClick = {
                onDebugger()
            })
            Item( if (soundEnabled) "Mute" else "Unmute", onClick = {
                onMute()
            })
        }
    }

    content()
}


class MyWindowState(
    val title: String,
    val size: WindowSize,
    val openNewWindow: () -> Unit,
    val exit: () -> Unit,
    private val close: (MyWindowState) -> Unit
) {
    fun close() = close(this)
}
