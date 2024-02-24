package SYSC3303Project;

import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Floor.FloorSubsystem;
import SYSC3303Project.Scheduler.SchedulerSubsystem;

/**
 * ThreadMain.java
 * This class runs the application using the example "ElevatorEvents.csv" as a sample list of commands
 */
public class ThreadMain {
    public static void main(String[] args) throws InterruptedException {
        Thread floor, elevator, scheduler;
        Synchronizer synchronizer;

        // Create the client and server threads, passing each thread
        // a reference to the shared synchronizer object.
        synchronizer = new Synchronizer();
        floor = new Thread(new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv"),"floor subsystem");
        elevator = new Thread(new ElevatorSubsystem(synchronizer),"elevator subsystem");
        scheduler = new Thread(new SchedulerSubsystem(synchronizer),"scheduler subsystem");


        // start the threads
        floor.start();
        elevator.start();
        scheduler.start();;
    }
}
