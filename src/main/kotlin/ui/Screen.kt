package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.floor

val lightGreen = Color(red = 202, green = 220, blue = 159 )
val darkGreen =  Color(red = 15, green = 56, blue = 15 )

val PixelOn = Color.White
val PixelOff = Color.Black


fun getColor(p: Int): Color {
    if (p == 0) return PixelOff
    return PixelOn
}

fun getOffset(i: Int, sizeX: Float, sizeY: Float, canvasSize: Size): Offset {
    val x = i % 64
    val y = floor((i / 64).toDouble())
    return Offset((x * sizeX), (y * sizeY).toFloat())
}

@Composable
fun EmulatorScreen(screen: MutableList<Int>, modifier: Modifier) {
    Card(modifier) {
        Canvas(modifier = Modifier.fillMaxSize().background(PixelOff)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val sizeX = canvasWidth / 64
            val sizeY = canvasHeight / 32
            screen.forEachIndexed { index, pixel ->
                drawRect(
                    getColor(pixel),
                    topLeft = getOffset(index, sizeX, sizeY, size), size = Size(sizeX + 1, sizeY + 1)
                )
            }

        }

    }
}
