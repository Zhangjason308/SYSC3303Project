package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

public class DoorsClosedState implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("moveUp".equals(event)) {
            System.out.println("Elevator State Change [Processing Destination Request]: DoorsClosed -> MovingUp");
            context.setState("MovingUp");
        } else if ("moveDown".equals(event)) {
            System.out.println("Elevator State Change [Processing Destination Request]: DoorsClosed -> MovingDown");
            context.setState("MovingDown");
        } else {
            System.out.println("Elevator State Change [Signaling Scheduler for New Request]: DoorsClosed -> Idle");
            context.setState("Idle");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: DoorsClosed\n");
    }
}
