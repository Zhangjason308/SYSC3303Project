package SYSC3303Project;

public class SchedulerSubsystem implements Runnable {

    Synchronizer synchronizer;

    public SchedulerSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public void run() {
        // retrieves a command from the Queue and sends the request to the elevator
        while (true) {
            synchronizer.retrieveCommand();
        }
    }
}