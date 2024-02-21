package SYSC3303Project.Scheduler;

public class CommandSelectedState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("commandSent".equals(event)) {
            context.setState("WaitForSensor");
        }
    }

    @Override
    public void displayState() {
        System.out.println("SchedulerState: CommandSelected, command sent.");
    }
}
