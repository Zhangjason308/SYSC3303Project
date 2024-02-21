package SYSC3303Project.Elevator;

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
        addState("Idle", new Idle());
        addState("MovingUp", new MovingUp());
        addState("MovingDown", new MovingDown());
        addState("DoorsOpen", new DoorsOpen());
        addState("DoorsClosed", new DoorsClosed());
        addState("Stopped", new Stopped());

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
    }

    public void triggerEvent(String event) {
        currentState.handleEvent(this, event);
    }


    public String getCurrentState() {
        for (Map.Entry<String, ElevatorState> entry : states.entrySet()) {
            if (entry.getValue().equals(currentState)) {
                return entry.getKey();
            }
        }
        return "Unknown"; //
    }
}
