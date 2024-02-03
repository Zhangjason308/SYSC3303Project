package SYSC3303Project;

/**
 * ThreadMain.java
 * This class runs the application using the example "ElevatorEvents.csv" as a sample list of commands
 */
public class ThreadMain {
    public static void main(String[] args)
    {
        Thread floor, elevator, scheduler;
        Synchronizer synchronizer;

        // Create the client and server threads, passing each thread
        // a reference to the shared synchronizer object.
        synchronizer = new Synchronizer();
      


        // start the threads
        floor.start();
        elevator.start();
        scheduler.start();
    }
}

