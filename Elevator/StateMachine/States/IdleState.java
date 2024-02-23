package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Elevator.StateMachine.States.ElevatorState;

public class IdleState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("moveUp".equals(event)) {
            context.setState("MovingUp");
        } else if ("openDoors".equals(event)) {
            context.setState("DoorsOpen");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Idle\n");
    }
}
