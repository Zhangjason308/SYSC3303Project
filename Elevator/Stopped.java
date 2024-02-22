package SYSC3303Project.Elevator;

public class Stopped implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("openDoors".equals(event)) {
            context.setState("DoorsOpen");
        } else if ("closeDoors".equals(event)) {
            // This transition might seem redundant if doors are assumed to be closed in Stopped state,
            // but it allows for explicit control over door state and future flexibility.
            context.setState("DoorsClosed");
        } else if ("moveUp".equals(event)) {
            context.setState("MovingUp");
        } else if ("moveDown".equals(event)) {
            context.setState("MovingDown");
        }
    }

    @Override
    public void displayState() {
        System.out.println("ElevatorState: Stopped");
    }
}
