package frc.commands.drive

import edu.wpi.first.wpilibj2.command.Command
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.withSign

class TeleopDrive(
    private val inputTurn: () -> Double,
    private val inputThrottle: () -> Double,
    private val inputYaw: () -> Double,
    private val drive: (forward: Double, side: Double, turn: Double) -> Unit,
    private val stop: () -> Unit,
    private val shouldAttenuate: () -> Boolean = { true },
    private val forward: () -> Double = { -1.0 },
) : Command() {

    override fun execute() {
        val throttle: Double
        val turn: Double
        val strafe: Double
        if (shouldAttenuate()) {
            throttle = attenuated(forward() * inputThrottle())
            turn = attenuated(-0.8 * inputTurn())
            strafe = attenuated(-inputYaw())
        } else {
            throttle = full(forward() * inputThrottle())
            turn = full(-inputTurn())
            strafe = full(-inputYaw())
        }
        drive(-throttle, -strafe, turn)
    }


    override fun end(interrupted: Boolean) {
        stop()
    }

    private fun full(value: Double): Double {
        return value
            .absoluteValue
            .pow(1.6)
            .withSign(value)
    }

    private fun attenuated(value: Double): Double {
        return 0.75 * value.absoluteValue.pow(2).withSign(value)
    }

}