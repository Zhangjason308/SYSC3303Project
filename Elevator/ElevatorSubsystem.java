package SYSC3303Project.Elevator;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Synchronizer;

import static SYSC3303Project.DirectionEnum.DOWN;
import static SYSC3303Project.DirectionEnum.UP;




public class ElevatorSubsystem implements Runnable {
    private int currentFloor = 1; // Starting floor
    private ElevatorStateMachine elevatorStateMachine;
    private Synchronizer synchronizer;


    public ElevatorSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
        this.elevatorStateMachine = new ElevatorStateMachine();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (synchronizer.hasSchedulerCommands()) {
                    FloorData command = synchronizer.getNextSchedulerCommand();
                    if (command != null) {
                        System.out.println("---------- ELEVATOR SUBSYSTEM: Received Command :" + command + " ----------\n");
                        processCommand(command);
                    }
                }

                Thread.sleep(100); // Sleep to reduce CPU usage when idle
            } catch (InterruptedException e) {
                System.out.println("---------- ELEVATOR SUBSYSTEM INTERRUPTED ---------- ");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processCommand(FloorData command) throws InterruptedException {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Processing Command :" + command + " ----------\n");
        int destinationFloor = command.getDestinationFloor();
        int arrivalFloor = command.getArrivalFloor();

        // Elevator moves to arrival floor to pickup passengers
        if (arrivalFloor != currentFloor) {
            moveToFloor(arrivalFloor,"boarding");
        }
        // Elevator is already at arrival floor to pickup passengers
        else {
            System.out.println("---------- ELEVATOR SUBSYSTEM: Already at floor " + currentFloor + " ----------\n");
            elevatorStateMachine.triggerEvent("stop");
            System.out.println("---------- ELEVATOR SUBSYSTEM: Passengers boarding ----------\n");
            elevatorStateMachine.triggerEvent("openDoors");
            System.out.println("---------- ELEVATOR SUBSYSTEM: Doors Closed ----------\n");
            elevatorStateMachine.triggerEvent("closeDoors");
        }

        // Move to the destination floor based on the command

        moveToFloor(destinationFloor,"departing");
        synchronized (synchronizer) {
            synchronizer.setDestinationSensor(true);
            synchronizer.notifyAll();
        }
        elevatorStateMachine.triggerEvent("idle");
    }

    private void moveToFloor(int destinationFloor, String action) {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Moving from floor " + currentFloor + " to floor " + destinationFloor + " ----------\n");

        if (currentFloor < destinationFloor) {
            goUp(destinationFloor);
        } else if (currentFloor > destinationFloor) {
            goDown(destinationFloor);
        }
        System.out.println("---------- ELEVATOR SUBSYSTEM: Stopping at floor " + currentFloor + " ----------\n");
        elevatorStateMachine.triggerEvent("stop");
        System.out.println("---------- ELEVATOR SUBSYSTEM: Passenger " + action + " ----------\n");
        elevatorStateMachine.triggerEvent("openDoors");
        System.out.println("---------- ELEVATOR SUBSYSTEM: Doors Closed ----------\n");
        elevatorStateMachine.triggerEvent("closeDoors");

    }

    private void goUp(int destinationFloor) {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Arrived at floor " + destinationFloor + " ----------\n");
        elevatorStateMachine.triggerEvent("moveUp");
        currentFloor = destinationFloor;
    }

    private void goDown(int destinationFloor) {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Arrived at floor " + destinationFloor + " ----------\n");
        elevatorStateMachine.triggerEvent("moveDown");
        currentFloor = destinationFloor;
    }
}