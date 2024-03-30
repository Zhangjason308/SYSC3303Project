package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

/**
 * Concrete state class representing the state when Elevator's doors are closed.
 */
public class DoorsClosedState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        // If elevator is moving
        if ("accelerate".equals(event)) {
            System.out.println("Elevator State Change [Accelerating]: DoorsClosed -> Accelerating");
            context.setState("Accelerating");
        }
        else {
            System.out.println("Elevator State Change [Signaling Scheduler for New Request]: DoorsClosed -> Idle");
            context.setState("Idle");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: DoorsClosed\n");
    }
}
