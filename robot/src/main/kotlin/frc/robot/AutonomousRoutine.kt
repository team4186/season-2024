package frc.robot

/**
 * [AutonomousRoutine] is the interface to implement autonomous code
 */
interface AutonomousRoutine {
    /**
     * [init] is called in Robot.autonomousInit()
     *
     * Use it to reset the state or initialize the routine
     */
    fun init(robot: Robot)

    /**
     * [periodic] is called in Robot.autonomousPeriodic() every 20ms
     *
     * Do not use long-running or blocking operations as it will hold the program.
     */
    fun periodic(robot: Robot)

    /**
     * [exit] is called in Robot.autonomousExit
     *
     * Use it to clean up after autonomous phase
     */
    fun exit(robot: Robot)

    companion object {
        val Empty = object : AutonomousRoutine {
            override fun init(robot: Robot) {}
            override fun periodic(robot: Robot) {}
            override fun exit(robot: Robot) {}
        }
    }
}
