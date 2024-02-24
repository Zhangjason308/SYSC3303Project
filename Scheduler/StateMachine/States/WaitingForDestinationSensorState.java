package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

/**
 * Concrete state class representing the state when Scheduler is waiting to be notified of the elevator's destination floor sensor.
 */
public class WaitingForDestinationSensorState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("sensorDestination".equals(event)) {
            System.out.println("Scheduler State Change [Destination Sensor is True]: WaitingForDestinationSensor -> CommandComplete");
            context.setState("CommandComplete");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Scheduler State: WaitingForDestinationSensor\n");
    }
}
