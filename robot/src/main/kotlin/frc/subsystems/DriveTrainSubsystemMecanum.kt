package frc.subsystems

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.wpilibj.MotorSafety
import edu.wpi.first.wpilibj.drive.MecanumDrive
import edu.wpi.first.wpilibj2.command.SubsystemBase

class DriveTrainSubsystemMecanum(
    val frontLeft: WPI_TalonSRX = WPI_TalonSRX(9).apply{inverted = true},
    val rearLeft: WPI_TalonSRX = WPI_TalonSRX(8).apply{inverted = true},
    val frontRight: WPI_TalonSRX = WPI_TalonSRX(5),
    val rearRight: WPI_TalonSRX = WPI_TalonSRX(2),
) : SubsystemBase() {
    private val drive = MecanumDrive(frontLeft, rearLeft, frontRight, rearRight)
    private var currentForward = 0.0

    private val motorSafety: MotorSafety = object : MotorSafety() {
        override fun stopMotor() {
            sequenceOf(
                frontLeft,
                rearLeft,
                frontRight,
                rearRight,
            ).forEach { it.stopMotor() }
        }

        override fun getDescription(): String {
            return "EncoderDrive"
        }
    }

    override fun periodic() {
    }

    fun initialize() {
        drive.stopMotor()
        drive.isSafetyEnabled = false
    }

    fun stop() {
        drive.stopMotor()
        motorSafety.feed()
    }

    fun setToCoast() {
//        sequenceOf(
//            frontLeft,
//            rearLeft,
//            frontRight,
//            rearRight,
//        ).forEach { it.idleMode = CANSparkBase.IdleMode.kCoast }
    }

    fun setToBreak() {
//        sequenceOf(
//            frontLeft,
//            rearLeft,
//            frontRight,
//            rearRight,
//        ).forEach { it.idleMode = CANSparkBase.IdleMode.kBrake }
    }


    fun holonomic(forward: Double, turn: Double, strafe: Double) {
        drive.driveCartesian(forward, -strafe, turn)
        currentForward = forward
    }
}
