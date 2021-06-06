package chip8

import androidx.compose.ui.input.key.Key

var chip8KeyMap = mapOf<Key, Int>(
        Key.Button1 to 0x1,  // 1
        Key.Button2 to 0x2,  // 2
        Key.Button3 to 0x3,  // 3
        Key.Button4 to 0x4,  // 4
        Key.Q to 0x5,  // Q
        Key.W to 0x6,  // W
        Key.E to 0x7,  // E
        Key.R to 0x8,  // R
        Key.A to 0x9,  // A
        Key.S to 0xA,  // S
        Key.D to 0xB,  // D
        Key.F to 0xC,  // F
        Key.Z to 0xD,  // Z
        Key.X to 0xE,  // X
        Key.C to 0xF,  // C
        Key.V to 0x10,  // V
    )
