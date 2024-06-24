package frc.actions

import edu.wpi.first.math.controller.PIDController
import frc.subsystems.DriveTrainSubsystem
import frc.vision.LimelightRunner
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlignToTargetTest {

    @Test
    fun `when target is not visible, do nothing`() {

        val drive = mockk<DriveTrainSubsystem>(relaxed = true)
        val vision = mockk<LimelightRunner>(relaxed = true) {
            every { tagxOffset } returns 0.0
            every { hasTargetTag } returns false
            every { hasTargetRing } returns false
        }
        val pid = PIDController(0.005, 0.0, 0.0) //SpeakerTurnPID values

        assertFalse(
                alignToTarget(
                        forward = 0.0,
                        turnController = pid,
                        drive = drive,
                        vision = vision,
                        offset = 0.0
                )
        )
    }

    @Test
    fun `when target is directly at the front, do nothing`() {
        val drive = mockk<DriveTrainSubsystem>(relaxed = true)
        val vision = mockk<LimelightRunner>(relaxed = true) {
            every { tagxOffset } returns 0.0
            every { hasTargetTag } returns true
            every { hasTargetRing } returns false
        }
        val pid = PIDController(0.005, 0.0, 0.0) //SpeakerTurnPID values

        assertTrue(
                alignToTarget(
                        forward = 0.0,
                        turnController = pid,
                        drive = drive,
                        vision = vision,
                        offset = 0.0
                )
        )
        verifyOrder {
            drive.arcade(-0.0, -0.0, false)
        }
    }

    @Test
    fun `when tag is to the left, turn left`() {
        var xOffset = -28.0
        val drive = mockk<DriveTrainSubsystem>(relaxed = true)
        val vision = mockk<LimelightRunner>(relaxed = true) {
            every { tagxOffset } answers { xOffset }
            every { hasTargetTag } returns true
            every { hasTargetRing } returns false
        }
        val pid = PIDController(0.005, 0.0, 0.0) //SpeakerTurnPID values

        fun callAlign() = alignToTarget(
                forward = 0.0,
                turnController = pid,
                drive = drive,
                vision = vision,
                offset = 0.0
        )

        callAlign()

        verifyOrder {
            drive.arcade(-0.0, -0.14, false)
        }

        xOffset = -14.0

        callAlign()

        verifyOrder {
            drive.arcade(-0.0, -0.07, false)
        }

        xOffset = 7.0

        callAlign()

        verifyOrder {
            drive.arcade(-0.0, 0.035, false)
        }

        xOffset = 0.0

        callAlign()

        verifyOrder {
            drive.arcade(-0.0, -0.0, false)
        }
    }
}