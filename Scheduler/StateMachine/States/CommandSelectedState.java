package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

/**
 * Concrete state class representing the state when Scheduler selects a command from the queue.
 */
public class CommandSelectedState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("commandSent".equals(event)) {
            System.out.println("Scheduler State Change [Command Sent]: CommandSelected -> WaitingForArrivalSensor");
            context.setState("WaitingForArrivalSensor");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Scheduler State: CommandSelected\n");
    }
}
