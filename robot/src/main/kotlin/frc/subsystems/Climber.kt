package frc.subsystems

import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkMax
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj2.command.SubsystemBase

const val DEFAULT_FREE_CLIMBER_SPEED: Double = 0.4
const val TOP_EXTENSION: Double = -107.6

class Climber(
    val bottomLimit: DigitalInput,
    val motor: CANSparkMax,
) : SubsystemBase() {
    inline val isAtBottom: Boolean get() = bottomLimit.get()
    inline val position: Double get() = motor.encoder.position

    fun init() {
        motor.setIdleMode(CANSparkBase.IdleMode.kCoast)
    }

    fun moveUp(speed: Double = -DEFAULT_FREE_CLIMBER_SPEED) {
        motor.set(speed)
        if(motor.encoder.position < TOP_EXTENSION) {
            stopMotor()
        }
    }

    fun moveDown(speed: Double = DEFAULT_FREE_CLIMBER_SPEED) {
        when {
            isAtBottom -> {
                stopMotor()
            }
            else -> motor.set(speed)
        }
    }

    fun stopMotor() {
        motor.stopMotor()
    }
}
