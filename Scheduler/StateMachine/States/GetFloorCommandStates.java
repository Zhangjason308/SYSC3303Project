package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class GetFloorCommandStates implements SchedulerState{
    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("GetFloorCommand".equals(event)) {
            System.out.println("Scheduler State Change: Get Floor Command -> Processing Elevator Status");
            context.setState("ProcessingElevatorStatus");
        }
    }

    @Override
    public void displayState(FloorData command) {
        System.out.println("Scheduler State: Get Floor Command");
    }
}
