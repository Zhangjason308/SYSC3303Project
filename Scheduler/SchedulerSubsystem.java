package SYSC3303Project.Scheduler;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;
import SYSC3303Project.Synchronizer;
import java.util.PriorityQueue;
import java.util.Comparator;

public class SchedulerSubsystem implements Runnable {
    private Synchronizer synchronizer;
    private SchedulerStateMachine stateMachine;

    public SchedulerSubsystem(Synchronizer synchronizer) throws InterruptedException {
        this.synchronizer = synchronizer;
        this.stateMachine = new SchedulerStateMachine(); // Initialize the state machine
    }


    @Override
    public void run () {
        while (true) {
            try {
                processRequests();
                Thread.sleep(100); // Adjust the sleep time as necessary
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Scheduler thread was interrupted, failed to complete operation");
                break;
            }
        }
    }

    private void processRequests () throws InterruptedException {
        String currentStateName = stateMachine.getCurrentState();

        if ("Idle".equals(currentStateName) && synchronizer.hasFloorCommands()) {

            // Add command to the appropriate queue based on its direction
            DirectionEnum Direction = null;

            stateMachine.triggerEvent("queueNotEmpty");
        } else if ("CommandSelected".equals(currentStateName)) {
            FloorData command = synchronizer.getNextFloorCommand();
            dispatchToElevator(command);

            stateMachine.triggerEvent("commandSent");
            stateMachine.triggerEvent("sensorArrival");
            synchronized (synchronizer) {
                while (!synchronizer.getDestinationSensor()) {
                    synchronizer.wait();
                }
                stateMachine.triggerEvent("sensorDestination");
                synchronizer.setDestinationSensor(false);
                synchronizer.notifyAll();
            }

            stateMachine.triggerEvent("reset");
        } else if ("WaitingForArrivalSensor".equals(currentStateName)) {
            int elevatorCurrentFloor = synchronizer.getCurrentFloor();
            System.out.println("SCHEDULER NOTIFIED OF ELEVATOR CURRENT FLOOR: " + elevatorCurrentFloor);

            if (!true) {
                stateMachine.triggerEvent("sensorArrival");

            }
        } else if ("CommandComplete".equals(currentStateName)) {
            stateMachine.triggerEvent("reset");
        }

        try {
            Thread.sleep(100); // Adjust the sleep time as necessary
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, failed to complete operation");
        }
    }



    private void dispatchToElevator (FloorData command) throws InterruptedException {
        System.out.println("---------- SCHEDULER SUBSYSTEM: Dispatching Floor Request to Elevator: " + command + " ----------\n");
        synchronizer.addSchedulerCommand(command);
    }
}
