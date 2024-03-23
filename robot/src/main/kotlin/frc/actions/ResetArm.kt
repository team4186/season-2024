package frc.actions

import frc.subsystems.Arm

/**
 * [resetArm] will drive the motors until the bottom limit switch trigger.
 *
 * @param arm subject to actuate
 * @return true if reset is complete
 */
fun resetArm(arm: Arm): Boolean {
    if (arm.needReset) {
        arm.moveDown()
    }
    return arm.needReset
}