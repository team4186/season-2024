package frc.subsystems

import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkMax
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.Encoder
import edu.wpi.first.wpilibj2.command.SubsystemBase
import kotlin.math.absoluteValue

const val DEFAULT_FREE_MOVE_SPEED: Double = 0.35
const val DEFAULT_SETPOINT_THRESHOLD: Double = 1e-5

class Arm(
    val bottomLimit: DigitalInput,
    val topLimit: DigitalInput,
    val encoder: Encoder,
    val motor: CANSparkMax,
    val pid: PIDController,
) : SubsystemBase() {
    inline val isAtTop: Boolean get() = !topLimit.get()
    inline val isAtBottom: Boolean get() = !bottomLimit.get()

    inline val position: Double get() = encoder.distance

    var needReset: Boolean = true
        private set

    fun reset() {
        pid.reset()
        encoder.reset()
        encoder.distancePerPulse = 1/5.670
        needReset = true
    }

    fun move(to: Double, threshold: Double = DEFAULT_SETPOINT_THRESHOLD): Boolean {
        val speed = pid
            .calculate(position, to)
            .coerceIn(-DEFAULT_FREE_MOVE_SPEED, DEFAULT_FREE_MOVE_SPEED)

        when {
            speed > 0 -> moveUp(speed)
            speed < 0 -> moveDown(speed)
            else -> motor.stopMotor()
        }

        return (to - position).absoluteValue <= threshold
    }

    fun moveUp(speed: Double = DEFAULT_FREE_MOVE_SPEED) {
        when {
            isAtTop -> stopMotor()

            else -> motor.set(speed)
        }
    }

    fun moveDown(speed: Double = -DEFAULT_FREE_MOVE_SPEED) {
        when {
            isAtBottom -> {
                stopMotor()
                motor.encoder.setPosition(0.0)
                encoder.reset()
                needReset = false
            }

            else -> motor.set(speed)
        }
    }

    fun stopMotor() {
        motor.stopMotor()
        pid.reset()
    }
}

fun armSparkMaxMotors(
    lead: CANSparkMax,
    follower0: CANSparkMax,
    inverted: Boolean = false
): CANSparkMax {
    follower0.follow(lead, !inverted)

    lead.inverted = inverted

    lead.idleMode = CANSparkBase.IdleMode.kBrake
    follower0.idleMode = CANSparkBase.IdleMode.kBrake

    lead.encoder.setPositionConversionFactor(1.0)
    follower0.encoder.setPositionConversionFactor(1.0)

    return lead
}
