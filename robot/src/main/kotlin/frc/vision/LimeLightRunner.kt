package frc.vision

import edu.wpi.first.math.util.Units
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlin.math.tan

class LimelightRunner(
    private val tableRing: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight-ring"),
    private val tableTag: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight-tag")
) {
    fun periodic() {
        SmartDashboard.putBoolean("Has Target Ring?", hasTargetRing)
        SmartDashboard.putBoolean("Has Target Tag?", hasTargetTag)
        SmartDashboard.putNumber("X Offset", tagxOffset)
        //SmartDashboard.putNumber("Y Offset", yOffset)
        SmartDashboard.putNumber("% of Image", tagArea)
        SmartDashboard.putNumber("Distance", Units.metersToInches(distance))
    }


    val hasTargetRing: Boolean
        get() {
            return tableRing.getEntry("tv").getDouble(0.0) > 0.0
        }


    val hasTargetTag: Boolean
        get() {
            return tableTag.getEntry("tv").getDouble(0.0) > 0.0

        }

    val ringxOffset: Double get() = tableRing.getEntry("tx").getDouble(0.0)
    val ringyOffset: Double get() = tableRing.getEntry("ty").getDouble(0.0)
    val ringArea: Double get() = tableRing.getEntry("ta").getDouble(0.0)

    val tagxOffset: Double get() = tableTag.getEntry("tx").getDouble(0.0)
    val tagyOffset: Double get() = tableTag.getEntry("ty").getDouble(0.0)
    val tagArea: Double get() = tableTag.getEntry("ta").getDouble(0.0)

    //returns distance to AprilTag
    val distance: Double
        get() {
            //33.75 is height in inches of AprilTag off floor
            //21.41 degrees is mounting angle of limelight
            val angleInRadians = Math.toRadians((21.41 + tagyOffset))
            val distance = 33.75 / tan(angleInRadians)

            return if (hasTargetTag) distance else Double.NaN
        }

    //Subject to change
//  override val distance: Double
//        get() {
//            val targetHeight = 2.6416
//            val cameraHeight = 0.81 //Subject to change
//
//            val cameraAngle = 50.0 //Subject to change (ish)
//
//            val targetAngle: Double = yOffset
//            val totalAngleRad = Units.degreesToRadians(cameraAngle + targetAngle)
//            val distance = (targetHeight - cameraHeight) / tan(totalAngleRad)
//
//            return if (hasTarget) distance else Double.NaN
//        }


    fun setLight(mode: Boolean) {
        tableRing.getEntry("ledMode").setValue(if (mode) 3.0 else 1.0)
    }

    fun lookupTableRound(distanceToTag: Double): Int {
        val roundedNum = ((distanceToTag - 36.37) / 12.0)
        when {
            roundedNum > 12.0 -> return 11
            roundedNum < 12.0 && roundedNum >= 11.0 -> return 11
            roundedNum < 11.0 && roundedNum >= 10.0 -> return 11
            roundedNum < 10.0 && roundedNum >= 9.5 -> return 10
            roundedNum < 9.5 && roundedNum >= 9.0 -> return 9
            roundedNum < 9.0 && roundedNum >= 8.5 -> return 9
            roundedNum < 8.5 && roundedNum >= 8.0 -> return 8
            roundedNum < 8.0 && roundedNum >= 7.5 -> return 8
            roundedNum < 7.5 && roundedNum >= 7.0 -> return 7
            roundedNum < 7.0 && roundedNum >= 6.5 -> return 7
            roundedNum < 6.5 && roundedNum >= 6.0 -> return 6
            roundedNum < 6.0 && roundedNum >= 5.5 -> return 6
            roundedNum < 5.5 && roundedNum >= 5.0 -> return 5
            roundedNum < 5.0 && roundedNum >= 4.5 -> return 5
            roundedNum < 4.5 && roundedNum >= 4.0 -> return 4
            roundedNum < 4.0 && roundedNum >= 3.5 -> return 4
            roundedNum < 3.5 && roundedNum >= 3.0 -> return 3
            roundedNum < 3.0 && roundedNum >= 2.5 -> return 3
            roundedNum < 2.5 && roundedNum >= 2.0 -> return 2
            roundedNum < 2.0 && roundedNum >= 1.5 -> return 2
            roundedNum < 1.5 && roundedNum >= 1.0 -> return 1
            roundedNum < 1.0 && roundedNum >= 0.5 -> return 1
            else -> return 0
        }
    }
}