package SYSC3303Project;

import java.util.ArrayList;

public class Synchronizer {
    private ArrayList<FloorData> elevatorCommands = new ArrayList<>();
    private final int MAX_QUEUE_LENGTH = 4;

    private int destinationFloor;

    private int pickupFloor;

    private DirectionEnum direction = DirectionEnum.STILL;

    private int currentFloor;

    private boolean hasArrived = false;

    private boolean requestElevator = false;
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
    public synchronized FloorData retrieveCommand() {
        while (elevatorCommands.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        FloorData command = elevatorCommands.remove(0);
        notifyAll();
        return command;

    }

    public synchronized void requestElevator(DirectionEnum direction, int destinationFloor, int pickupFloor) {
        // if the elevator is processing a request, wait
        while (requestElevator) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        requestElevator = true; // elevator is now processing request
        this.direction = direction; // flags set for the request direction
        this.destinationFloor = destinationFloor; // flags set for the request destination floor
        this.pickupFloor = pickupFloor; // flags set for the request pickup floor
        notifyAll();
    }
    public synchronized void processElevatorRequest() {}
    public synchronized void sendElevatorToScheduler(int floor, int button, int requestNumber) {}
    public synchronized void sendSchedulerToFloor() {}
}
