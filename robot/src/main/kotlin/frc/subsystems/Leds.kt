package frc.subsystems

import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.AddressableLEDBuffer
import edu.wpi.first.wpilibj2.command.SubsystemBase

//TODO Reduce brightness of LEDs

class Leds(
    port: Int,
    private val length: Int = 32
) : SubsystemBase() {
    private val ledBuffer = AddressableLEDBuffer(length)
    private val led = AddressableLED(port).apply {
        setLength(ledBuffer.length)
        setData(ledBuffer)
    }

    fun init() {
        led.start()
    }

    fun lightUp(color: Color) {
        repeat(length) {
            ledBuffer.setRGB(it, color.r, color.g, color.b)
        }
        led.setData(ledBuffer)
    }

    enum class Color(val r: Int, val g: Int, val b: Int) {
        Red(255, 0, 0),
        Green(0, 255, 0)
    }
}