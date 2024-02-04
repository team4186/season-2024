package frc.robot

import com.revrobotics.CANSparkLowLevel
import com.revrobotics.CANSparkMax
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import frc.commands.drive.TeleopDrive
import frc.subsystems.DriveTrainSubsystem

class Robot : TimedRobot() {
    private enum class DriveMode {
        Raw,
        Cheesy
    }

    private val joystick0 = Joystick(0) //drive joystick


//    val ledBuffer = AddressableLEDBuffer(100)
//    val led = AddressableLED(9).apply {
//        setLength(ledBuffer.length)
//        setData(ledBuffer);
//    }

    private val driveTrainSubsystem = DriveTrainSubsystem()
    //private val compressor = Compressor(0, PneumaticsModuleType.CTREPCM)

    private val autonomousChooser = SendableChooser<Command>()
    private val driveModeChooser = SendableChooser<DriveMode>()

    private val rawDrive = TeleopDrive(
        inputThrottle = { joystick0.y },
        inputTurn = { joystick0.twist },
        inputYaw = { joystick0.x },
        drive = { forward, _, turn -> driveTrainSubsystem.arcade(forward, turn, squareInputs = true) },
        stop = { driveTrainSubsystem.stop() }
    )

    override fun robotInit() {

        //led.start();

        //driveTrainSubsystem.initialize()
//        compressor.enableDigital()

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


    var frame = 0
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

                led.setData(ledBuffer)
                */

        frame++
    }

    override fun autonomousInit() {
        //driveTrainSubsystem.setToBreak()
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

    val motor0: CANSparkMax = CANSparkMax(12, CANSparkLowLevel.MotorType.kBrushless)
    val motor1: CANSparkMax = CANSparkMax(15, CANSparkLowLevel.MotorType.kBrushless)
    val motor2: CANSparkMax = CANSparkMax(13, CANSparkLowLevel.MotorType.kBrushless)

    override fun teleopPeriodic() {
        motor0.set(0.75)
        motor1.set(0.75)
        motor2.set(0.75)
    }

    override fun teleopExit() {
        driveTrainSubsystem.setToBreak()
        CommandScheduler.getInstance().cancelAll()
    }

    override fun testInit() {
    }
}
