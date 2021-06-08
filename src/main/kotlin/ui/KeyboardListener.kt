package ui

import java.awt.KeyboardFocusManager

const val PRESSED = 401
const val RELEASED = 402

fun startKeyboardListener(keyMap: Map<Int, Int>, onKeyDown: (Int) -> Unit, onKeyUp: (Int) -> Unit) {
    KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher { keyEvent ->
            keyMap[keyEvent.keyCode]?.let { key ->
                if (keyEvent.id == PRESSED) {
                    onKeyDown(key)
                } else if (keyEvent.id == RELEASED) {
                    onKeyUp(key)
                }
            }
            false
        }
}