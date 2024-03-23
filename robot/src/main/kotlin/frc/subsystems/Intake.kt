package frc.subsystems

import com.revrobotics.CANSparkMax
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj2.command.SubsystemBase

class Intake(
    val intake: CANSparkMax,
    val intakeSlot: DigitalInput,
) : SubsystemBase() {

    inline val hasSomething: Boolean get() = !intakeSlot.get()

    fun launch() {
        intake.set(-0.5)
    }

    fun collect() : Boolean {
        when {
            hasSomething -> intake.stopMotor()
            else -> intake.set(-0.65)
        }
        return hasSomething
    }

    fun eject() {
        intake.set(0.65)
    }

    fun stopMotor() {
        intake.stopMotor()
    }
}