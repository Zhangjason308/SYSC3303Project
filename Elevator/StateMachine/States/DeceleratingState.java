package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

public class DeceleratingState implements ElevatorState{

    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("stop".equals(event)) {
            System.out.println("Elevator State Change [Arrive at Floor]: Decelerating -> Stopped");
            context.setState("Stopped");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Decelerating\n");
    }
}
