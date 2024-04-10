package SYSC3303Project.Tests.Iteration_5;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
import SYSC3303Project.Scheduler.ElevatorStatusGUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ElevatorStatusGUITest {

    private ElevatorStatusGUI elevatorStatusGUI;
    private Map<Integer, ElevatorStatus> elevatorStatusMap;

    @BeforeEach
    public void setUp() throws Exception {
        elevatorStatusGUI = new ElevatorStatusGUI();
        elevatorStatusMap = new HashMap<>();
    }

    @Test
    public void testUpdateElevatorStatuses() {
        // Assume ElevatorStatus has a constructor that allows setting initial properties
        // Or use setters if available
        int elevatorId = 10;
        Direction direction = Direction.UP;
        int currentFloor = 5;
        String state = "DoorsOpen";
        ElevatorStatus elevatorStatus = new ElevatorStatus(elevatorId, direction, currentFloor, state);

        elevatorStatusMap.put(elevatorId, elevatorStatus);

        elevatorStatusGUI.updateElevatorStatuses(elevatorStatusMap);

    }
}