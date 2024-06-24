package frc.autonomous

import frc.robot.Robot
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TwoNoteAutonTest {
    @Test
    fun `two note auton works`() {
        var intakeHasSomething = true
        var armPos = 17.0

        val robot = mockk<Robot>(relaxed = true) {
            every { intake.hasSomething } answers { intakeHasSomething }
            every { arm.needReset } returns false
            every { arm.move(to = armPos) } answers { true }
        }

        val subject = TwoNotesRoutine()
        subject.init(robot)
        assertEquals(TwoNotesRoutine.AutoSequence.RESETARM, subject.autoState)
        subject.periodic(robot)
        assertEquals(TwoNotesRoutine.AutoSequence.SHOOTPRELOAD, subject.autoState)
        intakeHasSomething = false
        subject.periodic(robot)
        //  assertEquals(TwoNotesRoutine.AutoSequence.ARMDOWN, subject.autoState)

    }
}