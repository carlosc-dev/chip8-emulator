package chip8

import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.experimental.and

@ExperimentalUnsignedTypes
open class Chip8(var showLogs: Boolean, var soundEnabled: Boolean) {
    var onLog: ((String) -> Unit)? = null
    var onPrintScreen: ((IntArray) -> Unit)? = null

    private val START_PC = 0x200
    val FONT_START_ADDRESS = 0x50
    val VIDEO_WIDTH = 64
    val VIDEO_HEIGHT = 32
    var cpuClockHz: Long = 600
    var PC = START_PC  // program counter
    var memory = UByteArray (4096) // 0xFFF
    var keys = IntArray(2000)
    var currentKey = -1
    var V = IntArray(16) // register
    var i = 0      // index register
    var stack = IntArray(16)
    var SP = 0     // stack pointer
    var ST = 0     // sound timer
    var DT = 0     // delay timer
    var screen = IntArray(VIDEO_WIDTH * VIDEO_HEIGHT)

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var cpuFuture: ScheduledFuture<*>? = null
    private var timerFuture: ScheduledFuture<*>? = null

    init {
        log("Emulator started")
        onPrintScreen?.let { it(screen) }

        loadFonts()
    }

    private fun restart() {
        PC = START_PC
        memory = UByteArray (4096) // 0xFFF
        keys = IntArray(1000)
        currentKey = -1
        V = IntArray(16) // 0xFF
        i = 0
        stack = IntArray(16)
        SP = 0
        ST = 0
        DT = 0
        screen = IntArray(VIDEO_WIDTH * VIDEO_HEIGHT)

        log("Emulator restarted")
        loadFonts()
    }

    fun loadRoom (game: InputStream) {
        restart()

        game.readBytes().forEachIndexed { i, byte ->
            memory[START_PC + i] = (byte and 0xFF.toByte()).toUByte()
        }

        log("Starting game")

        startTimers()
    }

    private fun startTimers() {
        val cpuTick = Runnable {
            Opcode
                .decode(memory[PC], memory[PC + 1])
                .execute(this)
        }

        val timerTick = Runnable {
            // Decrement the delay timer if it's been set
            if (DT > 0) DT--
            // Decrement the sound timer if it's been set
            if (ST > 0) {
                ST--
                if (soundEnabled) {
                    playSound()
                }
            }
        }

        cpuFuture = executor.scheduleAtFixedRate(cpuTick,0, 1_000_000L / cpuClockHz, TimeUnit.MICROSECONDS)
        timerFuture = executor.scheduleAtFixedRate(timerTick, 0 , 16L, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        cpuFuture?.cancel(true)
        timerFuture?.cancel(true)

        log("Emulation paused!")
    }

    fun resume() {
        startTimers()
        log("Emulation resumed!")
    }

    private fun loadFonts () {
        val fontSet = intArrayOf(
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        )

        for(i in 0 until fontSet.size - 1) {
            memory[FONT_START_ADDRESS + i] = fontSet[i].toUByte()
        }

        log("${fontSet.size} fonts loaded in to memory")
    }

    fun setKey(key: Int) {
        currentKey = key
        keys[key] = 1
        log("key press $key")
    }

    fun clearKey(key: Int) {
        currentKey = -1
        keys[key] = 0
        log("key released $key")
    }

    fun log (message: String) {
        if (showLogs) {
            println(message)
            onLog?.let { it("${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))} - $message") }
        }
    }
}

