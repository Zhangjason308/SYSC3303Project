package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Elevator.StateMachine.States.ElevatorState;

public class MovingUpState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("stop".equals(event)) {
            System.out.println("Elevator State Change [Arrive at Floor]: MovingUp -> Stopped");
            context.setState("Stopped");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: MovingUp\n");
    }
}
