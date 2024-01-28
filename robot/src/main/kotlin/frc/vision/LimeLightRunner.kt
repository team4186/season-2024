package frc.vision

import edu.wpi.first.math.util.Units
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

class LimelightRunner(
    private val table: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight")
) {
    fun periodic() {
        SmartDashboard.putBoolean("Has Target?", hasTarget)
        SmartDashboard.putNumber("X Offset", xOffset)
        //SmartDashboard.putNumber("Y Offset", yOffset)
        SmartDashboard.putNumber("% of Image", tagArea)
        SmartDashboard.putNumber("Distance", Units.metersToInches(distance))
    }

    val hasTarget: Boolean
        get() {
            return table.getEntry("tv").getDouble(0.0) > 0.0
        }

    val xOffset: Double get() = table.getEntry("tx").getDouble(0.0)
    val yOffset: Double get() = table.getEntry("ty").getDouble(0.0)
    val tagArea: Double get() = table.getEntry("ta").getDouble(0.0)

    val distance: Double
        get() {
            val targetDistance = 28.5 //inches away from limelight
            //at this target distance, the targetArea is 0.038 (3.8%) rough estimate needs adjusting
            val distance = targetDistance * tagArea / 0.038

            return if (hasTarget) distance else Double.NaN
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
        table.getEntry("ledMode").setValue(if (mode) 3.0 else 1.0)
    }
}