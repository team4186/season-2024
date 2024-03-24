package frc.autonomous

import frc.actions.MAX_SPEED
import frc.actions.launch
import frc.actions.resetArm
import frc.robot.Robot

/*
 * Create new kotlin file for each Routine.
 *
 * Make all state private to the file
 *
 */

class SingleNoteRoutine : AutonomousRoutine {
    private enum class AutoSequence {
        RESETARM,
        SHOOTPRELOAD,
        STOP
    }

    private var autoState = AutoSequence.RESETARM

    override fun init(robot: Robot) {
        autoState = AutoSequence.RESETARM
    }

    override fun exit(robot: Robot) {
        robot.arm.stopMotor()
        robot.launcher.stopMotor()
        robot.driveTrain.stop()
    }

    override fun periodic(robot: Robot) {
        when (autoState) {
            AutoSequence.RESETARM -> {
                if (resetArm(robot.arm)) {
                    autoState = AutoSequence.SHOOTPRELOAD
                }
            }

            AutoSequence.SHOOTPRELOAD -> {
                when {
                    robot.intake.hasSomething -> {
                        robot.arm.move(to = 17.0)
                        launch(robot.intake, robot.launcher, 0.70 * MAX_SPEED)
                    }

                    else -> {
                        robot.launcher.stopMotor()
                        autoState = AutoSequence.STOP
                    }
                }
            }

            AutoSequence.STOP -> {
                robot.arm.stopMotor()
                robot.launcher.stopMotor()
                robot.driveTrain.stop()
            }
        }
    }
}
