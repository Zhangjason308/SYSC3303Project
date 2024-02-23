package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;
import SYSC3303Project.Scheduler.StateMachine.States.SchedulerState;

public class WaitForSensorState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("sensorArrival".equals(event)) {
            System.out.println("Scheduler State Change [Arrival Sensor is True]: WaitForSensor -> CommandComplete");
            context.setState("CommandComplete");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Scheduler State: WaitForSensor\n");
    }
}
