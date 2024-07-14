package frc.robot

import com.revrobotics.CANSparkLowLevel
import com.revrobotics.CANSparkMax
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj.*
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.actions.*
import frc.autonomous.AutonomousRoutine
import frc.autonomous.SingleNoteRoutine
import frc.autonomous.TwoNotesRoutine
import frc.subsystems.*
import frc.vision.LimelightRunner

class Robot : TimedRobot() {
    private val joystick0 = Joystick(0) //drive joystick
    private val joystick1 = Joystick(1) //operator joystick


    val driveTrain = DriveTrainSubsystem(
            leftMotor = driveSparkMaxMotors(
                    CANSparkMax(8, CANSparkLowLevel.MotorType.kBrushless),
                    CANSparkMax(9, CANSparkLowLevel.MotorType.kBrushless),
                    inverted = true,
            ),
            rightMotor = driveSparkMaxMotors(
                    CANSparkMax(11, CANSparkLowLevel.MotorType.kBrushless),
                    CANSparkMax(10, CANSparkLowLevel.MotorType.kBrushless),
                    inverted = false,
            )
    )

    val arm = Arm(
            bottomLimit = DigitalInput(0),
            topLimit = DigitalInput(1),
            encoder = Encoder(5, 6, true, CounterBase.EncodingType.k1X),
            motor = armSparkMaxMotors(
                    CANSparkMax(20, CANSparkLowLevel.MotorType.kBrushless),
                    CANSparkMax(21, CANSparkLowLevel.MotorType.kBrushless),
            ),
            pid = PIDController(
                    0.015,
                    0.0,
                    0.0
            )
    )

    val intake = Intake(
            CANSparkMax(13, CANSparkLowLevel.MotorType.kBrushless),
            DigitalInput(4),
    )

    val launcher = Launcher(
            launcherSparkMaxMotors(
                    CANSparkMax(12, CANSparkLowLevel.MotorType.kBrushless),
                    CANSparkMax(15, CANSparkLowLevel.MotorType.kBrushless),
            )
    )

    val climber = Climber(
            DigitalInput(3),
            CANSparkMax(3, CANSparkLowLevel.MotorType.kBrushless),
    )

    val leds = Leds(
            port = 0,
    )

    internal val limelightRunner = LimelightRunner()

    private val autonomousChooser = SendableChooser<AutonomousRoutine>()
    private var selectedAutonomous: AutonomousRoutine = AutonomousRoutine.Empty

    override fun robotInit() {
        driveTrain.initialize()

        // Set up the autonomous options
        with(autonomousChooser) {
            setDefaultOption("Nothing", AutonomousRoutine.Empty)
            addOption("Single Note Routine", SingleNoteRoutine())
            addOption("Two Notes Routine", TwoNotesRoutine())
            // TODO add more routine options

            SmartDashboard.putData("Autonomous Mode", this)
        }
        SmartDashboard.putNumber("Override Angle Please", 17.0)
        SmartDashboard.putNumber("Override Speed", 0.7 * MAX_SPEED)
        SmartDashboard.putNumber("Change Angle", 0.0)
    }

    override fun robotPeriodic() {
        report()

        val isTagOnTarget = with(limelightRunner) { hasTargetTag && tagxOffset in -10.0..10.0 }

        leds.lightUp(
                when {
                    isTagOnTarget -> Leds.Color.Green
                    intake.hasSomething -> Leds.Color.Blue
                    else -> Leds.Color.Red
                }
        )
    }

    override fun autonomousInit() {
        driveTrain.setToBreak()

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
        arm.init()
        driveTrain.setToCoast()
        leds.init()
    }

