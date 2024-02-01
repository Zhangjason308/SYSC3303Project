package SYSC3303Project;

/**
 * ElevatorSubsystemClient.java
 * This class is a elevator subsystem for an elevator real-time system. This subsystem
 * is a client thread that makes calls to the server thread, in which then it can perform
 * the Elevator movement.
 */
public class ElevatorSubsystem implements Runnable {

    private int TIME_FOR_1_FLOOR;

    private int TIME_FOR_2_FLOOR;

    private int TIME_FOR_3_FLOOR;

    private int TIME_FOR_1_PERSON;

    private int TIME_FOR_2_PERSON;

    private int TIME_FOR_3_PERSON;

    private int TIME_FOR_4_PERSON;
    Synchronizer synchronizer;

    private int currentFloor;
    public ElevatorSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
        
    }

    public void move(int currentFloor, int destinationFloor) {
        int floorsToMove = Math.abs(currentFloor - destinationFloor);
        Thread.sleep();
    }
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

    }

}