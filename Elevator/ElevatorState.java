package SYSC3303Project.Elevator;


public interface ElevatorState {
    void handleEvent(ElevatorStateMachine context, String event);
    void displayState();
}


