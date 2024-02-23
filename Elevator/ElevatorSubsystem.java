package SYSC3303Project.Elevator;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Synchronizer;

import static SYSC3303Project.DirectionEnum.DOWN;
import static SYSC3303Project.DirectionEnum.UP;




public class ElevatorSubsystem implements Runnable {
    private int currentFloor = 1; // Starting floor
    private DirectionEnum currentDirection = DirectionEnum.UP; // Default direction is UP
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
                        System.out.println("---------- ELEVATOR SUBSYSTEM: Received Command :" + command + " ----------");
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

    private void processCommand(FloorData command) {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Processing Command :" + command + " ----------");
        int destinationFloor = command.getDestinationFloor();
        int arrivalFloor = command.getArrivalFloor();

        // Determine the direction for the current command
        if (  arrivalFloor > currentFloor) {
            currentDirection = UP;
            moveToFloor(arrivalFloor,"boarding");
        } else if (arrivalFloor < currentFloor) {
            currentDirection = DOWN;
            moveToFloor(arrivalFloor,"boarding");
        } else {
            elevatorStateMachine.setState("Stopped");
            System.out.println("---------- ELEVATOR SUBSYSTEM: Already at floor " + currentFloor + " ----------");
            elevatorStateMachine.setState("DoorsOpen");
            System.out.println("---------- ELEVATOR SUBSYSTEM: Passengers boarding ----------");
            elevatorStateMachine.setState("DoorsClosed");
        }

        // Move to the destination floor based on the command

        moveToFloor(destinationFloor,"departing");
        elevatorStateMachine.setState("Idle");
    }

    private void moveToFloor(int destinationFloor, String action) {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Moving from floor " + currentFloor + " to floor " + destinationFloor + " ----------\n");

        if (currentFloor < destinationFloor) {
            goUp(destinationFloor);
        } else {
            goDown(destinationFloor);
        }

        System.out.println("---------- ELEVATOR SUBSYSTEM: Stopping at floor " + currentFloor + " ----------");
        elevatorStateMachine.setState("Stopped");
        elevatorStateMachine.setState("DoorsOpen");
        System.out.println("---------- ELEVATOR SUBSYSTEM: Passenger " + action + " ----------");
        elevatorStateMachine.setState("DoorsClosed");
    }

    private void goUp(int destinationFloor) {
        currentFloor = destinationFloor;
        elevatorStateMachine.setState("MovingUp");
        System.out.println("---------- ELEVATOR SUBSYSTEM: Going up, now at floor " + currentFloor + " ----------\n");
    }

    private void goDown(int destinationFloor) {
        currentFloor = destinationFloor;
        elevatorStateMachine.setState("MovingDown");
        System.out.println("---------- ELEVATOR SUBSYSTEM: Going down, now at floor " + currentFloor + " ----------\n");
    }
}