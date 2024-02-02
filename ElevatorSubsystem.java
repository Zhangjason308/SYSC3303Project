package SYSC3303Project;

import java.util.ArrayList;

/**
 * ElevatorSubsystemClient.java
 * This class is a elevator subsystem for an elevator real-time system. This subsystem
 * is a client thread that makes calls to the server thread, in which then it can perform
 * the Elevator movement.
 */
public class ElevatorSubsystem implements Runnable {

    private final long STATIONARY = 0; // elevator is stationary

    private final long TRAVEL_TIME_1 = 1405; // travel time per floor (ms), moving 1 floor

    private final long TRAVEL_TIME_2 = 1037; // travel time per floor (ms), moving 2 floors

    private final long TRAVEL_TIME_3 = 867; // travel time per floor (ms), moving 3 floors

    private final long LOADING_TIME = 1050; // time to load passenger into elevator (ms)

    private Synchronizer synchronizer;

    private int currentFloor;

    private final long[] floorTimes = new long[4];

    private FloorData floorData;
    public ElevatorSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
        floorTimes[0] = (STATIONARY);
        floorTimes[1] = (TRAVEL_TIME_1);
        floorTimes[2] = (TRAVEL_TIME_2);
        floorTimes[3] = (TRAVEL_TIME_3);
    }

    public void move(int currentFloor, DirectionEnum direction, int destinationFloor) throws InterruptedException {
        int floorsToMove = Math.abs(currentFloor - destinationFloor);
        for(int i = 0; i < floorsToMove; i++) {
            System.out.println("Elevator is moving " + direction.toString() + "from floor " + currentFloor + " to floor " + destinationFloor);
            Thread.sleep(floorTimes[(floorsToMove)]); // Time it takes for elevator to move to a floor
            currentFloor = (direction == DirectionEnum.UP) ? currentFloor+1 : currentFloor-1;
            synchronizer.notifyFloor(currentFloor);
        }
        System.out.println("Elevator has stopped at floor " + destinationFloor + "\nElevator doors are opening");
        Thread.sleep(LOADING_TIME);
        System.out.println("Elevator doors have closed");

    }
    public void run() {
        while(true){
            floorData = synchronizer.processElevatorRequest();
        }




    }

}