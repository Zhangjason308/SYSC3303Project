package SYSC3303Project.Elevator;

public class DoorsOpen implements ElevatorState {
    @Override
    public void handleEvent(ElevatorStateMachine context, String event) {
        if ("closeDoors".equals(event)) {
            context.setState("DoorsClosed");
        }
    }

    @Override
    public void displayState() {
        System.out.println("ElevatorState: DoorsOpen");
    }
}
