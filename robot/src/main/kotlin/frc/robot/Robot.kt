package frc.robot

import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkLowLevel
import com.revrobotics.CANSparkMax
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.controller.PIDController
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
    private val joystick0 = Joystick(0) //drive joystick
/*
    private val ledBuffer = AddressableLEDBuffer(20)
    private val led = AddressableLED(9).apply {
        setLength(ledBuffer.length)
        setData(ledBuffer)
    }



HELLO
 */
    // TODO set the channels and device ids
    private val launcherBottomLimit = DigitalInput(0)
    private val launcherTopLimit = DigitalInput(1)
//    private val launcherArmEncoderLeft = DutyCycleEncoder(2)
//    private val launcherArmEncoderRight = DutyCycleEncoder(4)
    private val boreEncoder = Encoder(5, 6, true, CounterBase.EncodingType.k1X)
    private val launcherArmMotorsPID = PIDController(0.01, 0.0, 0.0) // power first until oscillates, I until gets there fast, then D until no oscillations
    private var launchPostAccelerationDelay = 0
    private val lookupArray = arrayOf(doubleArrayOf(-55.0,25.0), doubleArrayOf(-70.0,33.0), doubleArrayOf(-70.0,35.0), doubleArrayOf(-70.0,36.0))

//    launcher motors - right motor(21) follows left motor(20)
    private val launcherArmMotors = CANSparkMax(20, CANSparkLowLevel.MotorType.kBrushless)
        .also { lead ->
            with(CANSparkMax(21, CANSparkLowLevel.MotorType.kBrushless)) {
                setIdleMode(CANSparkBase.IdleMode.kBrake)
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



    private val intake: CANSparkMax = CANSparkMax(13, CANSparkLowLevel.MotorType.kBrushless)

    private val driveTrainSubsystem = DriveTrainSubsystem()

    private val limelightRunner = LimelightRunner()

    private val autonomousChooser = SendableChooser<Command>()

    private val rawDrive = TeleopDrive(
        inputThrottle = { joystick0.y },
        inputTurn = { joystick0.twist },
        inputYaw = { joystick0.x },
        drive = { forward, _, turn -> driveTrainSubsystem.arcade(forward, turn, squareInputs = true) },
        stop = { driveTrainSubsystem.stop() }
    )

    //
    override fun robotInit() {
//        led.start()

        driveTrainSubsystem.initialize()

        with(autonomousChooser) {
            setDefaultOption("Nothing", null)
            SmartDashboard.putData("Autonomous Mode", this)
        }
    }


    override fun robotPeriodic() {
        CommandScheduler.getInstance().run()
    }

    override fun autonomousInit() {
        driveTrainSubsystem.setToBreak()
        val autonomous = autonomousChooser.selected
        autonomous?.schedule()
    }

    override fun autonomousPeriodic() {
    }

    override fun autonomousExit() {
        CommandScheduler.getInstance().cancelAll()
    }

    override fun teleopInit() {
        driveTrainSubsystem.setToCoast()
        rawDrive.schedule()
        // resetArm()

        while(!armDown())
        {
            println("arm moving down")
        }

        boreEncoder.reset()
    }

    private val targetSpeed = -5000 * 0.69
//    var frame = 0
    override fun teleopPeriodic() {
        SmartDashboard.putNumber("Launcher Speed", launcher.encoder.velocity)
        //SmartDashboard.putNumber("Left Arm Encoder", launcherArmEncoderLeft.run { absolutePosition - positionOffset })
        //SmartDashboard.putNumber("Right Arm Encoder", launcherArmEncoderRight.run { absolutePosition - positionOffset })
        SmartDashboard.putNumber("Launcher Motor Encoder", launcherArmMotors.encoder.position)
//        SmartDashboard.putNumber("Right Arm Motor Encoder", launcherArmRight.encoder.position)
        SmartDashboard.putBoolean("Bottom Limit", launcherBottomLimit.get())
        SmartDashboard.putBoolean("Top Limit", launcherTopLimit.get())
        getEncoderValue()

//    var encoderValue = getEncoderValue()

    var desiredAngle = 0.0
    var lookUpSpeed = 0.0

    if(limelightRunner.hasTargetTag) {
        val distanceToTag = limelightRunner.distance
        val roundedDistance = limelightRunner.lookupTableRound(distanceToTag)
        desiredAngle = lookupArray[(roundedDistance/2)-1][1]
        lookUpSpeed = lookupArray[(roundedDistance/2)-1][0]
        println("roundedDistance: " + roundedDistance)
        println("desiredAngle: " + desiredAngle)
        println("lookUpSpeed: " + lookUpSpeed)
    }

    if(joystick0.getRawButton(1)) {
        launch(5000 * (lookUpSpeed + 0.05), lookUpSpeed)
    } else if(joystick0.getRawButton(2)) {
        collect()
    } else {
        launcher.stopMotor()
        intake.stopMotor()
    }

    if(joystick0.getRawButton(3)) {
        armUp()
    } else if(joystick0.getRawButton(4)) {
        armDown()
    } else if(joystick0.getRawButton(5)) {
        felipeSetAngle(convertToTicks(desiredAngle))
    } else if(joystick0.getRawButton(6)) {
        felipeSetAngle(0.0)
    } else if(joystick0.getRawButton(7)) {
        felipeSetAngle(499.0)
    } else if(joystick0.getRawButton(8)) {
        felipeSetAngle(170.0)
    } else {
        felipeSetAngle(convertToTicks(17.9))
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


    private fun launch(speed: Double, lookUpSpeed: Double) {
//        private val targetSpeed = -5000 * 0.2
        if (!intakeSlot.get()) {
            launcher.set(lookUpSpeed)
            println("Outside if launcher speed: " + launcher.encoder.velocity.absoluteValue)
            println("Outside if target speed: " + speed.absoluteValue)
            if (launcher.encoder.velocity.absoluteValue >= speed.absoluteValue) {
                println("Inside if launcher speed: " + launcher.encoder.velocity.absoluteValue)
                println("Inside if target speed: " + speed.absoluteValue)
                intake.set(-0.5)
//                launchPostAccelerationDelay++
//                if (launchPostAccelerationDelay >= 2) {
//                    //
//                }
            } //else {
//                launchPostAccelerationDelay = 0
//            }
        } else {
            launcher.stopMotor()
            intake.stopMotor()
        }
    }

    private fun collect() {
        if (intakeSlot.get()) {
            intake.set(-0.65)
        } else {
            intake.stopMotor()
        }
    }

    private fun getEncoderValue(): Double {
        SmartDashboard.putNumber("Bore Encoder", boreEncoder.distance)
        return boreEncoder.distance
    }

    private fun convertToTicks(angle: Double):Double {
        return angle * 5.670
    }

    /*

    private fun resetArm() {
        // TODO find correct speed to run the arm towards zero
        // TODO find if the limit switch is open by default

        when {
            launcherBottomLeftLimit.get() -> launcherArmLeft.set(-0.3)
            else -> {
                launcherArmLeft.encoder.setPosition(0.0)
               // launcherArmEncoderLeft.reset()
            }
        }

        when {
            launcherBottomRightLimit.get() -> launcherArmRight.set(-0.3)
            else -> {
                launcherArmRight.encoder.setPosition(0.0)
               // launcherArmEncoderRight.reset()
            }

        }
    }
     */


    var launcherSpeed = 0.2  //TODO armSpeed

    //top limit switch is false when broken
    //bottom limit switch is true when broken
    private fun armUp() {
        var limitValue = launcherTopLimit.get()
        if(!limitValue) {
            println("trying to stop")
            launcherArmMotors.stopMotor()
            launcherArmMotors.encoder.setPosition(0.25)
        }
        else {
            println("trying to move")
            launcherArmMotors.set(launcherSpeed)
        }
    }

    private fun armDown(): Boolean {
        if(launcherBottomLimit.get()) {
            println("trying to stop")
            launcherArmMotors.stopMotor()
            return true
//            launcherArmMotors.encoder.setPosition(0.0)
        }
        else {
            launcherArmMotors.set(-launcherSpeed)
            return false
        }
    }

    private fun felipeSetAngle(setpoint:Double) {
        val upperLimit = launcherTopLimit.get()
        val bottomLimit = launcherBottomLimit.get()
        val position = boreEncoder.distance
        val velocity = MathUtil.clamp(launcherArmMotorsPID.calculate(position, setpoint), -launcherSpeed, launcherSpeed)
        when {
            !upperLimit && velocity > 0 -> launcherArmMotors.stopMotor()
            bottomLimit && velocity < 0 -> {
                launcherArmMotors.stopMotor()
                launcherArmMotors.encoder.setPosition(0.0)
            }
            else -> launcherArmMotors.set(velocity)
        }
    }

    private fun setArmAngle(setpoint: Double) {
//        var topLimitValue = launcherTopLimit.get()
//        var bottomLimitValue = launcherTopLimit.get()
        val position = boreEncoder.distance
//        if(!topLimitValue && !bottomLimitValue && boreEncoder.rate > 0) {
//            launcherArmMotors.stopMotor()
//        } else if(bottomLimitValue && topLimitValue && boreEncoder.rate < 0) {
//            launcherArmMotors.stopMotor()
//        } else {
//            println("Setpoint: " + setpoint)
//            println("Encoder Distance: " + position)
        launcherArmMotors.set(MathUtil.clamp(launcherArmMotorsPID.calculate(position, setpoint), -0.3, 0.3))
    }

    private fun shinBreaker(setpoint: Double) {
        var topLimitValue = launcherTopLimit.get()
        var bottomLimitValue = launcherTopLimit.get()
    }


//        if(!topLimitValue && !bottomLimitValue && boreEncoder.rate > 0) {
//            launcherArmMotors.stopMotor()
//        } else if(bottomLimitValue && topLimitValue && boreEncoder.rate < 0) {
//            launcherArmMotors.stopMotor()
//        } else {
//            println("Setpoint: " + setpoint)
//            println("Encoder Distance: " + position)
//            launcherArmMotors.set(MathUtil.clamp(launcherArmMotorsPID.calculate(position, setpoint), -0.3, 0.3))
//        }
//    }

    override fun teleopExit() {
//        driveTrainSubsystem.setToBreak()
        CommandScheduler.getInstance().cancelAll()
    }

    override fun testInit() {
    }

    override fun disabledPeriodic() {
    }
}
