package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

/**
 * Concrete state class representing the state when the command that the Scheduler sent out has been successfully complete.
 */
public class CommandCompleteState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("reset".equals(event)) {
            System.out.println("Scheduler State Change [Return to Idle]: CommandComplete -> Idle");
            context.setState("Idle");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Scheduler State: CommandComplete\n");
    }
}
