@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package chip8

import kotlin.random.Random


class Instructions {
    companion object {
        fun run(chip8: Chip8, opcode: Opcode) {
            try {
                instructionMap[opcode.value and 0xF000]?.let { run ->
                    chip8.log("${chip8.PC} -> $opcode")
                    chip8.PC+= 2
                    run(chip8, opcode)
                }
            } catch (error: Error) {
                unknownInstruction(chip8, opcode)
            }
        }
    }
}

private var instructionMap = mapOf<Int, (Chip8, Opcode) -> Unit>(
    0x0000 to ::handleZero,
    0x1000 to ::jumpToAddress,
    0x2000 to ::callSubroutine,
    0x3000 to ::skipIfEqualsToN,
    0x4000 to ::skipIfNotEqualsToN,
    0x5000 to ::skipIfXEqualsY,
    0x6000 to ::setVxToN,
    0x7000 to ::addNToVx,
    0x8000 to ::handleEight,
    0x9000 to ::skipIfVxNotEqualsToVy,
    0xA000 to ::setIndex,
    0xB000 to ::jumpToNPlusV0,
    0xC000 to ::setRand,
    0xD000 to ::draw,
    0xE000 to ::handleE,
    0xF000 to ::handleF,
)

private fun handleZero(chip8: Chip8, opcode: Opcode) {
    mapOf(
        0x00E0 to ::clearDisplay,
        0x00EE to ::returnFromSubroutine,
    )[opcode.value and 0x00FF]?.let {
        it(chip8, opcode)
    }
}

private fun handleEight(chip8: Chip8, opcode: Opcode) {
    mapOf(
        0x0000 to ::setVxToVy,
        0x0001 to ::setVxOr,
        0x0002 to ::setVxAnd,
        0x0003 to ::setVxXor,
        0x0004 to ::addYtoX,
        0x0005 to ::xMinusY,
        0x0006 to ::lessSignificant,
        0x0007 to ::setVxToVxMinusVy,
        0x000E to ::mostSignificantBit,
    )[opcode.value and 0x000F]?.let {
        it(chip8, opcode)
    }
}

private fun handleE(chip8: Chip8, opcode: Opcode) {
    mapOf(
        0x009E to ::skipIfKeyNotEquals,
        0x00A1 to ::skipIfKeyEquals,
    )[opcode.value and 0x00FF]?.let {
        it(chip8, opcode)
    }
}

private fun handleF(chip8: Chip8, opcode: Opcode) {
    mapOf(
        0x0007 to ::setXtoDelayTimer,
        0x000A to ::waitForKey,
        0x0015 to ::setDelayTimer,
        0x0018 to ::setSoundTimer,
        0x001E to ::addVxToI,
        0x0029 to ::setIToSpriteAddress,
        0x0033 to ::saveDecimal,
        0x0055 to ::saveRegistersInMemory,
        0x0065 to ::loadRegistersFromMemory,
    )[opcode.value and 0x00FF]?.let {
        it(chip8, opcode)
    }
}

/**
 * 0x00E0 - Clear the display.
 */
private fun clearDisplay(chip8: Chip8, opcode: Opcode) {
    chip8.screen.fill(0)
    chip8.onPrintScreen?.let { it(chip8.screen) }
}

/**
 * 0x00EE - The interpreter sets the program counter to the address at the top of the stack,
 * then subtracts 1 from the stack pointer.
 */
private fun returnFromSubroutine(chip8: Chip8, opcode: Opcode) {
    chip8.PC = chip8.stack[chip8.SP]
    chip8.SP--
}

/**
 * 0x1NNN - The interpreter sets the program counter to nnn.
 */
private fun jumpToAddress(chip8: Chip8, opcode: Opcode) {
    chip8.PC = opcode.nnn
}

/**
 * 0x2nnn - The interpreter increments the stack pointer,
 * then puts the current PC on the top of the stack.
 * The PC is then set to nnn.
 */
private fun callSubroutine(chip8: Chip8, opcode: Opcode) {
    chip8.SP++
    chip8.stack[chip8.SP] = chip8.PC
    chip8.PC = opcode.nnn
}

/**
 * 0x3xkk - The interpreter compares register Vx to kk,
 * and if they are equal, increments the program counter by 2.
 */
private fun skipIfEqualsToN(chip8: Chip8, opcode: Opcode) {
    if (chip8.V[opcode.x] == opcode.nn) {
        chip8.PC += 2
    }
}

/**
 * 0x4xkk - The interpreter compares register Vx to kk,
 * and if they are not equal, increments the program counter by 2.
 */
private fun skipIfNotEqualsToN(chip8: Chip8, opcode: Opcode) {
    if (chip8.V[opcode.x] != opcode.nn) {
        chip8.PC += 2
    }
}

