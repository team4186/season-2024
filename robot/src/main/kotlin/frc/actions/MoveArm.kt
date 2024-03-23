package frc.actions

import frc.subsystems.Arm

const val FULL_UP = Double.MAX_VALUE
const val FULL_DOWN = -Double.MAX_VALUE

fun moveArm(arm: Arm, position: Double): Boolean {
    if (arm.needReset) {
        arm.moveDown()
        return true
    }

    return arm.move(position)
}