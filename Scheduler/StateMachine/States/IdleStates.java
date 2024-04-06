package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class IdleStates implements SchedulerState{
    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("Idle".equals(event)) {
            System.out.println("Scheduler State Change: Idle -> Get Elevator Status");
            context.setState("GetElevatorStatus");
        }
    }

    @Override
    public void displayState(FloorData command) {
        System.out.println("Scheduler State: Idle");
    }
}
