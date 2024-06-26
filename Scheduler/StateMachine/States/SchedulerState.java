package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

/**
 * Interface representing the states of the Scheduler in a state machine.
 */
public interface SchedulerState {
    void handleEvent(SchedulerStateMachine context, String event, FloorData command);
    void displayState(FloorData command);
}

