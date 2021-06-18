import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.application
import chip8.Chip8
import chip8.chip8KeyMap
import ui.*
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalUnsignedTypes
fun main() {
    System.setProperty("skiko.rendering.laf.global", "true")

    val emptyScreen =  mutableListWithCapacity<Int>(2048)
    val list = mutableListOf("GUI Started")
    val chip8 = Chip8(showLogs = false, soundEnabled = false)

    startKeyboardListener(chip8KeyMap, onKeyDown = chip8::setKey, onKeyUp = chip8::clearKey)

    application {
        var logs by remember { mutableStateOf(list) }
        var screenPixels by remember { mutableStateOf(emptyScreen) }
        var playing by remember { mutableStateOf(false) }
        var paused by remember { mutableStateOf(false) }
        var showDebugger by remember { mutableStateOf(false) }

        val winState = MyWindowState(
            title = "Chip 8 Emulator",
            size = WindowSize(1280.dp, 720.dp),
            openNewWindow = { },
            exit = { },
            close = { }
        )

        chip8.onLog = fun(log: String) {
            logs = (logs + mutableListOf(log)) as MutableList<String>
        }

        chip8.onPrintScreen = fun(pixels: IntArray) {
            screenPixels = pixels.toMutableList()
        }

        chip8.showLogs = showDebugger

        if (showDebugger) {
            Window(title = "Debugger", onCloseRequest = { showDebugger = false}) {
                Debugger(logs)
            }
        }

        EmulatorWindow(
            state = winState,
            playing,
            paused,
            showDebugger,
            chip8.soundEnabled,
            onPause = {
                if (!paused) {
                    paused = true
                    chip8.stop()
                } else {
                    paused = false
                    chip8.resume()
                }
            },
            onPlay = { path ->
                val game = File(path).inputStream()
                chip8.loadRoom(game)
                playing = true
                paused = false
            },
            onStop = {
                chip8.stop()

                playing = false
                paused = false

                screenPixels = emptyScreen
            },
            onMute = {
                chip8.soundEnabled = !chip8.soundEnabled
            },
            onDebugger = {
               showDebugger = !showDebugger
            },
        ) {
            MaterialTheme {
                EmulatorScreen(screenPixels, Modifier.fillMaxWidth().fillMaxHeight())
            }
        }
    }
}

fun <T> mutableListWithCapacity(capacity: Int): MutableList<T> =
    ArrayList(capacity)



