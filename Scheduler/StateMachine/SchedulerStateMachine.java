package SYSC3303Project.Scheduler.StateMachine;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.States.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class representing the state machine for the Scheduler Subsystem.
 */
public class SchedulerStateMachine {
    private Map<String, SchedulerState> states;
    public Map<String, SchedulerState> getStates() {return states;}
    private SchedulerState currentState;

    private FloorData command;


    private  int triggerTime = 0;
    public int getTriggerTime() {return triggerTime;}
    List<String> stateChange = new ArrayList<>();
    public List<String> getStateChange() {return stateChange;}

    public SchedulerStateMachine(FloorData command) {
        states = new HashMap<>();
        initializeStates();
        setState("Idle");
        this.command = command;
    }

    private void initializeStates() {
        addState("Idle", new IdleState());
        addState("CommandSelected", new CommandSelectedState());
        addState("WaitingForArrivalSensor", new WaitingForArrivalSensorState());
        addState("WaitingForDestinationSensor", new WaitingForDestinationSensorState());
        addState("CommandComplete", new CommandCompleteState());
    }

    public void addState(String stateName, SchedulerState state) {
        states.put(stateName, state);
    }

    public void setState(String stateName) {
        SchedulerState state = states.get(stateName);
        if (state == null) {
            throw new IllegalStateException("State '" + stateName + "' does not exist.");
        }
        this.currentState = state;
        currentState.displayState(command);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void triggerEvent(String event) {
        currentState.handleEvent(this, event, command);
        triggerTime++;
        stateChange.add(event);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentState() {
        for (Map.Entry<String, SchedulerState> entry : states.entrySet()) {
            if (entry.getValue().equals(currentState)) {
                return entry.getKey();
            }
        }
        return null; // Or throw an exception if the state is not found
    }
}
