package SYSC3303Project.Elevator.StateMachine;

import SYSC3303Project.Elevator.StateMachine.States.*;

import java.util.HashMap;
import java.util.Map;

public class ElevatorStateMachine {
    private Map<String, ElevatorState> states;
    private ElevatorState currentState;

    public ElevatorStateMachine() {
        states = new HashMap<>();
        initializeStates();
        setState("Idle");
    }

    private void initializeStates() {
        addState("Idle", new IdleState());
        addState("MovingUp", new MovingUpState());
        addState("MovingDown", new MovingDownState());
        addState("DoorsOpen", new DoorsOpenState());
        addState("DoorsClosed", new DoorsClosedState());
        addState("Stopped", new StoppedState());

    }

    public void addState(String stateName, ElevatorState state) {
        states.put(stateName, state);
    }

    public void setState(String stateName) {
        ElevatorState state = states.get(stateName);
        if (state == null) {
            throw new IllegalStateException("State '" + stateName + "' does not exist.");
        }
        this.currentState = state;
        currentState.displayState();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void triggerEvent(String event) {
        currentState.handleEvent(this, event);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