/**
 * 0x5xy0 - The interpreter compares register Vx to register Vy,
 * and if they are equal, increments the program counter by 2.
 */
private fun skipIfXEqualsY(chip8: Chip8, opcode: Opcode) {
    if (chip8.V[opcode.x] == chip8.V[opcode.y]) {
        chip8.PC += 2
    }
}

/**
 * 0x6xkk - The interpreter puts the value kk into register Vx.
 */
private fun setVxToN(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = opcode.nn
}

/**
 * 0x7xkk - Adds the value kk to the value of register Vx,
 * then stores the result in Vx.
 */
private fun addNToVx(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] += opcode.nn

    if (chip8.V[opcode.x] > 255) {
        chip8.V[opcode.x] -= 256
    }
}

/**
 * 0x8xy0 - Stores the value of register Vy in register Vx.
 */
private fun setVxToVy(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = chip8.V[opcode.y]
}

/**
 * 0x8xy1 - Vx = Vx OR Vy.
 */
private fun setVxOr(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = chip8.V[opcode.x] or chip8.V[opcode.y]
}

/**
 * 0x8xy2 - Vx = Vx AND Vy.
 */
private fun setVxAnd(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = chip8.V[opcode.x] and chip8.V[opcode.y]
}

/**
 * 0x8xy3 - Vx = Vx XOR Vy.
 */
private fun setVxXor(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = chip8.V[opcode.x] xor chip8.V[opcode.y]
}

/**
 * 0x8xy4 - Vx = Vx + Vy, set VF = carry.
 */
private fun addYtoX(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = chip8.V[opcode.x] + chip8.V[opcode.y]

    if (chip8.V[opcode.x] > 0xFF) {
        chip8.V[0xF] = 1
    } else {
        chip8.V[0xF] = 0
    }

    chip8.V[opcode.x] = chip8.V[opcode.x] and 0xFF // ?????

    if (chip8.V[opcode.x] > 255) {
        chip8.V[opcode.x] -= 256
    }
}

/**
 * 0x8xy5 - Vx = Vx - Vy, SF = not borrow
 */
private fun xMinusY(chip8: Chip8, opcode: Opcode) {

    if (chip8.V[opcode.x] > chip8.V[opcode.y]) {
        chip8.V[0xF] = 1
    } else {
        chip8.V[0xF] = 0
    }

    chip8.V[opcode.x] = chip8.V[opcode.x] - chip8.V[opcode.y]

    if (chip8.V[opcode.x] < 0) {
        chip8.V[opcode.x] += 256
    }
}

/**
 * 0x8xy6 - Vx = Vx SHR 1.
 */
private fun lessSignificant(chip8: Chip8, opcode: Opcode) {
    chip8.V[0xF] = chip8.V[opcode.x] and 0x1
    chip8.V[opcode.x] = chip8.V[opcode.x] shr 1

    if (chip8.V[opcode.x] > 255) {
        chip8.V[opcode.x] -= 256
    }
}

/**
 * 0x8xy7 - Vx = Vy - Vx, set VF = NOT borrow.
 */
private fun setVxToVxMinusVy(chip8: Chip8, opcode: Opcode) {
    if (chip8.V[opcode.y] > chip8.V[opcode.x]) {
        chip8.V[0xF] = 1
    } else {
        chip8.V[0xF] = 0
    }

    chip8.V[opcode.x] = (chip8.V[opcode.y] - chip8.V[opcode.x])

    if (chip8.V[opcode.x] < 0) {
        chip8.V[opcode.x] += 256
    }
}

/**
 * 0x8xyE - Vx = Vx SHL 1.
 */
private fun mostSignificantBit(chip8: Chip8, opcode: Opcode) {
    chip8.V[0xF] = (chip8.V[opcode.x] and 0x80) shr 7
    chip8.V[opcode.x] = (chip8.V[opcode.x] shl 1)
}

/**
 * 0x9xy0 - Skip next instruction if Vx != Vy.
 */
private fun skipIfVxNotEqualsToVy(chip8: Chip8, opcode: Opcode) {
    if (chip8.V[opcode.x] != chip8.V[opcode.y]) {
        chip8.PC += 2
    }
}

/**
 * 0xAnnn - The value of register I is set to nnn.
 */
private fun setIndex(chip8: Chip8, opcode: Opcode) {
    chip8.i = opcode.nnn
}

/**
 * 0xBnnn - Jump to location nnn + V0.
 */
private fun jumpToNPlusV0(chip8: Chip8, opcode: Opcode) {
    chip8.PC = opcode.nnn + chip8.V[0x0]
}

