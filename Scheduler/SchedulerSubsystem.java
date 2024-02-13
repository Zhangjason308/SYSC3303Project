package SYSC3303Project.Scheduler;

import SYSC3303Project.Synchronizer;

/**
 * SchedulerSubsystem.java
 * This class is a scheduler subsystem for an elevator real-time system. This subsystem
 * is a server thread that retrieves an elevator command from a queue, and then tells the elevator
 * to act on that command.
 */
public class SchedulerSubsystem implements Runnable {

    Synchronizer synchronizer;

    public SchedulerSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public Synchronizer getSynchronizer() {return synchronizer;}

    public void run() {
        // retrieves a command from the Queue and sends the request to the elevator
        while (synchronizer.getNumOfCallRetrieveCommand() < 3) {
            synchronizer.retrieveCommand();
        }

        System.out.println("retrieveCommand function was called " + synchronizer.getNumOfCallRetrieveCommand() + " times");
    }
}