package frc.robot

import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkLowLevel
import com.revrobotics.CANSparkMax
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj.*
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.CommandScheduler
import frc.commands.drive.TeleopDrive
import frc.subsystems.DriveTrainSubsystem
import frc.vision.LimelightRunner
import kotlin.math.absoluteValue

class Robot : TimedRobot() {
    private val joystick0 = Joystick(0) //drive joystick
    private val joystick1 = Joystick(1) //operator joystick
    private val ledBuffer = AddressableLEDBuffer(20)
    private val led = AddressableLED(9).apply {
        setLength(ledBuffer.length)
        setData(ledBuffer)
    }

    // TODO set the channels and device ids
    val launcherBottomLimit = DigitalInput(0)
    private val launcherTopLimit = DigitalInput(1)
//    private val launcherArmEncoderLeft = DutyCycleEncoder(2)
//    private val launcherArmEncoderRight = DutyCycleEncoder(4)
    private val boreEncoder = Encoder(5, 6, true, CounterBase.EncodingType.k1X)
    private val launcherArmMotorsPID = PIDController(0.005, 0.0, 0.0) // power first until oscillates, I until gets there fast, then D until no oscillations
    private var launchPostAccelerationDelay = 0
    val lookupArray = arrayOf(doubleArrayOf(-0.70,17.0),
        doubleArrayOf(-0.70,17.0),
        doubleArrayOf(-0.70,21.0),
        doubleArrayOf(-0.70,26.0),
        doubleArrayOf(-0.70,28.0),
        doubleArrayOf(-0.70,30.0),
        doubleArrayOf(-0.70,32.0),
        doubleArrayOf(-0.75,34.0),
        doubleArrayOf(-0.75,37.0),
        doubleArrayOf(-0.80,37.0),
        doubleArrayOf(-0.80,38.5),
        doubleArrayOf(-0.85,38.7))

//    launcher motors - right motor(21) follows left motor(20)
val launcherArmMotors = CANSparkMax(20, CANSparkLowLevel.MotorType.kBrushless)
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

    val intakeSlot = DigitalInput(4)
    val launcher = CANSparkMax(12, CANSparkLowLevel.MotorType.kBrushless)
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


    val intake: CANSparkMax = CANSparkMax(13, CANSparkLowLevel.MotorType.kBrushless)

    val driveTrainSubsystem = DriveTrainSubsystem()

    val limelightRunner = LimelightRunner()

    private val autonomousChooser = SendableChooser<AutonomousRoutine>()
    private var selectedAutonomous: AutonomousRoutine = AutonomousRoutine.Empty

    enum class autoSequence {
        RESETARM, SHOOTPRELOAD, ARMDOWN, MOVEFORWARD, SHOOTSECONDNOTE, STOP
    } //MOVEFORWARD needs to intake as well

    enum class teleopSequence {
        RESETARM, NORMALTELEOP, STOP
    }

    var teleopState = teleopSequence.RESETARM
    var autoState = autoSequence.RESETARM
    var stopDriveTrain = false

    private val rawDrive = TeleopDrive(
        inputThrottle = { joystick0.y },
        inputTurn = { -joystick0.twist },
        inputYaw = { joystick0.x },
        drive = { forward, _, turn -> driveTrainSubsystem.arcade(forward, turn, squareInputs = true) },
        stop = { driveTrainSubsystem.stop() }
    )

    //
    override fun robotInit() {
//        led.start()

        driveTrainSubsystem.initialize()

        with(autonomousChooser) {
            addOption("Nothing", AutonomousRoutine.Empty)
            addOption("Single Note Routine", SingleNoteRoutine())
            addOption("Double Note Routine", DoubleNoteRoutine())
            SmartDashboard.putData("Autonomous Mode", this)
        }
    }

