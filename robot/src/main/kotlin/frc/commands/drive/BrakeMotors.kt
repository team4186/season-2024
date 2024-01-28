package frc.commands.drive

import edu.wpi.first.wpilibj2.command.Command
import frc.subsystems.DriveTrainSubsystemMecanum

class BrakeMotors(
    private val drive: DriveTrainSubsystemMecanum
) : Command() {
    private var wait = 0

    override fun execute() {
        if (wait == 0) {
            drive.setToBreak()
            wait++
        }
    }

    override fun end(interrupted: Boolean) {
        drive.setToCoast()
        wait = 0
    }
}