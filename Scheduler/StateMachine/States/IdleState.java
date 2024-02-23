package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class IdleState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("queueNotEmpty".equals(event)) {
            System.out.println("Scheduler State Change [Command Queue is not empty]: Idle -> CommandSelected");
            context.setState("CommandSelected");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Scheduler State: Idle\n");
    }
}