    override fun robotPeriodic() {
        CommandScheduler.getInstance().run()

        SmartDashboard.putNumber("Launcher Speed", launcher.encoder.velocity)
        //SmartDashboard.putNumber("Left Arm Encoder", launcherArmEncoderLeft.run { absolutePosition - positionOffset })
        //SmartDashboard.putNumber("Right Arm Encoder", launcherArmEncoderRight.run { absolutePosition - positionOffset })
        SmartDashboard.putNumber("Launcher Motor Encoder", launcherArmMotors.encoder.position)
//        SmartDashboard.putNumber("Right Arm Motor Encoder", launcherArmRight.encoder.position)
        SmartDashboard.putBoolean("Bottom Limit", launcherBottomLimit.get())
        SmartDashboard.putBoolean("Top Limit", launcherTopLimit.get())
        SmartDashboard.putBoolean("Intake Slot", intakeSlot.get())
        getEncoderValue()
    }

    override fun autonomousInit() {
        driveTrainSubsystem.setToBreak()

        // Retrieve the selected autonomous routine and initialize
        selectedAutonomous = autonomousChooser.selected
        selectedAutonomous.init(this)
    }

//    var driveForward = true
    override fun autonomousPeriodic() {
       selectedAutonomous.periodic(this)

    /*
    if(limelightRunner.hasTargetTag) {
        val distanceToTag = limelightRunner.distance
        val roundedDistance = limelightRunner.lookupTableRound(distanceToTag)
        desiredAngle  = lookupArray[(roundedDistance)][1]
        lookUpSpeed = lookupArray[(roundedDistance)][0]
    }

    if(!intakeSlot.get()) {
        felipeSetAngle(convertToTicks(desiredAngle))
        launch(lookUpSpeed + 0.01, lookUpSpeed)
    } else {
        if(limelightRunner.hasTargetRing) {
            driveTrainSubsystem.arcade(1.0, 0.0, false)
        }
    }
*/
    }

    override fun autonomousExit() {
        selectedAutonomous.exit(this)
    }

    override fun teleopInit() {
        driveTrainSubsystem.setToCoast()
        rawDrive.schedule()
        // resetArm()

        boreEncoder.reset()
        teleopState = teleopSequence.RESETARM
        println("teleopState is now RESETARM")
    }

//    private val targetSpeed = -5000 * 0.69
    var lookUpSpeed = 0.0
    var desiredAngle = 0.0
//    var frame = 0

    override fun teleopPeriodic() {
        when(teleopState) {
            teleopSequence.RESETARM -> {
                armDown()
                if(!launcherBottomLimit.get()) {
                    teleopState = teleopSequence.NORMALTELEOP
                }
            }
            teleopSequence.NORMALTELEOP -> {
//    var encoderValue = getEncoderValue()

//    var desiredAngle = 0.0
//    var lookUpSpeed = 0.0

                if(limelightRunner.hasTargetTag) {
                    val distanceToTag = limelightRunner.distance
                    val roundedDistance = limelightRunner.lookupTableRound(distanceToTag)
                    desiredAngle = lookupArray[(roundedDistance)][1]
                    lookUpSpeed = lookupArray[(roundedDistance)][0]

                    println("roundedDistance: " + roundedDistance)
                }

                if(!intakeSlot.get()) {
                   repeat(ledBuffer.length) {
                       ledBuffer.setRGB(it, 0, 255, 0)
                   }
                }

                if(joystick0.getRawButton(1) || joystick1.getRawButton(1)) {
                    launch(lookUpSpeed + 0.01, lookUpSpeed)
                } else if(joystick0.getRawButton(2) || joystick1.getRawButton(2)) {
                    collect()
                } else if(joystick0.getRawButton(10) || joystick1.getRawButton(10)) {
//                    felipeSetAngle(convertToTicks(90.0))
                    launch(-0.20 + 0.01, -0.20)
                } else if(joystick0.getRawButton(11) || joystick1.getRawButton(11)) {
                    antiCollect()
                } else {
                    launcher.stopMotor()
                    intake.stopMotor()
                }

                if(joystick0.getRawButton(3) || joystick1.getRawButton(3)) {
                    armUp()
                } else if(joystick0.getRawButton(4) || joystick1.getRawButton(4)) {
                    armDown()
                } else if(joystick0.getRawButton(5) || joystick1.getRawButton(5)) {
                    felipeSetAngle(convertToTicks(desiredAngle))
                } else if(joystick0.getRawButton(6) || joystick1.getRawButton(6)) {
                    felipeSetAngle(0.0)
                } else if(joystick0.getRawButton(7) || joystick1.getRawButton(7)) {
                    felipeSetAngle(convertToTicks(92.0))
                } else if(joystick0.getRawButton(8) || joystick1.getRawButton(8)) {
                    felipeSetAngle(170.0)
                } else {
                    felipeSetAngle(convertToTicks(17.9))
                }

                if (joystick0.getRawButton(12) || joystick1.getRawButton(12)) {
                    alignToTarget()
                    stopDriveTrain = true
                } else {
                    if(stopDriveTrain) {
                        driveTrainSubsystem.stop()
                        stopDriveTrain = false
                    }
                }
            }
            teleopSequence.STOP -> {

            }
        }
}


