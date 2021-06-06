
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import chip8.chip8KeyMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.Debugger
import ui.EmulatorScreen
import ui.EmulatorWindow
import ui.MyWindowState
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalUnsignedTypes
fun main() {
    System.setProperty("skiko.rendering.laf.global", "true")

    val chip8 = Chip8(false)
    val list = mutableListOf("GUI Started")
    val emptyScreen =  mutableListWithCapacity<Int>(2048)


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
            screenPixels = pixels.map { i -> i } as MutableList<Int>
        }

        if (showDebugger) {
            Window (title = "Debugger") {
                Debugger(logs)
            }
        }

        EmulatorWindow(
            state = winState,
            playing,
            paused,
            showDebugger,
            onPause = {
                if (!paused) {
                    paused = true
                    chip8.stop()
                }
            },
            onPlay = { path ->
                val game = File(path).inputStream()
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    chip8.loadRoom(game)
                }
                playing = true
                paused = false
            },
            onStop = {
                chip8.stop()
                chip8.restart()
                playing = false
                paused = false
                screenPixels = emptyScreen
            },
            onDebugger = {
               showDebugger = true
            },
        ) {
            MaterialTheme {
                EmulatorScreen(screenPixels, Modifier.fillMaxWidth().fillMaxHeight().onKeyEvent {
                    println("yoooo")
                    if (chip8KeyMap[it.key] != null) {
                        println("yoooo")
                    }

                    true
                })
            }
        }
    }
}

fun <T> mutableListWithCapacity(capacity: Int): MutableList<T> =
    ArrayList(capacity)






