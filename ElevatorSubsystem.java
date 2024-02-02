package SYSC3303Project;

/**
 * ElevatorSubsystemClient.java
 * This class is a elevator subsystem for an elevator real-time system. This subsystem
 * is a client thread that makes calls to the server thread, in which then it can perform
 * the Elevator movement.
 */
public class ElevatorSubsystem implements Runnable {

    private final TRAVEL_TIME_1 = 14.05;

    private float TRAVEL_TIME_2 = 20.74;

    private float TRAVEL_TIME_3 = 26;

    private float LOADING_TIME = 10.50;

    Synchronizer synchronizer;

    private int currentFloor;

    private Synchronizer synchronizer;

    private ArrayList<float> floorTimes = new ArrayList<>();
    public ElevatorSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
        floorTimes.add(STATIONARY);
        floorTimes.add(TRAVEL_TIME_1);
        floorTimes.add(TRAVEL_TIME_2);
        floorTimes.add(TRAVEL_TIME_3);
    }

    public void move(int currentFloor, int destinationFloor) {
        int floorsToMove = Math.abs(currentFloor - destinationFloor);
        Thread.sleep(floorTimes[floorsToMove]);
        Thread.sleep(LOADING_TIME);
    }
    public void run() {
        synchronizer.processElevatorRequest();


    }

}