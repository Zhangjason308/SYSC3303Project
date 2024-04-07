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
    private SchedulerState currentState;
    private FloorData command;
    List<String> stateChange = new ArrayList<>();

    public SchedulerStateMachine(FloorData command) {
        states = new HashMap<>();
        initializeStates();
        setState("Idle");
        this.command = command;
    }

    private void initializeStates() {
        addState("Idle", new IdleStates());
        addState("GetElevatorStatus", new GetElevatorStatusStates());
        addState("GetFloorCommand", new GetFloorCommandStates());
        addState("ProcessingElevatorStatus", new ProcessingElevatorStatusStates());
        addState("ProcessingFloorCommand", new ProcessingFloorCommandStates());
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
        // Or throw an exception if the state is not found
        return null;
    }
}
