package SYSC3303Project;

import SYSC3303Project.Floor.FloorData;
import java.util.LinkedList;
import java.util.Queue;

public class Synchronizer {
    private int currentFloor = 1; // Default floor before any movement

    // Queue for commands from floors to the scheduler
    private final Queue<FloorData> floorCommandQueue = new LinkedList<>();
    // Queue for commands from the scheduler to the elevator
    private final Queue<FloorData> schedulerCommandQueue = new LinkedList<>();

    private boolean destinationSensor = false;

    // Method to add a command from a floor
    public synchronized void addFloorCommand(FloorData command) {
        floorCommandQueue.offer(command);
        notifyAll(); // Notify the scheduler that a new command is available
    }

    // Method for the scheduler to retrieve the next command from a floor
    public synchronized FloorData getNextFloorCommand() throws InterruptedException {
        while (floorCommandQueue.isEmpty()) {
            wait(); // Wait until a command is available
        }
        return floorCommandQueue.poll();
    }

    // Method for the scheduler to add a command for the elevator
    public synchronized void addSchedulerCommand(FloorData command) {
        schedulerCommandQueue.offer(command);
        notifyAll(); // Notify the elevator subsystem that a new command is available
    }

    // Method for the elevator to retrieve the next command from the scheduler
    public synchronized FloorData getNextSchedulerCommand() throws InterruptedException {
        while (schedulerCommandQueue.isEmpty()) {
            wait(); // Wait until a command is available
        }
        return schedulerCommandQueue.poll();
    }

    // Utility methods if needed for checking queues are not empty (optional)
    public synchronized boolean hasFloorCommands() {
        return !floorCommandQueue.isEmpty();
    }

    public synchronized boolean hasSchedulerCommands() {
        return !schedulerCommandQueue.isEmpty();
    }

    public void setDestinationSensor(boolean hasArrived) {
        destinationSensor = hasArrived;
    }

    public boolean getDestinationSensor() {
        return destinationSensor;
    }
    // Method for the SchedulerSubsystem to get the current floor
    public synchronized int getCurrentFloor() {
        return this.currentFloor;
    }
}
