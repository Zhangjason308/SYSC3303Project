package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

/**
 * Concrete state class representing the state when Scheduler is waiting to select a command.
 */
public class IdleState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("commandReceived".equals(event)) {
            System.out.println(command + "\n" + "Scheduler State Change [Command Queue is not empty]: Idle -> CommandSelected");
            context.setState("CommandSelected");
        }
    }


    @Override
    public void displayState(FloorData command) {
        System.out.println( command + "\n" + "Scheduler State: Idle\n");
    }


}
