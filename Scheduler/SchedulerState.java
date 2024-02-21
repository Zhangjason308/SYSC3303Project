package SYSC3303Project.Scheduler;

public interface SchedulerState{
    void handleEvent(SchedulerStateMachine context, String event);
    void displayState();
}

