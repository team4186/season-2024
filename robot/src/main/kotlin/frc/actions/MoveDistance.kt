package frc.actions

import edu.wpi.first.math.controller.PIDController
import frc.subsystems.DriveTrainSubsystem

val MoveDistancePid = PIDController(0.15, 0.0, 0.0)

fun moveDistance(
    driveTrain: DriveTrainSubsystem,
    distance: Double,
    leftPid: PIDController = MoveDistancePid,
    rightPid: PIDController = MoveDistancePid,
): Boolean {
    val leftSpeed = leftPid.calculate(driveTrain.leftMotor.encoder.position, distance).coerceIn(-0.3, 0.3)
    val rightSpeed = rightPid.calculate(driveTrain.rightMotor.encoder.position, distance).coerceIn(-0.3, 0.3)

    driveTrain.leftMotor.set(leftSpeed)
    driveTrain.rightMotor.set(rightSpeed)

    return (driveTrain.leftMotor.encoder.position >= distance) || (driveTrain.rightMotor.encoder.position >= distance)
}
