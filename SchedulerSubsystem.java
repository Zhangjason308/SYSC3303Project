package SYSC3303Project;

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

        System.out.println("retrieveComman function was called " + synchronizer.getNumOfCallRetrieveCommand() + " times");
    }
}