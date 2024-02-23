package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public interface SchedulerState{
    void handleEvent(SchedulerStateMachine context, String event);
    void displayState();
}

