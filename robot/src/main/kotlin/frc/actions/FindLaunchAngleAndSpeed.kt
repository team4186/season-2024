package frc.actions

import frc.vision.LimelightRunner

data class AngleAndSpeed(
    val angle: Double,
    val speed: Double,
)

private val DefaultAngleAndSpeed = AngleAndSpeed(17.9, 0.0)

private val lookupArray = arrayOf(
    AngleAndSpeed(17.0, -0.70),
    AngleAndSpeed(17.0, -0.70),
    AngleAndSpeed(21.0, -0.70),
    AngleAndSpeed(26.0, -0.70),
    AngleAndSpeed(28.0, -0.70),
    AngleAndSpeed(30.0, -0.70),
    AngleAndSpeed(32.0, -0.70),
    AngleAndSpeed(34.0, -0.75),
    AngleAndSpeed(37.0, -0.75),
    AngleAndSpeed(37.0, -0.80),
    AngleAndSpeed(38.5, -0.80),
    AngleAndSpeed(38.7, -0.85)
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