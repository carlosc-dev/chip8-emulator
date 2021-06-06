
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@ExperimentalUnsignedTypes
class Chip8(val showLogs: Boolean) {
    var onLog: ((String) -> Unit)? = null
    var onPrintScreen: ((IntArray) -> Unit)? = null

    private var paused = false
    private var updateScreen = false
    private val START_PC = 0x200
    private val FONT_START_ADDRESS = 0x50
    val VIDEO_WIDTH = 64
    val VIDEO_HEIGHT = 32

    private var PC = START_PC  // program counter
    private var memory = UByteArray (4096) // 0xFFF
    private var keys = IntArray(2000)
    private var currentKey = 0
    private var register = IntArray(16) // 0xFF
    private var i = 0     // index register
    private var stack = IntArray(16)
    private var SP = 0   // stack pointer
    private var ST = 0     // sound timer
    private var DT = 0     // delay timer
    private var screen = IntArray(VIDEO_WIDTH * VIDEO_HEIGHT)

    init {
        log("Emulator started")
        onPrintScreen?.let { it(screen) }

        loadFonts()
    }


    private fun restart() {
        paused = false
        updateScreen = false
        PC = START_PC
        memory = UByteArray (4096) // 0xFFF
        keys = IntArray(1000)
        currentKey = 0
        register = IntArray(16) // 0xFF
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
            memory[START_PC + i] = byte.toUByte()
        }

