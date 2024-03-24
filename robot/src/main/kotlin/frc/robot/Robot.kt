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
    private val ledBuffer = AddressableLEDBuffer(20)
//    private val led = AddressableLED(9).apply {
//        setLength(ledBuffer.length)
//        setData(ledBuffer)
//    }


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
            0.005,
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
        arm.reset()
        driveTrain.setToCoast()
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

        // TODO use the `isTagOnTarget` to light up the LEDs

        // Waiting for the arm to reset
        if (resetArm(arm)) return

        SmartDashboard.putNumber("Launcher Speed", launcher.speed)
        SmartDashboard.putNumber("Launcher Motor Encoder", arm.position)
        SmartDashboard.putBoolean("Bottom Limit", arm.isAtBottom)
        SmartDashboard.putBoolean("Top Limit", arm.isAtTop)

        val (desiredAngle, lookUpSpeed) = findLaunchAngleAndSpeed(limelightRunner)

        when {
            checkButton(1) -> launch(intake, launcher, lookUpSpeed + 0.01, lookUpSpeed)
            checkButton(2) -> intake.collect()
            checkButton(10) -> launch(intake, launcher, -0.20 + 0.01, -0.20)
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
            checkButton(7) -> arm.move(to = 92.0)
            checkButton(8) -> arm.move(to = 170.0)
            else -> arm.move(to = 17.9)
        }

    }

    override fun teleopExit() {
    }

    override fun testInit() {
    }

    override fun testPeriodic() {
        SmartDashboard.putNumber("Launcher Speed", launcher.speed)
        SmartDashboard.putNumber("Launcher Motor Encoder", arm.position)
        SmartDashboard.putBoolean("Bottom Limit", arm.isAtBottom)
        SmartDashboard.putBoolean("Top Limit", arm.isAtTop)
        SmartDashboard.putBoolean("Intake Limit", intake.hasSomething)
    }

    override fun disabledPeriodic() {
    }
}
