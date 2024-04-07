package SYSC3303Project;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ElevatorWatchdogTest {

    @Test
    public void testElevatorWatchdog() throws InterruptedException {
        // Simulate elevator status map
        Map<Integer, ElevatorStatus> elevatorStatusMap = new HashMap<>();

        // Add some elevators with initial status
        elevatorStatusMap.put(1, new ElevatorStatus(1, Direction.UP,12,"Moving"));
        elevatorStatusMap.put(2, new ElevatorStatus(2, Direction.UP,14,"Moving"));
        elevatorStatusMap.put(3, new ElevatorStatus(3,  Direction.UP,16,"Moving"));

        // Create an instance of ElevatorWatchdogTask
        Runnable elevatorWatchdogTask = new Runnable() {
            private final Map<Integer, Integer> lastCheckedLevels = new ConcurrentHashMap<>();
            private final Map<Integer, Long> watchStartTimes = new ConcurrentHashMap<>();
            private final long checkIntervalMillis = 35000; // Check interval for movement

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    long currentTime = System.currentTimeMillis();
                    elevatorStatusMap.forEach((id, status) -> {
                        if ("Moving".equals(status.getState())) {
                            Integer lastLevel = lastCheckedLevels.get(id);
                            if (!watchStartTimes.containsKey(id)) {
                                // Start watching this elevator and log it
                                watchStartTimes.put(id, currentTime);
                                System.out.println(" - Watching Elevator ID: " + id + " for movement.");
                            } else if (lastLevel != null && !lastLevel.equals(status.getCurrentFloor())) {
                                // If the elevator has moved to a new floor, reset the watch timer
                                watchStartTimes.put(id, currentTime);
                                System.out.println(" - Elevator ID: " + id + " moved to floor " + status.getCurrentFloor() + ". Resetting watch timer.");
                            }

                            // Update last known level
                            lastCheckedLevels.put(id, status.getCurrentFloor());

                            // Check if the elevator has stalled
                            if (lastLevel != null && lastLevel.equals(status.getCurrentFloor()) &&
                                    (currentTime - watchStartTimes.get(id)) > checkIntervalMillis) {
                                // Elevator level has not changed in the interval while it was supposed to be moving
                                handleStalledElevator(id, status.getCurrentFloor());
                            }
                        } else {
                            // If not moving, remove from watch list and last checked levels
                            watchStartTimes.remove(id);
                            lastCheckedLevels.remove(id);
                        }
                    });

                    try {
                        Thread.sleep(1000); // Check every second
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            private void handleStalledElevator(int elevatorId, int currentFloor) {
                // Log, alert, or recover from the stalled elevator situation
                System.out.println(" - WatchDogTimer: Elevator " + elevatorId + " appears to be stalled at floor " + currentFloor);
                elevatorStatusMap.get(elevatorId).setState("Disabled");
                System.out.println(" - WatchDogTimer: Elevator " + elevatorId + " has been disabled");

                // Reset the watch for this elevator
                watchStartTimes.remove(elevatorId);
                lastCheckedLevels.remove(elevatorId);
            }
        };

        // Start the watchdog task in a separate thread
        Thread watchdogThread = new Thread(elevatorWatchdogTask);
        watchdogThread.start();

        // Simulate elevator movement
        Thread.sleep(2000); // Wait for 2 seconds
        elevatorStatusMap.get(1).setCurrentFloor(2); // Elevator 1 moves to floor 2

        Thread.sleep(2000); // Wait for 2 seconds
        elevatorStatusMap.get(2).setCurrentFloor(4); // Elevator 2 moves to floor 4

        Thread.sleep(2000); // Wait for 2 seconds

        // Wait for the watchdog task to finish processing
        Thread.sleep(45000); // Adjust this time based on your expected execution time

        // Verify the changes made by the watchdog
        assertEquals("Disabled", elevatorStatusMap.get(3).getState()); // Elevator 3 should be disabled
        assertEquals("Disabled", elevatorStatusMap.get(2).getState()); // Elevator 2 should be disabled
        assertEquals("Disabled", elevatorStatusMap.get(1).getState()); // Elevator 1 should be disabled


        // Interrupt the watchdog thread to stop execution
        watchdogThread.interrupt();
    }
}
