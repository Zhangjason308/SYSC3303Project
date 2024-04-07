package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class ProcessingFloorCommandStates implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("ProcessingFloorCommand".equals(event)) {
            System.out.println("Scheduler State Change: Processing Floor Command -> Idle");
            context.setState("Idle");
        }
    }


    @Override
    public void displayState(FloorData command) {
        System.out.println("Scheduler State: Processing Floor Command");
    }

}
