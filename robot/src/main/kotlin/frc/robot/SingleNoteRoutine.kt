package frc.robot

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

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
        ARMDOWN,
        MOVEFORWARD,
        SHOOTSECONDNOTE,
        STOP
    } //MOVEFORWARD needs to intake as well

    private var autoState = AutoSequence.RESETARM
    private var lookUpSpeed = 0.0
    private var desiredAngle = 0.0

    override fun init(robot: Robot) {
        autoState = AutoSequence.RESETARM
    }

    override fun exit(robot: Robot) {
        robot.launcherArmMotors.stopMotor()
        robot.launcher.stopMotor()
        robot.driveTrainSubsystem.stop()
    }

    override fun periodic(robot: Robot) {
        when (autoState) {
            AutoSequence.RESETARM -> {
                robot.armDown()
                if (robot.launcherBottomLimit.get()) {
                    autoState = AutoSequence.SHOOTPRELOAD
                }
            }

            AutoSequence.SHOOTPRELOAD -> {
                if (robot.limelightRunner.hasTargetTag) {
                    val distanceToTag = robot.limelightRunner.distance
                    val roundedDistance = robot.limelightRunner.lookupTableRound(distanceToTag)
                    desiredAngle = robot.lookupArray[(roundedDistance)][1]
                    lookUpSpeed = robot.lookupArray[(roundedDistance)][0]
                }

                when {
                    !robot.intakeSlot.get() -> {
                        robot.felipeSetAngle(robot.convertToTicks(17.0))
                        robot.launch(-0.70 + 0.01, -0.70)
                    }

                    else -> {
                        robot.launcher.stopMotor()
                        //                    autoState = autoSequence.ARMDOWN
                        autoState = when {
                            SmartDashboard.getBoolean(
                                "TwoPieceforTrue",
                                true
                            ) -> AutoSequence.ARMDOWN

                            else -> AutoSequence.STOP
                        }
                    }
                }
            }

            AutoSequence.ARMDOWN -> {
                robot.felipeSetAngle(robot.convertToTicks(0.0))
                if (robot.launcherBottomLimit.get()) {
                    robot.launcherArmMotors.stopMotor()
                    autoState = AutoSequence.MOVEFORWARD
                }
            }

            AutoSequence.MOVEFORWARD -> {
                robot.driveTrainSubsystem.arcade(1.0, 0.0, false)
                robot.collect()
                if (!robot.intakeSlot.get()) {
                    robot.driveTrainSubsystem.stop()
                    autoState = AutoSequence.SHOOTSECONDNOTE
                }
            }

            AutoSequence.SHOOTSECONDNOTE -> {
                if (robot.limelightRunner.hasTargetTag) {
                    val distanceToTag = robot.limelightRunner.distance
                    val roundedDistance = robot.limelightRunner.lookupTableRound(distanceToTag)
                    desiredAngle = robot.lookupArray[(roundedDistance)][1]
                    lookUpSpeed = robot.lookupArray[(roundedDistance)][0]
                }

                if (!robot.intakeSlot.get()) {
                    robot.felipeSetAngle(robot.convertToTicks(desiredAngle))
                    robot.launch(lookUpSpeed + 0.01, lookUpSpeed)
                } else {
                    robot.launcher.stopMotor()
                    autoState = AutoSequence.STOP
                }
            }

            AutoSequence.STOP -> {
                robot.launcherArmMotors.stopMotor()
                robot.launcher.stopMotor()
                robot.driveTrainSubsystem.stop()
            }
        }
    }
}
