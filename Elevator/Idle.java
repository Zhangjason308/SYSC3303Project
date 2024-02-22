package SYSC3303Project.Elevator;

public class Idle implements ElevatorState {
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
        System.out.println("ElevatorState: Idle.");
    }
}
