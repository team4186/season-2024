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
import frc.subsystems.DriveTrainSubsystem
import frc.vision.LimelightRunner
import kotlin.math.absoluteValue

class Robot : TimedRobot() {
    private val joystick0 = Joystick(0) //drive joystick
    private val joystick1 = Joystick(1) //operator joystick
    private val ledBuffer = AddressableLEDBuffer(20)
//    private val led = AddressableLED(9).apply {
//        setLength(ledBuffer.length)
//        setData(ledBuffer)
//    }

    // TODO set the channels and device ids
    internal val launcherBottomLimit = DigitalInput(0)
    private val launcherTopLimit = DigitalInput(1)

    //    private val launcherArmEncoderLeft = DutyCycleEncoder(2)
//    private val launcherArmEncoderRight = DutyCycleEncoder(4)
    private val boreEncoder = Encoder(5, 6, true, CounterBase.EncodingType.k1X)
    private val launcherArmMotorsPID = PIDController(
        0.005,
        0.0,
        0.0
    ) // power first until oscillates, I until gets there fast, then D until no oscillations
    private var launchPostAccelerationDelay = 0
    internal val lookupArray = arrayOf(
        doubleArrayOf(-0.70, 17.0),
        doubleArrayOf(-0.70, 17.0),
        doubleArrayOf(-0.70, 21.0),
        doubleArrayOf(-0.70, 26.0),
        doubleArrayOf(-0.70, 28.0),
        doubleArrayOf(-0.70, 30.0),
        doubleArrayOf(-0.70, 32.0),
        doubleArrayOf(-0.75, 34.0),
        doubleArrayOf(-0.75, 37.0),
        doubleArrayOf(-0.80, 37.0),
        doubleArrayOf(-0.80, 38.5),
        doubleArrayOf(-0.85, 38.7)
    )

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


    private val intake: CANSparkMax = CANSparkMax(13, CANSparkLowLevel.MotorType.kBrushless)

    val driveTrainSubsystem = DriveTrainSubsystem()

    internal val limelightRunner = LimelightRunner()

    private val autonomousChooser = SendableChooser<AutonomousRoutine>()
    private var selectedAutonomous: AutonomousRoutine = AutonomousRoutine.Empty

    enum class teleopSequence {
        RESETARM,
        NORMALTELEOP,
        STOP
    }

    var teleopState = teleopSequence.RESETARM

    override fun robotInit() {
        driveTrainSubsystem.initialize()

        // Set up the autonomous options
        with(autonomousChooser) {
            setDefaultOption("Nothing", AutonomousRoutine.Empty)
            addOption("Single Note Routine", SingleNoteRoutine())
            // TODO add more routine options

            SmartDashboard.putData("Autonomous Mode", this)
        }

        SmartDashboard.putBoolean("TwoPieceforTrue", true)
    }

    override fun robotPeriodic() {
    }

    override fun autonomousInit() {
        driveTrainSubsystem.setToBreak()

        // Retrieve the selected autonomous routine and initialize
        selectedAutonomous = autonomousChooser.selected
        selectedAutonomous.init(this)
    }

    override fun autonomousPeriodic() {
        selectedAutonomous.periodic(this)
    }

    override fun autonomousExit() {
        selectedAutonomous.exit(this)
    }

    override fun teleopInit() {
        driveTrainSubsystem.setToCoast()
        // resetArm()

        boreEncoder.reset()
        teleopState = teleopSequence.RESETARM
        println("teleopState is now RESETARM")
    }

    //    private val targetSpeed = -5000 * 0.69
//    var frame = 0

