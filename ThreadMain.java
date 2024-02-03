package SYSC3303Project;

public class ThreadMain {
    public static void main(String[] args)
    {
        Thread floor, elevator, scheduler;
        Synchronizer synchronizer;

        // Create the agent and barista threads, passing each thread
        // a reference to the shared counter object.
        synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv");
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(synchronizer);
        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(synchronizer);
        floor = new Thread(floorSubsystem, "floor subsystem");
        elevator = new Thread(elevatorSubsystem, "elevator subsystem");
        scheduler = new Thread(schedulerSubsystem, "scheduler subsystem");


        // start the threads
        floor.start();
        elevator.start();
        scheduler.start();
    }
}

