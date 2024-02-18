package frc.robot

import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkLowLevel
import com.revrobotics.CANSparkMax
import edu.wpi.first.wpilibj.*
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import frc.commands.drive.TeleopDrive
import frc.subsystems.DriveTrainSubsystem
import frc.vision.LimelightRunner
import kotlin.math.absoluteValue

class Robot : TimedRobot() {
//    private val joystick0 = Joystick(0) //drive joystick
/*
    private val ledBuffer = AddressableLEDBuffer(20)
    private val led = AddressableLED(9).apply {
        setLength(ledBuffer.length)
        setData(ledBuffer)
    }

 */

    // TODO set the channels and device ids

//    private val launcherLeftArmLow = DigitalInput(0)
//    private val launcherRightArmLow = DigitalInput(1)
//    private val launcherArmEncoderLeft = DutyCycleEncoder(2)
//    private val launcherArmEncoderRight = DutyCycleEncoder(4)
    private val launcherArmMotors = CANSparkMax(20, CANSparkLowLevel.MotorType.kBrushless)
        .also { lead ->
            with(CANSparkMax(21, CANSparkLowLevel.MotorType.kBrushless)) {
                setIdleMode(CANSparkBase.IdleMode.kCoast)
                encoder.setPositionConversionFactor(1.0)
                follow(lead, true)
            }
        }
        .apply {
            setIdleMode(CANSparkBase.IdleMode.kBrake)
            // TODO find the conversion factor
            encoder.setPositionConversionFactor(1.0)

            // TODO find if the encoder and motor needs to be inverted
        }
    /*
    // TODO find if it can be handled as a follower of launcherArmLeft
    private val launcherArmRight = CANSparkMax(21, CANSparkLowLevel.MotorType.kBrushless)
        .apply {
            setIdleMode(CANSparkBase.IdleMode.kBrake)
            // TODO find the conversion factor
            encoder.setPositionConversionFactor(1.0)
            // TODO find if the encoder and motor needs to be inverted
        }


     */
/*

    private val intakeSlot = DigitalInput(4)
    private val launcher = CANSparkMax(12, CANSparkLowLevel.MotorType.kBrushless)
        .also { lead ->
            with(CANSparkMax(15, CANSparkLowLevel.MotorType.kBrushless)) {
                setIdleMode(CANSparkBase.IdleMode.kCoast)
                follow(lead)
            }
        }
        .apply {
            setIdleMode(CANSparkBase.IdleMode.kCoast)
            pidController.i = 0.0000033
        }

     */

//    private val intake: CANSparkMax = CANSparkMax(13, CANSparkLowLevel.MotorType.kBrushless)

//    private val driveTrainSubsystem = DriveTrainSubsystem()

    private val limelightRunner = LimelightRunner()

    private val autonomousChooser = SendableChooser<Command>()
/*
    private val rawDrive = TeleopDrive(
        inputThrottle = { joystick0.y },
        inputTurn = { joystick0.twist },
        inputYaw = { joystick0.x },
        drive = { forward, _, turn -> driveTrainSubsystem.arcade(forward, turn, squareInputs = true) },
        stop = { driveTrainSubsystem.stop() }
    )
*/
    override fun robotInit() {
//        led.start()

//        driveTrainSubsystem.initialize()

        with(autonomousChooser) {
            setDefaultOption("Nothing", null)
            SmartDashboard.putData("Autonomous Mode", this)
        }
    }


    override fun robotPeriodic() {
        CommandScheduler.getInstance().run()
    }

    override fun autonomousInit() {
//        driveTrainSubsystem.setToBreak()
        val autonomous = autonomousChooser.selected
        autonomous?.schedule()
    }

    override fun autonomousPeriodic() {
    }

    override fun autonomousExit() {
        CommandScheduler.getInstance().cancelAll()
    }

    override fun teleopInit() {
//        driveTrainSubsystem.setToCoast()
//        rawDrive.schedule()
        // resetArm()
    }

    private val targetSpeed = -5000 * 0.6
//    var frame = 0
    override fun teleopPeriodic() {

        launcherArmMotors.set(0.05)
        /*
        SmartDashboard.putNumber("Launcher Speed", launcher.encoder.velocity)
        SmartDashboard.putNumber("Left Arm Encoder", launcherArmEncoderLeft.run { absolutePosition - positionOffset })
        SmartDashboard.putNumber("Right Arm Encoder", launcherArmEncoderRight.run { absolutePosition - positionOffset })
        SmartDashboard.putNumber("Left Arm Motor Encoder", launcherArmLeft.encoder.position)
        SmartDashboard.putNumber("Right Arm Motor Encoder", launcherArmRight.encoder.position)

         */

        when {
//            joystick0.getRawButton(1) -> launch(targetSpeed)
//            joystick0.getRawButton(2) -> collect()
//            joystick0.getRawButton(3) -> resetArm()
            else -> {
//                intake.stopMotor()
//                launcher.stopMotor()
            }
        }
/*
        if (limelightRunner.hasTargetRing) {
            println("Robot has the game piece.")
            if (limelightRunner.xOffset > 0) {
                repeat(ledBuffer.length) { ledBuffer.setRGB(it, 0, 200, 0) }
//                driveTrainSubsystem.arcade(0.0, 0.3, false)
            } else if (limelightRunner.xOffset < 0) {
                repeat(ledBuffer.length) { ledBuffer.setRGB(it, 200, 0, 0) }
//                driveTrainSubsystem.arcade(0.0, -0.3, false)
            }
        } else {
//            frame = 0
            println("No game piece")
            repeat(ledBuffer.length) { ledBuffer.setRGB(it, 0, 0, 200) }
//            driveTrainSubsystem.stop()
        }


        //repeat(ledBuffer.length) { ledBuffer.setRGB(it, 70, 0, 150) }
        led.setData(ledBuffer)

 */
    }
/*
    private var launchPostAccelerationDelay = 0
    private fun launch(speed: Double) {
        if (!intakeSlot.get()) {
            launcher.pidController.setReference(speed, CANSparkBase.ControlType.kVelocity)
            if (launcher.encoder.velocity.absoluteValue >= speed.absoluteValue) {
                launchPostAccelerationDelay++
                if (launchPostAccelerationDelay >= 5) {
                    intake.set(-0.75)
                }
            } else {
                launchPostAccelerationDelay = 0
            }
        } else {
            launcher.stopMotor()
            intake.stopMotor()
        }
    }

    private fun collect() {
        if (intakeSlot.get()) {
            intake.set(-0.5)
        } else {
            intake.stopMotor()
        }
    }

    private fun resetArm() {
        // TODO find correct speed to run the arm towards zero
        // TODO find if the limit switch is open by default

        when {
            launcherLeftArmLow.get() -> launcherArmLeft.set(-0.3)
            else -> {
                launcherArmLeft.encoder.setPosition(0.0)
                launcherArmEncoderLeft.reset()
            }
        }

        when {
            launcherRightArmLow.get() -> launcherArmRight.set(-0.3)
            else -> {
                launcherArmRight.encoder.setPosition(0.0)
                launcherArmEncoderRight.reset()
            }

        }
    }

 */

    override fun teleopExit() {
//        driveTrainSubsystem.setToBreak()
        CommandScheduler.getInstance().cancelAll()
    }

    override fun testInit() {
    }

    override fun disabledPeriodic() {
    }
}