    override fun teleopPeriodic() {
        when (teleopState) {
            teleopSequence.RESETARM -> {
                armDown()
                if (launcherBottomLimit.get()) {
                    teleopState = teleopSequence.NORMALTELEOP
                }
            }

            teleopSequence.NORMALTELEOP -> {
                SmartDashboard.putNumber("Launcher Speed", launcher.encoder.velocity)
                //SmartDashboard.putNumber("Left Arm Encoder", launcherArmEncoderLeft.run { absolutePosition - positionOffset })
                //SmartDashboard.putNumber("Right Arm Encoder", launcherArmEncoderRight.run { absolutePosition - positionOffset })
                SmartDashboard.putNumber("Launcher Motor Encoder", launcherArmMotors.encoder.position)
//        SmartDashboard.putNumber("Right Arm Motor Encoder", launcherArmRight.encoder.position)
                SmartDashboard.putBoolean("Bottom Limit", launcherBottomLimit.get())
                SmartDashboard.putBoolean("Top Limit", launcherTopLimit.get())
                getEncoderValue()

                val (desiredAngle, lookUpSpeed) = when {
                    limelightRunner.hasTargetTag -> {
                        val distanceToTag = limelightRunner.distance
                        val roundedDistance = limelightRunner.lookupTableRound(distanceToTag)
                        println("roundedDistance: $roundedDistance")
                        Pair(
                            lookupArray[(roundedDistance)][1],
                            lookupArray[(roundedDistance)][0]
                        )
                    }

                    else -> 17.9 to 0.0
                }

                fun checkButton(id: Int) = joystick0.getRawButton(id) || joystick1.getRawButton(id)

                val isTagOnTarget = when {
                    checkButton(12) -> alignToTarget(
                        forward = 0.5,
                        turnController = SpeakerTurnPid,
                        drive = driveTrainSubsystem,
                        vision = limelightRunner,
                        offset = 0.0
                    )

                    else -> {
                        manualDrive(
                            forward = joystick0.y,
                            turn = joystick0.twist,
                            drive = driveTrainSubsystem
                        )
                        false
                    }
                }

                // TODO use the `isTagOnTarget` to light up the LEDs

                when {
                    checkButton(1) -> launch(lookUpSpeed + 0.01, lookUpSpeed)
                    checkButton(2) -> collect()
                    checkButton(10) -> launch(-0.20 + 0.01, -0.20)
                    checkButton(11) -> antiCollect()
                    else -> {
                        launcher.stopMotor()
                        intake.stopMotor()
                    }
                }

                when {
                    checkButton(3) -> armUp()
                    checkButton(4) -> armDown()
                    checkButton(5) -> felipeSetAngle(convertToTicks(desiredAngle))
                    checkButton(6) -> felipeSetAngle(0.0)
                    checkButton(7) -> felipeSetAngle(convertToTicks(92.0))
                    checkButton(8) -> felipeSetAngle(170.0)
                    else -> felipeSetAngle(convertToTicks(17.9))
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

    fun convertToTicks(angle: Double): Double {
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


    var launcherSpeed = 0.35  //TODO armSpeed

    //top limit switch is false when broken
    //bottom limit switch is true when broken
    private fun armUp() {
        var limitValue = launcherTopLimit.get()
        if (!limitValue) {
            println("trying to stop")
            launcherArmMotors.stopMotor()
            launcherArmMotors.encoder.setPosition(0.25)
        } else {
            println("trying to move")
            launcherArmMotors.set(launcherSpeed)
        }
    }

    fun armDown(): Boolean {
        if (launcherBottomLimit.get()) {
            println("trying to stop")
            boreEncoder.reset()
            launcherArmMotors.stopMotor()
            return true
//            launcherArmMotors.encoder.setPosition(0.0)
        } else {
            launcherArmMotors.set(-launcherSpeed)
            return false
        }
    }

    fun felipeSetAngle(setpoint: Double): Boolean {
        val upperLimit = launcherTopLimit.get()
        val bottomLimit = launcherBottomLimit.get()
        val position = boreEncoder.distance
        val velocity = MathUtil.clamp(launcherArmMotorsPID.calculate(position, setpoint), -launcherSpeed, launcherSpeed)
        when {
            !upperLimit && velocity > 0 -> {
                launcherArmMotors.stopMotor()
                return true
            }

            bottomLimit && velocity < 0 -> {
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