        log("Starting game")
        paused = false
        cycle()
    }

    fun stop() {
        paused = true
        log("Emulation paused!")
    }

    fun resume() {
        paused = false
        cycle()
        log("Emulation resumed!")
    }

    private fun cycle() {
        var lastCycle = 0L

        while (!paused) {
            val time = System.currentTimeMillis()

            if (time > lastCycle + 1 && !paused) {
                lastCycle = time

                emulateChipOpcode()

                if (updateScreen) {
                    onPrintScreen?.let{ it(screen) }
                    updateScreen = false
                }

                // Decrement the delay timer if it's been set
                if (DT > 0)
                {
                    --DT
                }
                // Decrement the sound timer if it's been set
                if (ST > 0)
                {
                    --ST
                }

            }
        }
    }

    private fun getOpcode (): Int {
        return memory[PC].toInt() shl 8 or memory[PC + 1].toInt()
    }

    private fun getRandByte (): Int {
        return Random.nextInt(255)
    }

    private fun emulateChipOpcode () {
        val opcode = getOpcode()
        val c1 = memory[PC].toString(16)
        val c2 = memory[PC + 1].toString(16)
        val vx = (opcode and 0x0F00) shr 8
        val vy = (opcode and 0x00F0) shr 4
        val lPC = PC
        PC +=2

        when(opcode and 0xF000) {
            0x0000 -> when(opcode and 0x00FF) {
                0x00E0 ->  {
                    log("$lPC [$c1 $c2] Clear the screen")
                    for (i in screen) {
                        screen[i] = 0x0
                    }
                    updateScreen = true
                }
                0x00EE -> {
                    log("$lPC [$c1 $c2] Return from a subroutine")

                    PC = stack[SP]
                    SP--
                }
                else -> log("$lPC [$c1 $c2] unknown operation 0  ${opcode.toString(16)}")
            }
            0x1000 -> {
                log("$lPC [$c1 $c2] Jump to address ${opcode and 0x0FFF}")
                PC = opcode and 0x0FFF
            }
            0x2000 -> {
                log("$lPC [$c1 $c2] Call subRoutine at ${(opcode and 0x0FFF)}")
                SP++
                stack[SP] = PC

                PC = opcode and 0x0FFF
            }
            0x3000 -> {
                log("$lPC [$c1 $c2] Skip next instruction if VX == NN // V[$vx] ${register[vx]} == ${(opcode and 0x00FF)}")
                if (register[vx] == opcode and 0x00FF) {
                    PC+= 2
                }
            }
            0x4000 -> {
                log("$lPC [$c1 $c2] Skip next instruction if VX != NN // V[$vx] ${register[vx]} != ${(opcode and 0x00FF)}")
                if (register[vx] != opcode and 0x00FF) {
                    PC += 2
                }
            }
            0x5000 -> {
                log("$lPC [$c1 $c2] Skip next instruction if VX == VY // V[$vx] ${register[vx]} == V[$vy] ${register[vy]}")
                if (register[vx] == register[vy]) {
                    PC += 2
                }
            }
            0x6000 -> {
                log("$lPC [$c1 $c2] Set VX to NN // V$vx = ${(opcode and 0x00FF)}")
                register[vx] = opcode and 0x00FF
            }
            0x7000 -> {
                log("$lPC [$c1 $c2] Add NN to VX // V[$vx] = ${register[vx]} + ${(opcode and 0x00FF)}")
                register[vx] += opcode and 0x00FF

                if (register[vx] > 255) {
                    register[vx] -= 256
                }
            }
            0x8000 -> when (opcode and 0x000F) {
                0x0000 -> {
                    log("$lPC [$c1 $c2] Set VX to the value of VY // ${register[vx]} = ${register[vy]}")
                    register[vx] = register[vy]
                }
                0x0001 -> {
                    log("$lPC [$c1 $c2] Set VX to VX OR VY")
                    register[vx] = register[vx] or register[vy]
                }
                0x0002 -> {
                    log("$lPC [$c1 $c2] Set VX to VX AND VY")
                    register[vx] = register[vx] and register[vy]
                }
                0x0003 -> {
                    log("$lPC [$c1 $c2] Set VX to VX XOR VY")
                    register[vx] = register[vx] xor register[vy]
                }
                0x0004 -> {
                    log("$lPC [$c1 $c2] VX + VY. carry ? VF=1 else VF=0")
                    register[vx] = register[vx] + register[vy]

                    if (register[vx] > 0xFF) {
                        register[0xF] = 1
                    } else {
                        register[0xF] = 0
                    }

                    register[vx] = register[vx] and 0xFF
                }
                0x0005 -> {
                    log("$lPC [$c1 $c2] Vx = Vx - Vy borrow ? VF=0 else VF=1")

                    if (register[vx] > register[vy]) {
                        register[0xF] = 1
                    } else {
                        register[0xF] = 0
                    }

                    register[vx] = register[vx] - register[vy]
                }
                0x0006 -> {
                    log("$lPC [$c1 $c2] less significant bit of VX to VF then VX >> 1 ")

                    register[0xF] = register[vx] and 0x1
                    register[vx] = register[vx] shr 1
                }
                0x0007 -> {
                    log("$lPC [$c1 $c2] VX = VY - VX borrow? VF=0 else VF=1")

                    if (register[vy] > register[vx]) {
                        register[0xF] = 1
                    } else {
                        register[0xF] = 0
                    }

                    register[vx] = register[vy] - register[vx]
                }
                0x000E -> {
                    log("$lPC [$c1 $c2] most significant bit of VX to VF then VX << 1")

                    register[0xF] = (register[vx] and 0x80) shr 7
                    register[vx] = register[vx] shl 1

                    if (register[vx] > 255) {
                        register[vx] -= 256
                    }
                }
                else -> log("$lPC [$c1 $c2] Unknown operation 8")
            }
            0x9000 -> {
                log("$lPC [$c1 $c2] Skip next instruction if VX !== VY // $vx !== $vy")

                if (register[vx] != register[vy]) {
                    PC+= 2
                }
            }
            0xA000 -> {
                log("$lPC [$c1 $c2] set i to address ${opcode and 0x0FFF}")
                i = opcode and 0x0FFF
            }
            0xB000 -> {
                log("$lPC [$c1 $c2] Jump to address ${opcode and 0x0FFF} + V0")
                PC = opcode and 0x0FFF + register[0x0]
            }
            0xC000 -> {
                log("$lPC [$c1 $c2] VX = rand() and NN")

                register[vx] = getRandByte() and opcode and 0x00FF
            }
            0xD000 -> {
                log("$lPC [$c1 $c2] Draw pixel at (V[$vx], V[$vy]) (${register[vx]}, ${register[vy]}) width 8, height = ${opcode and 0x000F}")

                val height = opcode and 0x000F
                val width = 8

                val xPos = register[vx]
                val yPos = register[vy]

                register[0xF] = 0

                for (row in 0 until height) {
                    var spriteByte = memory[i + row].toInt()

                    for(col in 0 until width ) {

                        if (spriteByte and 0x80 != 0) {
                            var x = xPos + col
                            var y = yPos + row

                            // prevent overflow
                            if(x >= 64) x -= 64
                            if(x < 0) x += 64
                            if(y >= 32) y -= 32
                            if(y < 0)  y += 32

                            var screenPixel = (y * VIDEO_WIDTH) + x

                            // if pixels are deleted VF = 1 else VF = 0
                            if (screen[screenPixel] != 0) {
                                register[0xF] = 1
                            }

                            // display sprite on screen
                            screen[screenPixel] = screen[screenPixel] xor 0xF

                        }
                         spriteByte = spriteByte shl 1
                    }
                }

                updateScreen = true
            }
            0xE000 -> when(opcode and 0x00FF) {
                0x009E -> {
                    log("$lPC [$c1 $c2] Skip next if key in VX is pressed")
                    if (keys[register[vx]] == 1) {
                        PC += 2
                    }
                }
                0x00A1 -> {
                    log("$lPC [$c1 $c2] Skip next if key [${register[vx]}] in VX [${vx}] not pressed")

                    if (keys[register[vx]] == 0) {
                        PC += 2
                    }
                }
                else -> log("$lPC [$c1 $c2] unknown operation E")
            }
            0xF000 -> when(opcode and 0x00FF) {
                0x0007 -> {
                    log("$lPC [$c1 $c2] VX = DT (delay timer)")
                    register[vx] = DT
                }
                0x000A -> {
                    log("$lPC [$c1 $c2] wait key press and store it in VX (halt until key)")


                    if (currentKey == 0) {
                        // if no key is found repeat step
                        PC -= 2
                    } else {
                        register[vx] = currentKey
                    }
                }
                0x0015 -> {
                    log("$lPC [$c1 $c2] DT = VX")
                    DT = register[vx]
                }
                0x0018 -> {
                    log("$lPC [$c1 $c2] ST = VX")
                    ST = register[vx]
                }
                0x001E -> {
                    log("$lPC [$c1 $c2] I = I + V[$vx] /// $i = $i + ${register[vx]}")
                    i += register[vx]
                }
                0x0029 -> {
                    log("$lPC [$c1 $c2] set I to location of sprite for digit VX")

                    i = FONT_START_ADDRESS + (5 * register[vx])
                }
                0x0033 -> {
                    log("$lPC [$c1 $c2] Store binary coded decimal of VX")
                    var number = register[vx]

                    for(index in 2 downTo 0) {
                        memory[i + index] = (number.rem(10)).toUByte()
                        number /= 10
                    }
                }
                0x0055 -> {
                    log("$lPC [$c1 $c2] Store v0 to VX in memory at address i")
                    for(r in 0x0..vx) {
                        memory[r + i] = register[r].toUByte()
                    }
                }
                0x0065 -> {
                    log("$lPC [$c1 $c2] Fill V0 to VX from I [$i]")

                    for(r in 0x0..vx) {
                        register[r] = memory[r + i].toInt()
                    }
                }
                else -> log("$lPC [$c1 $c2] unknown operation F")
            }
            else -> log("$lPC [$c1 $c2] not handled yet")
        }
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

    fun setKeyPress(key: Int) {
        currentKey = key
        keys[key] = 1
    }

    fun clearKey(key: Int) {
        keys[key] = 0
    }

    private fun log (message: String) {
        if (showLogs) {
            println(message)
        }

        onLog?.let { it("${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))} - $message") }
    }

}

