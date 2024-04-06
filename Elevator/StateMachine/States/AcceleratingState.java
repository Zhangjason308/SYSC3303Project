package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

/**
 * Concrete state class representing the state when Elevator is accelerating.
 */
public class AcceleratingState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("cruise".equals(event)) {
            System.out.println("Elevator State Change [Cruise]: Accelerating -> Cruising");
            context.setState("Cruising");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Accelerating\n");
    }
}


