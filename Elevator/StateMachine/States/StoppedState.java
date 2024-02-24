package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Elevator.StateMachine.States.ElevatorState;

public class StoppedState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        // Open Doors to pickup or drop off passengers
        if ("openDoors".equals(event)) {
            System.out.println("Elevator State Change [Open Doors]: Stopped -> DoorsOpen");
            context.setState("DoorsOpen");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Stopped\n");
    }
}
