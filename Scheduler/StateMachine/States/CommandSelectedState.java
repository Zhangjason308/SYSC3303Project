package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class CommandSelectedState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("commandSent".equals(event)) {
            System.out.println("Scheduler State Change [Command Sent]: CommandSelected -> WaitForSensor");
            context.setState("WaitForSensor");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Scheduler State: CommandSelected\n");
    }
}