    var launchButtonWasPressed = false
    var solution = DefaultAngleAndSpeed
    var restAngle = 0.0
    override fun teleopPeriodic() {
        fun checkButton(id: Int) = joystick0.getRawButton(id) || joystick1.getRawButton(id)

        when {
            checkButton(12) -> alignToTarget(
                    forward = joystick0.y,
                    turnController = SpeakerTurnPid,
                    drive = driveTrain,
                    vision = limelightRunner,
                    offset = 0.0
            )

            else -> {
                manualDrive(
                        forward = joystick0.y,
                        turn = joystick0.twist,
                        drive = driveTrain
                )
            }
        }

        // Waiting for the arm to reset
        if (resetArm(arm)) return



        val overrideAngle = SmartDashboard.getNumber("Override Angle Please", 17.0)
        val overrideSpeed = SmartDashboard.getNumber("Override Speed", 0.7 * MAX_SPEED)

        when (joystick0.pov) {
            0 -> restAngle = 55.0
            180 -> restAngle = 0.0
        }

        SmartDashboard
                .getNumber("Change Angle", 0.0)
                .let { angleChange ->
                    when {
                        checkButton(6) -> SmartDashboard.putNumber("Change Angle", angleChange + 1)
                        checkButton(5) -> SmartDashboard.putNumber("Change Angle", angleChange - 1)
                        else -> Unit
                    }
                }

        when {
            checkButton(1) -> {
                if(!launchButtonWasPressed) {
                    solution = findLaunchAngleAndSpeed(limelightRunner)
                }
                val angleChange = SmartDashboard.getNumber("Change Angle", 0.0)
                val inPosition = arm.move(to = solution.angle + angleChange)

                launch(intake, launcher, solution.speed, keepRunning = true, inPosition)
            }

            checkButton(2) -> intake.collect()
            checkButton(3) -> arm.moveUp()
            checkButton(4) -> arm.moveDown()
            checkButton(9) -> {
                val inPosition = arm.move(to = overrideAngle)
                launch(intake, launcher, overrideSpeed, keepRunning = true, inPosition)
            }


            checkButton(7) -> climber.moveDown()
            checkButton(8) -> climber.moveUp()

            checkButton(10) -> {
                val inPosition = arm.move(to = 90.0)
                launch(intake, launcher, 0.20 * 5000, keepRunning = true, inPosition)
            }

            checkButton(11) -> intake.eject()
            else -> {
                launcher.stopMotor()
                intake.stopMotor()
                climber.stopMotor()
                arm.move(to = restAngle)
            }
        }

        val (desiredAngle, lookUpSpeed) = solution
        SmartDashboard.putNumber("Desired angle", desiredAngle)
        SmartDashboard.putNumber("Desired launch speed", lookUpSpeed)
        launchButtonWasPressed = checkButton(1)
    }

    override fun teleopExit() {
    }

    override fun testInit() {
        driveTrain.initialize()
        climber.motor.encoder.position = 0.0
    }

    override fun testPeriodic() {

    }

    override fun disabledPeriodic() {
    }

    private fun report() {
        SmartDashboard.putNumber("Launcher Speed", launcher.speed)
        SmartDashboard.putNumber("Arm position", arm.position)
        SmartDashboard.putBoolean("Is Arm At Bottom", arm.isAtBottom)
        SmartDashboard.putBoolean("Is Arm At Top", arm.isAtTop)
        SmartDashboard.putBoolean("Something in Intake", intake.hasSomething)
        SmartDashboard.putNumber("Drive right distance", driveTrain.rightMotor.encoder.position)
        SmartDashboard.putNumber("Drive left distance", driveTrain.leftMotor.encoder.position)
        SmartDashboard.putNumber("Drive right velocity", driveTrain.rightMotor.encoder.velocity)
        SmartDashboard.putNumber("Drive left velocity", driveTrain.leftMotor.encoder.velocity)
        SmartDashboard.putNumber("Climber Position", climber.position)
        SmartDashboard.putBoolean("Climber is at Bottom", climber.isAtBottom)
        SmartDashboard.putString(
                "Tag distance",
                limelightRunner
                        .distance
                        .let {
                            when {
                                it.isNaN() -> "NaN"
                                else -> limelightRunner.lookupTableRound(it).toString()
                            }
                        }
        )
    }
}
