package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

public class DoorsOpenState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("closeDoors".equals(event)) {
            System.out.println("Elevator State Change [Closing Doors]: DoorsOpen -> DoorsClosed");
            context.setState("DoorsClosed");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: DoorsOpen\n");
    }
}
