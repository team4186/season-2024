package frc.actions

import frc.vision.LimelightRunner

data class AngleAndSpeed(
    val angle: Double,
    val speed: Double,
)

private val DefaultAngleAndSpeed = AngleAndSpeed(17.9, 0.0)

const val MAX_SPEED = 5000
const val TEST_SPEED = MAX_SPEED * 0.20

private val lookupArray = arrayOf(
    AngleAndSpeed(17.0, 0.70 * MAX_SPEED),
    AngleAndSpeed(17.0, 0.70 * MAX_SPEED),
    AngleAndSpeed(21.0, 0.70 * MAX_SPEED),
    AngleAndSpeed(26.0, 0.70 * MAX_SPEED),
    AngleAndSpeed(28.0, 0.70 * MAX_SPEED),
    AngleAndSpeed(30.0, 0.70 * MAX_SPEED),
    AngleAndSpeed(32.0, 0.70 * MAX_SPEED),
    AngleAndSpeed(37.0, 0.75 * MAX_SPEED),
    AngleAndSpeed(34.0, 0.75 * MAX_SPEED),
    AngleAndSpeed(37.0, 0.80 * MAX_SPEED),
    AngleAndSpeed(38.5, 0.80 * MAX_SPEED),
    AngleAndSpeed(38.7, 0.85 * MAX_SPEED)
)

fun findLaunchAngleAndSpeed(
    limelight: LimelightRunner,
): AngleAndSpeed {
    return when {
        limelight.hasTargetTag -> {
            val distanceToTag = limelight.distance
            val roundedDistance = limelight.lookupTableRound(distanceToTag)
            println("roundedDistance: $roundedDistance")
            lookupArray[roundedDistance]
        }

        else -> DefaultAngleAndSpeed
    }

}