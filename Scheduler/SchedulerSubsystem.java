package SYSC3303Project.Scheduler;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.SchedulerStateMachine;
import SYSC3303Project.Synchronizer;
import java.util.PriorityQueue;
import java.util.Comparator;

public class SchedulerSubsystem implements Runnable {
    private Synchronizer synchronizer;
    private SchedulerStateMachine stateMachine;
    private PriorityQueue<FloorData> upQueue; // Queue for requests going up
    private PriorityQueue<FloorData> downQueue; // Queue for requests going down

    public SchedulerSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
        this.stateMachine = new SchedulerStateMachine(); // Initialize the state machine
        this.upQueue = new PriorityQueue<>(Comparator.comparingInt(FloorData::getArrivalFloor));
        this.downQueue = new PriorityQueue<>(Comparator.comparingInt(FloorData::getArrivalFloor).reversed());
    }

    @Override
    public void run() {
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

    private void processRequests() throws InterruptedException {
        String currentStateName = stateMachine.getCurrentState();

        if ("Idle".equals(currentStateName) && synchronizer.hasFloorCommands()) {
            FloorData command = synchronizer.getNextFloorCommand();
            // Add command to the appropriate queue based on its direction
            DirectionEnum Direction = null;
            if (command.getDirection() == DirectionEnum.UP) {
                upQueue.add(command);
            } else {
                downQueue.add(command);
            }
            stateMachine.triggerEvent("queueNotEmpty");
        } else if ("CommandSelected".equals(currentStateName)) {
            // Decide which queue to serve based on the elevator's current floor and direction
            // For simplicity, this example assumes you always serve the upQueue first if not empty
            if (!upQueue.isEmpty()) {
                FloorData command = upQueue.poll();
                dispatchToElevator(command);
            } else if (!downQueue.isEmpty()) {
                FloorData command = downQueue.poll();
                dispatchToElevator(command);
            }
            stateMachine.triggerEvent("commandSent");
            stateMachine.triggerEvent("sensorArrival");

            stateMachine.triggerEvent("reset");
        } else if ("WaitForSensor".equals(currentStateName)) {
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



    private void dispatchToElevator(FloorData command) throws InterruptedException {
        System.out.println("SCHEDULER_SUBSYSTEM: Dispatching FLOOR REQUEST to Elevator: " + command);
        synchronizer.addSchedulerCommand(command);
    }
}
