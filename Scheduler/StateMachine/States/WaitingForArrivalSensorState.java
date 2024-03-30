package SYSC3303Project.Scheduler.StateMachine.States;

import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;

/**
 * Concrete state class representing the state when Scheduler is waiting to be notified of the elevator's arrival floor sensor.
 */
public class WaitingForArrivalSensorState implements SchedulerState {

    @Override
    public void handleEvent(SchedulerStateMachine context, String event, FloorData command) {
        if ("sensorArrival".equals(event)) {
            System.out.println(command + "\n" + "Scheduler State Change [Arrival Sensor is True]: WaitingForArrivalSensor -> WaitingForDestinationSensor");
            context.setState("WaitingForDestinationSensor");
        }
    }

    @Override
    public void displayState(FloorData command) {
        System.out.println(command + "\n" + "Scheduler State: WaitingForArrivalSensor\n");
    }
}
