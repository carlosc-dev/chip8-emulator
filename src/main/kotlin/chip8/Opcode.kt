package chip8

@Suppress("ConvertSecondaryConstructorToPrimary")
open class Opcode() {
    var op: Hex = Hex(0)
    var id: Hex = Hex(0)
    var firstNib: Int = 0
    var x: Int = 0
    var y: Int = 0
    var n: Int = 0
    var nn: Int = 0
    var nnn: Int = 0
    var value: Int = 0

    companion object {
        fun decode(mem1: UByte, mem2: UByte): Opcode {
            val op = Hex(mem1.toInt() shl 8 or mem2.toInt())
            return Opcode(op)
        }
    }

    private constructor(op: Hex) : this() {
        this.op = op
        this.value = op.value
        this.id = op.first()
        this.x = ((op and 0x0F00) shr 8).value
        this.y = ((op and 0x00F0) shr 4).value
        this.firstNib = op.msb()
        this.n = op.n()
        this.nn = op.nn()
        this.nnn = op.nnn()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun execute(chip8: Chip8) {
        Instructions.run(chip8, this)
    }


    override fun toString(): String {
        return "$op"
    }

}
