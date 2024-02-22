package SYSC3303Project.Elevator;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Synchronizer;

import java.util.Comparator;
import java.util.PriorityQueue;

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
                        System.out.println("ElevatorSubSystem: Received Command :"+command);
                        processCommand(command);
                    }
                }

                Thread.sleep(100); // Sleep to reduce CPU usage when idle
            } catch (InterruptedException e) {
                System.out.println("ElevatorSubsystem interrupted.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processCommand(FloorData command) {
        System.out.println("ElevatorSubsystem: Processing command: " + command);
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
            System.out.println("ElevatorSubsystem: Already at floor " + currentFloor);
            elevatorStateMachine.setState("DoorsOpen");
            System.out.println("ElevatorSubsystem: Passengers boarding");
            elevatorStateMachine.setState("DoorsClosed");
        }

        // Move to the destination floor based on the command

        moveToFloor(destinationFloor,"departing");
        elevatorStateMachine.setState("Idle");
    }

    private void moveToFloor(int destinationFloor, String action) {
        System.out.println("ElevatorSubsystem: Moving from floor " + currentFloor + " to floor " + destinationFloor);

        if (currentFloor < destinationFloor) {
            elevatorStateMachine.setState("MovingUp");
            currentFloor = destinationFloor;
            System.out.println("ElevatorSubsystem: Going up, now at floor " + currentFloor);




        } else {
            elevatorStateMachine.setState("MovingDown");
            currentFloor = destinationFloor;
            System.out.println("ElevatorSubsystem: Going down, now at floor " + currentFloor);


        }

        System.out.println("ElevatorSubsystem: Stopping at floor " + currentFloor);
        elevatorStateMachine.setState("Stopped");
        elevatorStateMachine.setState("DoorsOpen");
        System.out.println("ElevatorSubsystem: Passenger " + action);
        elevatorStateMachine.setState("DoorsClosed");
    }

    private void goUp() {
        currentFloor++;
        elevatorStateMachine.setState("MovingUp");
        System.out.println("ElevatorSubsystem: Going up, now at floor " + currentFloor);
    }

    private void goDown() {
        currentFloor--;
        elevatorStateMachine.setState("MovingDown");
        System.out.println("ElevatorSubsystem: Going down, now at floor " + currentFloor);
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public Synchronizer getSynchronizer() {
        return synchronizer;
    }
}