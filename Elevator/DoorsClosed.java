package SYSC3303Project.Elevator;

public class DoorsClosed implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("moveUp".equals(event)) {
            context.setState("MovingUp");
        } else if ("moveDown".equals(event)) {
            context.setState("MovingDown");
        }
    }

    @Override
    public void displayState() {
        System.out.println("ElevatorState: DoorsClosed");
    }
}