/**
 * 0xCxkk - Random number form 0 to 255 and kk to Vx
 */
private fun setRand(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = Random.nextInt(255) and opcode.nn
}

/**
 * 0xDxyn - Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
 */
private fun draw(chip8: Chip8, opcode: Opcode) {
    if (chip8.screen == null) {
        chip8.log("SCREEN NULL")
        return
    }

    val height = 0 until opcode.n
    val width = 0  until 8

    chip8.V[0xF] = 0

    height.forEach { byte ->
        var spriteByte = chip8.memory[chip8.i + byte].toInt()
        var y = (chip8.V[opcode.y] + byte) % chip8.VIDEO_HEIGHT

        width.forEach { bit ->
            if (spriteByte and 0x80 != 0) {
                var x = (chip8.V[opcode.x] + bit) % chip8.VIDEO_WIDTH

                val screenPos = x + (y * chip8.VIDEO_WIDTH)

                if (chip8.screen!![screenPos] != 0) {
                    chip8.V[0xF] = 1
                }
                // draw screen
                chip8.screen!![screenPos] = chip8.screen!![screenPos] xor 0xF
            }
            spriteByte = spriteByte shl 1

        }

    }
    chip8.onPrintScreen?.let { it(chip8.screen) }

}

/**
 * 0xEx9e - Skip next instruction if key with the value of Vx is pressed.
 */
private fun skipIfKeyNotEquals(chip8: Chip8, opcode: Opcode) {
    if (chip8.keys[chip8.V[opcode.x]] == 1) {
        chip8.PC += 2
    }
}

/**
 * 0xExA1 - Skip next instruction if key with the value of Vx is not pressed.
 */
private fun skipIfKeyEquals(chip8: Chip8, opcode: Opcode) {
    if (chip8.keys[chip8.V[opcode.x]] == 0) {
        chip8.PC += 2
    }
}

/**
 * 0xFx07 - The value of DT is placed into Vx.
 */
private fun setXtoDelayTimer(chip8: Chip8, opcode: Opcode) {
    chip8.V[opcode.x] = chip8.DT
}

/**
 * 0xFx0A - wait for key press, store key in VX
 */
private fun waitForKey(chip8: Chip8, opcode: Opcode) {
    if (chip8.currentKey == 0) {
        chip8.PC -= 2
    } else {
        chip8.V[opcode.x] = chip8.currentKey
    }
}

/**
 * 0xFx15 - Delay Timer = Vx
 */
private fun setDelayTimer(chip8: Chip8, opcode: Opcode) {
    chip8.DT = chip8.V[opcode.x]
}

/**
 * 0xFx18 - Sound timer = Vx
 */
private fun setSoundTimer(chip8: Chip8, opcode: Opcode) {
    chip8.ST = chip8.V[opcode.x]
}

/**
 * 0xFx1E - Set I = I + Vx.
 */
private fun addVxToI(chip8: Chip8, opcode: Opcode) {
    chip8.i += chip8.V[opcode.x]
}

/**
 * 0xFx29 - I = location of sprite for digit Vx
 */
private fun setIToSpriteAddress(chip8: Chip8, opcode: Opcode) {
    chip8.i = chip8.FONT_START_ADDRESS + (5 * chip8.V[opcode.x])
}

/**
 * 0xFx33 - The interpreter takes the decimal value of Vx,
 * and places the hundreds digit in memory at location in I,
 * the tens digit at location I+1, and the ones digit at location I+2.
 */
private fun saveDecimal(chip8: Chip8, opcode: Opcode) {
    var number = chip8.V[opcode.x]

    for(index in 2 downTo 0) {
        chip8.memory[chip8.i + index] = (number.rem(10).toUByte())
        number /= 10
    }
}

/**
 * 0xFx55 - Store registers V0 through Vx in memory starting at location I.
 */
private fun saveRegistersInMemory(chip8: Chip8, opcode: Opcode) {
    for(r in 0x0..opcode.x) {
        chip8.memory[r + chip8.i] = chip8.V[r].toUByte()
    }
}

/**
 * 0xFx65 - Read registers V0 through Vx from memory starting at location I.
 */
private fun loadRegistersFromMemory(chip8: Chip8, opcode: Opcode) {
    for(r in 0x0..opcode.x) {
        chip8.V[r] = chip8.memory[r + chip8.i].toInt()
    }
}

private fun unknownInstruction(chip8: Chip8, opcode: Opcode) {
    println("the fuck is this??? $opcode")
}

private infix fun UByte.shr(i: Int): UByte {
    return (this.toInt() shr i).toUByte()
}

private infix fun UByte.shl(i: Int): UByte {
    return (this.toInt() shl i).toUByte()
}
