package frc.actions

import frc.subsystems.Intake
import frc.subsystems.Launcher
import kotlin.math.absoluteValue

fun launch(intake: Intake, launcher: Launcher, speed: Double, lookUpSpeed: Double): Boolean {
    if (!intake.hasSomething) {
        launcher.stopMotor()
        intake.stopMotor()
        return false
    }

    val shouldLaunch = launcher.speed(to = lookUpSpeed)
    println("Outside if launcher speed: ${launcher.speed}")
    println("Outside if target speed: ${speed.absoluteValue}")
    if (shouldLaunch) {
        println("Inside if launcher speed: $speed")
        println("Inside if target speed: ${speed.absoluteValue}")
        intake.launch()
    }
    return true
}