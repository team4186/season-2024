package frc.vision

import edu.wpi.first.math.util.Units
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlin.math.roundToInt
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

    fun lookupTableRound(distanceToTag: Double): Int =
        ((distanceToTag - 36.37) / 12.0)
            .roundToInt()
            .coerceIn(0, 11)
}