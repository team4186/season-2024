package frc.autonomous

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.actions.*
import frc.robot.Robot
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    private var wait = Duration.ZERO

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
                        val inPosition = robot.arm.move(to = 17.0)
                        launch(robot.intake, robot.launcher, 0.70 * MAX_SPEED, true, inPosition)
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
                    wait = Duration.ZERO
                    robot.driveTrain.zeroEncoders()
                    autoState = AutoSequence.MOVEFORWARD
                }
            }

            AutoSequence.MOVEFORWARD -> {
                moveDistance(robot.driveTrain, -15.1)
                robot.intake.collect()
                if (robot.intake.hasSomething) {
                    robot.driveTrain.stop()
                    robot.intake.stopMotor()
                    autoState = AutoSequence.SHOOTSECONDNOTE
                }
            }

            AutoSequence.SHOOTSECONDNOTE -> {
                if (robot.intake.hasSomething) {
                    wait = Duration.ZERO
                    val (desiredAngle, lookUpSpeed) = findLaunchAngleAndSpeed(robot.limelightRunner)
                    val inPosition = moveArm(robot.arm, desiredAngle + 2.0)
                    launch(robot.intake, robot.launcher, lookUpSpeed, true, inPosition)
                } else {
                    wait += TICK
                    if (wait > 0.5.seconds) {
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

private val TICK = 20.milliseconds