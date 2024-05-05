package frc.actions

import edu.wpi.first.math.controller.PIDController
import frc.subsystems.DriveTrainSubsystem
import frc.vision.LimelightRunner


val AmplifierTurnPid = PIDController(0.05, 0.0, 0.0)
val SpeakerTurnPid = PIDController(0.005, 0.0, 0.0)

/**
 * [alignToTarget] will drive the robot forward in target's direction until
 * the target is not in the camera field of view.
 *
 * @param forward forward factor
 * @param turnController PID controller to calculate the turn factor
 * @param drive the drivetrain to control
 * @param vision Limelight proxy
 * @param offset offset distance in pixels from the center of the camera image
 *
 * @return if the vision target is at the setpoint
 *
 */
fun alignToTarget(
    forward: Double,
    turnController: PIDController,
    drive: DriveTrainSubsystem,
    vision: LimelightRunner,
    offset: Double = 0.0,
    direction: Double = -0.3,
): Boolean {
    // quick check if the tag is in the camera frustum
    if (!vision.hasTargetTag) {
        // stop the motors if not seeing the tag
        drive.stop()
        turnController.reset()
        return false
    }
    drive.arcade(
        forward * direction,
        -turnController.calculate(vision.tagxOffset + offset).coerceIn(-0.3, 0.3),
        squareInputs = false
    )
    return turnController.atSetpoint()
}