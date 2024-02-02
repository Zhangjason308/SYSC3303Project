package SYSC3303Project;

import java.util.ArrayList;

public class Synchronizer {

    //Create separate locks/critical sections for various synchronous methods
    private ArrayList<FloorData> elevatorCommands = new ArrayList<>();
    private final int MAX_QUEUE_LENGTH = 4;

    private FloorData selectedCommand;


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
        notifyAll();
    }

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
        System.out.println("Elevator is moving to floor " + selectedCommand.getDestinationFloor());
        Thread.sleep(5000);
        selectedCommand = null;
        notifyAll();
        return selectedCommand.getDestinationFloor();
    }

}
