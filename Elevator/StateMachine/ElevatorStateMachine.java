package SYSC3303Project.Elevator.StateMachine;

import SYSC3303Project.Elevator.StateMachine.States.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class representing the state machine for the ElevatorSubsystem.
 */
public class ElevatorStateMachine {
    private Map<String, ElevatorState> states;
    public Map<String, ElevatorState> getStates() {return states;}

    private ElevatorState currentState;



    private int triggerTime = 0;
    public int getTriggerTime() {return triggerTime;}
    private List<String> stateChange = new ArrayList<>();
    public List<String> getStateChange() {return stateChange;}


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
        stateChange.add(event);
        triggerTime++;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentState(){
        for(Map.Entry<String, ElevatorState> entry : states.entrySet()){
            if(entry.getValue().equals(currentState)){
                return entry.getKey();
            }
        }
        return  null;
    }
}
