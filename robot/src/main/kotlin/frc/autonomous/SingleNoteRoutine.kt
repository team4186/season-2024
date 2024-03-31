package frc.autonomous

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
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
        robot.arm.init()
    }

    override fun exit(robot: Robot) {
        robot.arm.stopMotor()
        robot.launcher.stopMotor()
        robot.driveTrain.stop()
    }

    override fun periodic(robot: Robot) {
        SmartDashboard.putString("Auto state", autoState.name)
        when (autoState) {
            AutoSequence.RESETARM -> {
                if (!resetArm(robot.arm)) {
                    autoState = AutoSequence.SHOOTPRELOAD
                }
            }

            AutoSequence.SHOOTPRELOAD -> {
                when {
                    robot.intake.hasSomething -> {
                        if (robot.arm.move(to = 17.0)) {
                            launch(robot.intake, robot.launcher, 0.70 * MAX_SPEED)
                        }
                    }

                    else -> {
                        autoState = AutoSequence.STOP
                    }
                }
            }

            AutoSequence.STOP -> {
                robot.intake.stopMotor()
                robot.launcher.stopMotor()
                robot.driveTrain.stop()
                robot.arm.stopMotor()
            }
        }
    }
}
