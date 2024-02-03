package SYSC3303Project;

import java.util.ArrayList;

/**
 * Synchronizer.java
 * This class is a thread-safe synchronizer for an elevator real-time system. This contains various methods for
 * the client and server threads to perform.
 */
public class Synchronizer {

    private ArrayList<FloorData> elevatorCommands = new ArrayList<>();
    private final int MAX_QUEUE_LENGTH = 4;
    private FloorData selectedCommand;
    public FloorData getSelectedCommand() {return selectedCommand;}
    private int numOfCallRetrieveCommand = 0;
    private int numOfCallProcessElevatorRequest = 0;
    public int getNumOfCallRetrieveCommand() {return numOfCallRetrieveCommand;}
    public int getNumOfCallProcessElevatorRequest() {return numOfCallProcessElevatorRequest;}
    public int getMAX_QUEUE_LENGTH() {return MAX_QUEUE_LENGTH;}

    // Floor sends input to the Synchronizer if the queue is not full
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

    public ArrayList<FloorData> getElevatorCommands() {
        return elevatorCommands;
    }

    // Scheduler selects a command from the Queue if there is a command in the queue
    public synchronized void retrieveCommand () {
        while (elevatorCommands.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        selectedCommand =  elevatorCommands.remove(0);
        numOfCallRetrieveCommand++;
        notifyAll();
    }

    // Elevator processes the command if there is a command retrieved
    public synchronized int processElevatorRequest() throws InterruptedException {
        while (selectedCommand == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Elevator is moving to floor " + selectedCommand.getArrivalFloor() + " to pickup passenger");
        Thread.sleep(2000);
        System.out.println("Elevator has arrived at floor " + selectedCommand.getArrivalFloor());
        Thread.sleep(2000);
        System.out.println("Elevator is moving to floor " + selectedCommand.getDestinationFloor());
        Thread.sleep(5000);
        int destination = selectedCommand.getDestinationFloor();
        selectedCommand = null;
        numOfCallProcessElevatorRequest++;
        notifyAll();
        return destination;
    }
}
