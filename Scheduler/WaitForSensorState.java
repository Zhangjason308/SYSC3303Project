package SYSC3303Project.Scheduler;

public class WaitForSensorState implements SchedulerState {
    @Override
    public void handleEvent(SchedulerStateMachine context, String event) {
        if ("sensorArrival".equals(event)) {
            context.setState("CommandComplete");
        }
    }

    @Override
    public void displayState() {
        System.out.println("SchedulerState: WaitForSensor, awaiting sensor signal.");
    }
}
