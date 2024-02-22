package SYSC3303Project.Elevator;

public class MovingDown implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("stop".equals(event)) {
            context.setState("DoorsOpen");
        }
    }

    @Override
    public void displayState() {
        System.out.println("ElevatorState : MovingDown");
    }
}
