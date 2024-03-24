package frc.subsystems

import com.revrobotics.CANSparkMax
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.Encoder
import frc.actions.resetArm
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArmTest {

    @Test
    fun `when I set a position the motor should actuate`() {
        var bottomLimitState = true
        var encoderValue = 0.0

        val bottomLimit = mockk<DigitalInput> {
            every { get() } answers { bottomLimitState }
        }
        val topLimit = mockk<DigitalInput>() {
            every { get() } returns true
        }
        val encoder = mockk<Encoder>(relaxed = true) {
            every { reset() } answers { encoderValue = 0.0 }
            every { distance } answers { encoderValue }
        }
        val motor = mockk<CANSparkMax>(relaxed = true)
        val pid = PIDController(0.005, 0.0, 0.0)

        val subject = Arm(bottomLimit, topLimit, encoder, motor, pid)

        subject.reset()

        assertTrue(resetArm(subject))
        verifyOrder {
            motor.set(-DEFAULT_FREE_MOVE_SPEED)
        }


        bottomLimitState = false
        assertFalse(resetArm(subject))
        verifyOrder {
            motor.stopMotor()
        }

        subject.move(to = 17.9)
        verifyOrder {
            motor.set(0.0895)
        }

        encoderValue = 17.9
        assertTrue(subject.move(to = 17.9))
        verifyOrder {
            motor.stopMotor()
        }
    }
}