    fun launch(speed: Double, lookUpSpeed: Double) {
//        private val targetSpeed = -5000 * 0.2
        if (!intakeSlot.get()) {
            launcher.set(lookUpSpeed)
            println("Outside if launcher speed: " + launcher.encoder.velocity.absoluteValue)
            println("Outside if target speed: " + (5000 * speed).absoluteValue)
            if (launcher.encoder.velocity.absoluteValue >= (5000 * speed).absoluteValue) {
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

    fun collect() {
        if (intakeSlot.get()) {
            intake.set(-0.65)
        } else {
            intake.stopMotor()
        }
    }

    private fun antiCollect() {
        intake.set(0.65)
    }

    private fun getEncoderValue(): Double {
        SmartDashboard.putNumber("Bore Encoder", boreEncoder.distance)
        return boreEncoder.distance
    }

    fun convertToTicks(angle: Double):Double {
        return angle * 5.670
    }

    private fun alignToTarget() {
        if (limelightRunner.hasTargetTag) {
            println("Robot has the game piece.")
            if (limelightRunner.tagxOffset > 0) {
                driveTrainSubsystem.arcade(0.0, 0.3, false)
                println("It's positive")
            } else if (limelightRunner.tagxOffset < 0) {
                driveTrainSubsystem.arcade(0.0, -0.3, false)
                println("It's negative")
            }
        } else {
            driveTrainSubsystem.stop()
        }
//        if(limelightRunner.tagxOffset < 0) {
//            driveTrainSubsystem.arcade(0.0,-0.3,false)
//            println("It's negative")
//        } else if(limelightRunner.tagxOffset > 0) {
//            driveTrainSubsystem.arcade(0.0,0.3,false)
//            println("It's positive")
//        } else {
//            driveTrainSubsystem.stop()
//            println("STOP")
//        }
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


    var armSpeed = 0.25

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
            launcherArmMotors.set(armSpeed)
        }
    }

    fun armDown(): Boolean {
        if(!launcherBottomLimit.get()) {
            println("trying to stop")
            boreEncoder.reset()
            launcherArmMotors.stopMotor()
            return true
//            launcherArmMotors.encoder.setPosition(0.0)
        }
        else {
            launcherArmMotors.set(-armSpeed)
            return false
        }
    }

    fun felipeSetAngle(setpoint:Double): Boolean {
        val upperLimit = launcherTopLimit.get()
        val bottomLimit = launcherBottomLimit.get()
        val position = boreEncoder.distance
        val velocity = MathUtil.clamp(launcherArmMotorsPID.calculate(position, setpoint), -armSpeed, armSpeed)
        when {
            !upperLimit && velocity > 0 -> {
                launcherArmMotors.stopMotor()
                return true
            }
            !bottomLimit && velocity < 0 -> {
                launcherArmMotors.stopMotor()
                launcherArmMotors.encoder.setPosition(0.0)
                return true
            }
            else -> {
                launcherArmMotors.set(velocity)
            }
        }
        return true
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
