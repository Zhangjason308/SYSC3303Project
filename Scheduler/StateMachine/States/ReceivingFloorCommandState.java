package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

public class ReceivingFloorCommandState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("receiveStatus".equals(event)) {
            System.out.println(command + "\n" + "Scheduler State Change [Elevator Status Sent]: ReceivingFloorCommand -> ReceivingElevatorStatus");
            context.setState("ReceivingElevatorStatus");
        }
    }


    @Override
    public void displayState(FloorData command) {
        System.out.println( command + "\n" + "Scheduler State: Receiving Floor Command\n");
    }

}
