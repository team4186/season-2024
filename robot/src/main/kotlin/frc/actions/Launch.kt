package frc.actions

import frc.subsystems.Intake
import frc.subsystems.Launcher
import kotlin.math.absoluteValue

fun launch(intake: Intake, launcher: Launcher, speed: Double): Boolean {
    if (!intake.hasSomething) {
        launcher.stopMotor()
        intake.stopMotor()
        return false
    }

    val shouldLaunch = launcher.accelerate(to = speed)

    if(shouldLaunch) {
        intake.launch()
    }
    return true
}