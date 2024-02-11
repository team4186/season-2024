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

class Robot : TimedRobot() {
    private enum class DriveMode {
        Raw,
        Cheesy
    }

    private val joystick0 = Joystick(0) //drive joystick


    val ledBuffer = AddressableLEDBuffer(10)
    val led = AddressableLED(9).apply {
        setLength(ledBuffer.length)
        setData(ledBuffer);
    }

    val digitalInput = DigitalInput(9)

    val exhaustU: CANSparkMax = CANSparkMax(12, CANSparkLowLevel.MotorType.kBrushless).apply {
        setIdleMode(CANSparkBase.IdleMode.kCoast)
        pidController.i = 0.0000033
    }
    val exhaustL: CANSparkMax = CANSparkMax(15, CANSparkLowLevel.MotorType.kBrushless).apply {
        setIdleMode(CANSparkBase.IdleMode.kCoast)
        follow(exhaustU)
    }
    val intake: CANSparkMax = CANSparkMax(13, CANSparkLowLevel.MotorType.kBrushless)

    val driveTrainSubsystem = DriveTrainSubsystem()
    private val compressor = Compressor(0, PneumaticsModuleType.CTREPCM)

    private val limelightRunner = LimelightRunner()

    private val autonomousChooser = SendableChooser<Command>()
    private val driveModeChooser = SendableChooser<DriveMode>()

    private val rawDrive = TeleopDrive(
        inputThrottle = { joystick0.y },
        inputTurn = { joystick0.twist },
        inputYaw = { joystick0.x },
        drive = { forward, _, turn -> driveTrainSubsystem.arcade(forward, turn, squareInputs = true) },
        stop = { driveTrainSubsystem.stop() }
    )

    private val limeLightRunner = LimelightRunner()


    override fun robotInit() {

        led.start()

        driveTrainSubsystem.initialize()
        compressor.enableDigital()

        with(autonomousChooser) {
            setDefaultOption("Nothing", null)
            SmartDashboard.putData("Autonomous Mode", this)
        }

        with(driveModeChooser) {
            setDefaultOption("Raw", DriveMode.Raw)
            addOption("Cheesy", DriveMode.Cheesy)
            SmartDashboard.putData("Drive Mode", this)
        }
    }


    //var frame = 0
    override fun robotPeriodic() {
        CommandScheduler.getInstance().run()

        /*
                when {
                    (frame % 50 == 0) && ((frame / 50) % 2 == 0) -> repeat(ledBuffer.length) {
                        ledBuffer.setRGB(it, 0, 0, 255)
                    }
                    (frame % 50 == 0) && ((frame / 50) % 2 != 0) -> repeat(ledBuffer.length) {
                        ledBuffer.setRGB(it, 255, 0, 0)
                    }
                }

         */

        repeat(ledBuffer.length) {
            ledBuffer.setRGB(it, 200, 0, 0)
            //println("It's reaching the LEDs")
        }

        led.setData(ledBuffer)

        //frame++
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
    }

    val targetSpeed = -5000 * 0.6
    var frame = 0
    override fun teleopPeriodic() {

        if(!digitalInput.get()) {
            if(joystick0.getRawButton(1)) {
                exhaustU.pidController.setReference(targetSpeed, CANSparkBase.ControlType.kVelocity)
                println(exhaustU.encoder.velocity)
                if(exhaustU.encoder.velocity <= targetSpeed) {
                    frame++
                    if(frame >= 5)
                    {
                        intake.set(-0.75)
                    }
                } else {
                    frame = 0
                }
            } else {
                intake.set(0.0)
            }
        } else {
            if(joystick0.getRawButton(2)) {
                intake.set(-0.5)
                exhaustU.set(0.0)
            } else {
                intake.set(0.0)
                exhaustU.set(0.0)
            }
        }

//        println("Teleop")
        if(limelightRunner.hasTargetRing) {
            println("Has target")
            if(limelightRunner.xOffset > 0) {
                driveTrainSubsystem.arcade(0.0, 0.3, false)
            } else if(limelightRunner.xOffset < 0) {
                driveTrainSubsystem.arcade(0.0, -0.3, false)
            }
        } else {
            //println("Does not has target")
            driveTrainSubsystem.stop()
        }
    }

    override fun teleopExit() {
        driveTrainSubsystem.setToBreak()
        CommandScheduler.getInstance().cancelAll()
    }

    override fun testInit() {
    }

    override fun disabledPeriodic() {
    }
}
