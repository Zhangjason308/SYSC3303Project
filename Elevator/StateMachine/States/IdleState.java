package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Elevator.StateMachine.States.ElevatorState;

/**
 * Concrete state class representing the state when Elevator's is not executing any command and is waiting for the scheduler to send a command.
 */
public class IdleState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        // If elevator is moving
        if ("accelerate".equals(event)) {
            System.out.println("Elevator State Change [Accelerating]: Idle -> Accelerating");
            context.setState("Accelerating");
        }
        // If requested arrival floor is at current floor of the elevator
        else if ("stop".equals(event)) {
            System.out.println("Elevator State Change [Stop]: Idle -> Stopped");
            context.setState("Stopped");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Idle\n");
    }
}
