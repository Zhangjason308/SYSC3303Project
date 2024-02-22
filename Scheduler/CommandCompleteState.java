package SYSC3303Project.Scheduler;

public class CommandCompleteState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("reset".equals(event)) {
            context.setState("Idle");
        }
    }

    @Override
    public void displayState() {
        System.out.println("SchedulerState: CommandComplete, command execution complete.");
    }
}
