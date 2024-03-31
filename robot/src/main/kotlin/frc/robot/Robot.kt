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

    val leds = Leds(
        port = 9,
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

    }

    override fun robotPeriodic() {
        report()
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

    override fun teleopPeriodic() {
        fun checkButton(id: Int) = joystick0.getRawButton(id) || joystick1.getRawButton(id)

        val isTagOnTarget = when {
            checkButton(12) -> alignToTarget(
                forward = 0.5,
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
                false
            }
        }

        leds.lightUp(
            when {
                isTagOnTarget -> Leds.Color.Green
                else -> Leds.Color.Red
            }
        )

        // Waiting for the arm to reset
        if (resetArm(arm)) return

        val (desiredAngle, lookUpSpeed) = findLaunchAngleAndSpeed(limelightRunner)
        SmartDashboard.putNumber("Desired angle", desiredAngle)
        SmartDashboard.putNumber("Desired launch speed", lookUpSpeed)

        when {
            checkButton(1) -> launch(intake, launcher, lookUpSpeed)
            checkButton(2) -> intake.collect()
            checkButton(10) -> launch(intake, launcher, TEST_SPEED)
            checkButton(11) -> intake.eject()
            else -> {
                launcher.stopMotor()
                intake.stopMotor()
            }
        }

        when {
            checkButton(3) -> arm.moveUp()
            checkButton(4) -> arm.moveDown()
            checkButton(5) -> arm.move(to = desiredAngle)
            checkButton(6) -> arm.move(to = 0.0)
            checkButton(7) -> arm.move(to = 90.0)
            checkButton(8) -> arm.move(to = 170.0)
            else -> arm.move(to = 0.0)
        }

    }

    override fun teleopExit() {
    }

    override fun testInit() {
        driveTrain.initialize()
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
    }
}
