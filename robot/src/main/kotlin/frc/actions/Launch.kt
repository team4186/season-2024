package frc.actions

import frc.subsystems.Intake
import frc.subsystems.Launcher
import kotlin.math.absoluteValue

fun launch(intake: Intake, launcher: Launcher, speed: Double, keepRunning: Boolean, inPosition: Boolean): Boolean {
    if (!intake.hasSomething) {
        if(!keepRunning) {
            launcher.stopMotor()
        }
        intake.stopMotor()
        return false
    }

    val shouldLaunch = launcher.accelerate(to = speed)

    if(shouldLaunch && inPosition) {
        intake.launch()
    }
    return true
}