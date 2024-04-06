package SYSC3303Project.Elevator.StateMachine.States;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

public class DisabledState implements ElevatorState {

    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        // Since the elevator is disabled, it might only handle an "enable" event,
        // which would transition it back to a functional state, such as "Stopped" or "Idle".
        if ("enable".equals(event)) {
            System.out.println("Elevator State Change [Maintenance Complete]: Disabled -> Stopped");
            context.setState("Stopped");
        } else {
            System.out.println("Elevator is currently disabled and cannot handle any events except 'enable'.");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator State: Disabled - Elevator is out of service.\n");
    }
}