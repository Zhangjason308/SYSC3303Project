package SYSC3303Project.Elevator.StateMachine.States;


import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

public interface ElevatorState {
    void handleEvent(ElevatorStateMachine context, String event);
    void displayState();
}


