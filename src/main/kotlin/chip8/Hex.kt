package chip8

class Hex(val value: Int) {
    override fun toString(): String {
        return value.toString(16)
    }

    infix fun shr(i: Int): Hex {
        return Hex(value shr i)
    }

    infix fun and(i: Int): Hex {
        return Hex(value and i)
    }

    fun first(): Hex {
        return Hex(value and 0xF000 shr 12)
    }

    fun msb(): Int {
        return value and 0x80
    }

    fun lsb(): Int {
        return value and 0x1
    }

    fun n(): Int {
        return value and 0x000F
    }

    fun nn(): Int {
        return value and 0x00FF
    }

    fun nnn(): Int {
        return value and 0x0FFF
    }
}