package frc.subsystems

import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkMax
import edu.wpi.first.wpilibj2.command.SubsystemBase
import kotlin.math.absoluteValue

class Launcher(
    val motor: CANSparkMax,
) : SubsystemBase() {
    inline val speed: Double get() = motor.encoder.velocity

    fun accelerate(to: Double): Boolean {
        motor.pidController.setReference(to, CANSparkBase.ControlType.kVelocity)
        return speed.absoluteValue >= to.absoluteValue
    }

    fun stopMotor() {
        motor.stopMotor()
    }
}


fun launcherSparkMaxMotors(
    lead: CANSparkMax,
    follower0: CANSparkMax,
    inverted: Boolean = true
): CANSparkMax {
    follower0.follow(lead)

    lead.inverted = inverted

    lead.idleMode = CANSparkBase.IdleMode.kCoast
    follower0.idleMode = CANSparkBase.IdleMode.kCoast

    lead.pidController.i = 0.00000033

    return lead
}
