package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

public class MovingState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("stop".equals(event)) {
            System.out.println("Elevator State Change [Arrive at Floor]: Moving -> Stopped");
            context.setState("Stopped");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Moving\n");
    }
}
