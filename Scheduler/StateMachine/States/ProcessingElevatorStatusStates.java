package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class ProcessingElevatorStatusStates implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("ProcessingElevatorStatus".equals(event)) {
            System.out.println("Scheduler State Change: Processing Elevator Status -> Processing Floor Command");
            context.setState("ProcessingFloorCommand");
        }
    }


    @Override
    public void displayState(FloorData command) {
        System.out.println("Scheduler State: Processing Elevator Status");
    }

}
