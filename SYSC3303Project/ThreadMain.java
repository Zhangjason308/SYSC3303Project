package SYSC3303Project;

public class ThreadMain {
    public static void main(String[] args)
    {
        Thread floor, elevator, scheduler;
        Synchronizer synchronizer;

        // Create the agent and barista threads, passing each thread
        // a reference to the shared counter object.
        synchronizer = new Synchronizer();
        floor = new Thread(new FloorSubsystem(synchronizer),"floor subsystem");
        elevator = new Thread(new ElevatorSubsystem(synchronizer),"elevator subsystem");
        scheduler = new Thread(new SchedulerSubsystem(synchronizer),"scheduler subsystem");


        // start the threads
        floor.start();
        elevator.start();
        scheduler.start();;
    }
}
