package SYSC3303Project.Test;

import SYSC3303Project.Elevator.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ElevatorSubsystemTest {

    @Test
    void testAddAndResolveDoorFault() {
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(null, 10);
        DoorFault fault = new DoorFault("STUCK_OPEN", 1);
        elevatorSubsystem.addDoorFault(fault);

        // Assert the fault is added
        assertFalse(elevatorSubsystem.getDoorFaults().isEmpty(), "Door fault should be added");

        elevatorSubsystem.simulateHandleDoorFaults();

        assertTrue(elevatorSubsystem.getDoorFaults().isEmpty(), "Door fault should be resolved");
    }

    @Test
    void testMultipleDoorFaultsResolution() {
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(null, 12);
        elevatorSubsystem.addDoorFault(new DoorFault("STUCK_OPEN", 1));
        elevatorSubsystem.addDoorFault(new DoorFault("STUCK_CLOSED", 1));

        assertEquals(2, elevatorSubsystem.getDoorFaults().size(), "Two door faults should be added");
        elevatorSubsystem.simulateHandleDoorFaults();
        assertTrue(elevatorSubsystem.getDoorFaults().isEmpty(), "All door faults should be resolved");
    }

}