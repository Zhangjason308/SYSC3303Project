package SYSC3303Project.Scheduler;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;
import SYSC3303Project.Synchronizer;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * SchedulerSubsystem.java
 * This class represents the Scheduler subsystem, which receives commands from the Floor Subsystem, and sends commands to the Elevator Subsystem.
 * The Scheduler implements a state machine to keep track of its functions and state.
 */
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
        //When in idle state and queue is not empty, change state to selected Command
        if ("Idle".equals(currentStateName) && synchronizer.hasFloorCommands()) {
            stateMachine.triggerEvent("queueNotEmpty");
        }
        // When in command selected state, selected a command from queue
        else if ("CommandSelected".equals(currentStateName)) {
            FloorData command = synchronizer.getNextFloorCommand();
            // Send the command to the elevator
            dispatchToElevator(command);

            stateMachine.triggerEvent("commandSent");
            // Pick up the passenger from the arrival floor
            stateMachine.triggerEvent("sensorArrival");
            // Wait until the destination sensor is triggered to changed state to command complete
            synchronized (synchronizer) {
                while (!synchronizer.getDestinationSensor()) {
                    synchronizer.wait();
                }
                stateMachine.triggerEvent("sensorDestination");
                synchronizer.setDestinationSensor(false);
                synchronizer.notifyAll();
            }
            // wait for another command from the synchronizer queue
            stateMachine.triggerEvent("reset");
        }

        try {
            Thread.sleep(100); // Adjust the sleep time as necessary
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, failed to complete operation");
        }
    }


    // Send the Command Request to the Elevator
    private void dispatchToElevator (FloorData command) throws InterruptedException {
        System.out.println("---------- SCHEDULER SUBSYSTEM: Dispatching Floor Request to Elevator: " + command + " ----------\n");
        synchronizer.addSchedulerCommand(command);
    }
}
