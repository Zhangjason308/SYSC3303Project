package SYSC3303Project;

import java.util.ArrayList;

public class Synchronizer {

    //Create separate locks/critical sections for various synchronous methods
    private ArrayList<FloorData> elevatorCommands = new ArrayList<>();
    private final int MAX_QUEUE_LENGTH = 4;

    private int destinationFloor;

    private int pickupFloor;

    private DirectionEnum direction = DirectionEnum.STILL;

    private int currentFloor;

    private String time;

    private boolean hasArrived = false;

    private boolean notifiedFloorSensor = false;

    private boolean handlingRequest = false;
    public synchronized void sendInputLine(FloorData floorData) {
        while (elevatorCommands.size()  >= MAX_QUEUE_LENGTH) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        elevatorCommands.add(floorData);
        notifyAll();
    }

    public synchronized FloorData retrieveCommandRequestElevator () {
        while (elevatorCommands.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        FloorData command = elevatorCommands.remove(0);
        this.direction = command.getDirection(); // flags set for the request direction
        this.destinationFloor = command.getDestinationFloor(); // flags set for the request destination floor
        this.pickupFloor = command.getArrivalFloor(); // flags set for the request pickup floor
        this.time = command.getTime();
        notifyAll();
        return command;
    }
    public synchronized FloorData processElevatorRequest() {
        while (handlingRequest) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            handlingRequest = true; // elevator is now processing request

        }
        notifyAll();
        return new FloorData(time, pickupFloor, direction, destinationFloor);


    }
    public synchronized void notifyFloor(int currentFloor) {
        // Elevator arrives at a floor sensor and updates the current floor flag
        while (!notifiedFloorSensor) { // while elevator is moving and has not reached a floor sensor
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.currentFloor = currentFloor;
        notifyAll();
    }
    public synchronized int retrieveElevatorNotification() {
        // Notifies the scheduler which floor sensor the elevator has just passed
        while (!notifiedFloorSensor) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifiedFloorSensor = false;
        notifyAll();
        return currentFloor;
    }
}
