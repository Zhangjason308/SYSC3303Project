package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Elevator.StateMachine.States.ElevatorState;

/**
 * Concrete state class representing the state when Elevator's is not executing any command and is waiting for the scheduler to send a command.
 */
public class IdleState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        // If requested arrival floor is above the current floor of the elevator
        if ("moveUp".equals(event)) {
            System.out.println("Elevator State Change [Move Up]: Idle -> MovingUp");
            context.setState("MovingUp");
        }
        // If requested arrival floor is below the current floor of the elevator
        else if ("moveDown".equals(event)) {
            System.out.println("Elevator State Change [Move Down]: Idle -> MovingDown");
            context.setState("MovingDown");
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
