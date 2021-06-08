package chip8

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode

var chip8KeyMap = mapOf(
        Key.One.nativeKeyCode to 0x1,  // 1
        Key.Two.nativeKeyCode to 0x2,  // 2
        Key.Three.nativeKeyCode to 0x3,  // 3
        Key.Four.nativeKeyCode to 0x4,  // 4
        Key.Q.nativeKeyCode to 0x5,  // Q
        Key.W.nativeKeyCode to 0x6,  // W
        Key.E.nativeKeyCode to 0x7,  // E
        Key.R.nativeKeyCode to 0x8,  // R
        Key.A.nativeKeyCode to 0x9,  // A
        Key.S.nativeKeyCode to 0xA,  // S
        Key.D.nativeKeyCode to 0xB,  // D
        Key.F.nativeKeyCode to 0xC,  // F
        Key.Z.nativeKeyCode to 0xD,  // Z
        Key.X.nativeKeyCode to 0xE,  // X
        Key.C.nativeKeyCode to 0xF,  // C
        Key.V.nativeKeyCode to 0x10,  // V
    )
