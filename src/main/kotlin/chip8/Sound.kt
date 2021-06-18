package chip8

import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl

@Synchronized
fun playSound() {
    Thread {
        try {
            val clip = AudioSystem.getClip()
            val stream = File("src/main/resources/sound.wav")
            val inputStream: AudioInputStream = AudioSystem.getAudioInputStream(stream)
              //  playSound.class.java.getResourceAsStream("/path/to/sounds/$url")
            clip.open(inputStream)
            val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            gainControl.value = -50.0f // Reduce volume by 10 decibels.

            clip.start()
        } catch (e: Exception) {
            System.err.println("sound -> ${e.message}")
        }
    }.start()
}