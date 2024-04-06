package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class GetElevatorStatusStates implements SchedulerState{
    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("GetElevatorStatus".equals(event)) {
            System.out.println("Scheduler State Change: Get Elevator Status -> Get Floor Command");
            context.setState("GetFloorCommand");
        }
    }

    @Override
    public void displayState(FloorData command) {
        System.out.println("Scheduler State: Get Elevator Status");
    }
}
