package frc.commands.drive

import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertEquals

class TeleopDriveTest {
    @Test
    fun `when throttling forward the drivetrain should go forward`() {
        val subject = TeleopDrive(
            inputTurn = { 0.0 },
            inputThrottle = { 1.0 }, // Throttling Forward
            inputYaw = { 0.0 },
            drive = { forward, side, turn ->
                // Asserting behavior is correct
                assertEquals(1.0, forward)
                assertEquals(0.0, side.absoluteValue) // can be -0.0
                assertEquals(0.0, turn.absoluteValue) // can be -0.0
            },
            stop = {},
            shouldAttenuate = { false },
            forward = { -1.0 },
        )

        // Call execute to run the process
        subject.execute()
    }

    @Test
    fun `when throttling backward the drivetrain should go reverse`() {
        val subject = TeleopDrive(
            inputTurn = { 0.0 },
            inputThrottle = { -1.0 }, // Throttling Backward
            inputYaw = { 0.0 },
            drive = { forward, side, turn ->
                // Asserting behavior is correct
                assertEquals(-1.0, forward)
                assertEquals(0.0, side.absoluteValue) // can be -0.0
                assertEquals(0.0, turn.absoluteValue) // can be -0.0
            },
            stop = {},
            shouldAttenuate = { false },
            forward = { -1.0 },
        )

        // Call execute to run the process
        subject.execute()
    }

    @Test
    fun `when strafing the drivetrain should strafe`() {
        TeleopDrive(
            inputTurn = { 0.0 },
            inputThrottle = { 0.0 },
            inputYaw = { 1.0 },
            drive = { forward, side, turn ->
                // Asserting behavior is correct
                assertEquals(0.0, forward.absoluteValue)
                assertEquals(1.0, side)
                assertEquals(0.0, turn.absoluteValue) // can be -0.0
            },
            stop = {},
            shouldAttenuate = { false },
            forward = { -1.0 },
        ).execute()

        TeleopDrive(
            inputTurn = { 0.0 },
            inputThrottle = { 0.0 },
            inputYaw = { -1.0 },
            drive = { forward, side, turn ->
                // Asserting behavior is correct
                assertEquals(0.0, forward.absoluteValue)
                assertEquals(-1.0, side)
                assertEquals(0.0, turn.absoluteValue) // can be -0.0
            },
            stop = {},
            shouldAttenuate = { false },
            forward = { -1.0 },
        ).execute()
    }

    @Test
    fun `when turning the drivetrain should turn`() {
        TeleopDrive(
            inputTurn = { 1.0 },
            inputThrottle = { 0.0 },
            inputYaw = { 0.0 },
            drive = { forward, side, turn ->
                // Asserting behavior is correct
                assertEquals(0.0, forward.absoluteValue)
                assertEquals(0.0, side.absoluteValue)
                assertEquals(-1.0, turn)
            },
            stop = {},
            shouldAttenuate = { false },
            forward = { -1.0 },
        ).execute()

        TeleopDrive(
            inputTurn = { -1.0 },
            inputThrottle = { 0.0 }, // Throttling Forward
            inputYaw = { 0.0 },
            drive = { forward, side, turn ->
                // Asserting behavior is correct
                assertEquals(0.0, forward.absoluteValue)
                assertEquals(0.0, side.absoluteValue)
                assertEquals(1.0, turn)
            },
            stop = {},
            shouldAttenuate = { false },
            forward = { -1.0 },
        ).execute()
    }
}