package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Elevator.StateMachine.States.ElevatorState;

/**
 * Concrete state class representing the state when Elevator is moving down.
 */
public class MovingDownState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("stop".equals(event)) {
            System.out.println("Elevator State Change [Arrive at Floor]: MovingDown -> Stopped");
            context.setState("Stopped");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: MovingDown\n");
    }
}
