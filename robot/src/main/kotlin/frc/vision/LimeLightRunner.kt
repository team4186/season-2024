package frc.vision

import edu.wpi.first.math.util.Units
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

class LimelightRunner(
    private val tableRing: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight-ring"),
    private val tableTag: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight-tag")
) {
    fun periodic() {
        SmartDashboard.putBoolean("Has Target Ring?", hasTargetRing)
        SmartDashboard.putBoolean("Has Target Tag?", hasTargetTag)
        SmartDashboard.putNumber("X Offset", xOffset)
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

    val xOffset: Double get() = tableRing.getEntry("tx").getDouble(0.0)
    val yOffset: Double get() = tableRing.getEntry("ty").getDouble(0.0)
    val tagArea: Double get() = tableRing.getEntry("ta").getDouble(0.0)

    val distance: Double
        get() {
            val targetDistance = 28.5 //inches away from limelight
            //at this target distance, the targetArea is 0.038 (3.8%) rough estimate needs adjusting
            val distance = targetDistance * tagArea / 0.038

            return if (hasTargetRing) distance else Double.NaN
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
}