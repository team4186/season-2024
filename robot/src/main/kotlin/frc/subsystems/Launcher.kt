package frc.subsystems

import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkMax
import edu.wpi.first.wpilibj2.command.SubsystemBase
import kotlin.math.absoluteValue

class Launcher(
    val motor: CANSparkMax,
) : SubsystemBase() {
    inline val speed: Double get() = motor.encoder.velocity

    fun speed(to: Double): Boolean {
        motor.set(to)
        return speed.absoluteValue >= to.absoluteValue
    }

    fun stopMotor() {
        motor.stopMotor()
    }
}


fun launcherSparkMaxMotors(
    lead: CANSparkMax,
    follower0: CANSparkMax,
    inverted: Boolean = false
): CANSparkMax {
    follower0.follow(lead)

    follower0.inverted = inverted
    lead.inverted = inverted

    lead.idleMode = CANSparkBase.IdleMode.kCoast
    follower0.idleMode = CANSparkBase.IdleMode.kCoast

    lead.pidController.i = 0.0000033

    return lead
}
