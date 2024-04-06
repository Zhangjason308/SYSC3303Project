package SYSC3303Project.Elevator.StateMachine.States;


import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;

/**
 * Interface representing the states of the Elevator in a state machine.
 */
public interface ElevatorState {
    void handleEvent(ElevatorStateMachine context, String event);
    void displayState();
}


