package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

/**
 * Concrete state class representing the state when Elevator is cruising.
 */
public class CruisingState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("decelerate".equals(event)) {
            System.out.println("Elevator State Change [decelerate]: Cruising -> Decelerating");
            context.setState("Decelerating");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Cruising\n");
    }
}
