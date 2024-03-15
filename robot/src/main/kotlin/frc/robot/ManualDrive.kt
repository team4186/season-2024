package frc.robot

import frc.subsystems.DriveTrainSubsystem
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.withSign

fun manualDrive(
    forward: Double,
    turn: Double,
    drive: DriveTrainSubsystem,
    direction: Double = -1.0,
) {
    drive.arcade(
        attenuated(direction * forward),
        attenuated(direction * -0.8 * turn),
        squareInputs = true,
    )
}

private fun attenuated(value: Double): Double {
    return 0.90 * value.absoluteValue.pow(2).withSign(value)
}
