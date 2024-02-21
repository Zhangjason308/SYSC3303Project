package SYSC3303Project.Scheduler;

public class IdleState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("queueNotEmpty".equals(event)) {
            context.setState("CommandSelected");
        }
    }

    @Override
    public void displayState() {
        System.out.println("SchedulerState: Idle, waiting for commands.");
    }
}
