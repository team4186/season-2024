package frc.autonomous

import frc.actions.*
import frc.robot.Robot

/*
 * Create new kotlin file for each Routine.
 *
 * Make all state private to the file
 *
 */

class TwoNotesRoutine : AutonomousRoutine {
    private enum class AutoSequence {
        RESETARM,
        SHOOTPRELOAD,
        ARMDOWN,
        MOVEFORWARD,
        SHOOTSECONDNOTE,
        STOP
    } //MOVEFORWARD needs to intake as well

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
            AutoSequence.RESETARM -> if (!resetArm(robot.arm)) {
                autoState = AutoSequence.SHOOTPRELOAD
            }

            AutoSequence.SHOOTPRELOAD -> {
                when {
                    robot.intake.hasSomething -> {
                        if (moveArm(robot.arm, 17.0)) {
                            launch(robot.intake, robot.launcher, 0.70 * MAX_SPEED)
                        }
                    }

                    else -> {
                        robot.launcher.stopMotor()
                        autoState = AutoSequence.ARMDOWN
                    }
                }
            }

            AutoSequence.ARMDOWN -> {
                robot.arm.move(to = 0.0)
                if (robot.arm.isAtBottom) {
                    autoState = AutoSequence.MOVEFORWARD
                }
            }

            AutoSequence.MOVEFORWARD -> {
                robot.driveTrain.arcade(1.0, 0.0, false)
                robot.intake.collect()
                if (robot.intake.hasSomething) {
                    robot.driveTrain.stop()
                    autoState = AutoSequence.SHOOTSECONDNOTE
                }
            }

            AutoSequence.SHOOTSECONDNOTE -> {
                if (robot.intake.hasSomething) {
                    val (desiredAngle, lookUpSpeed) = findLaunchAngleAndSpeed(robot.limelightRunner)
                    if (moveArm(robot.arm, desiredAngle)) {
                        launch(robot.intake, robot.launcher, lookUpSpeed)
                    }
                } else {
                    robot.launcher.stopMotor()
                    autoState = AutoSequence.STOP
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
