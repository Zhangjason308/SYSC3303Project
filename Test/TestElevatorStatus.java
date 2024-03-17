package SYSC3303Project.Test;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestElevatorStatus {

    // Test the constructor and getters
    @Test
    void testConstructorAndGetters() {
        int currentFloor = 5;
        Direction direction = Direction.UP;
        int id = 1;
        ElevatorStatus elevatorStatus = new ElevatorStatus(currentFloor, direction, id);
        assertEquals(currentFloor, elevatorStatus.getCurrentFloor());
        assertEquals(direction, elevatorStatus.getDirection());
        assertEquals(id, elevatorStatus.getId());
    }

    // Test the setters
    @Test
    void testSetters() {
        ElevatorStatus elevatorStatus = new ElevatorStatus(1, Direction.STATIONARY, 1);
        int newFloor = 10;
        Direction newDirection = Direction.DOWN;
        elevatorStatus.setCurrentFloor(newFloor);
        elevatorStatus.setDirection(newDirection);
        assertEquals(newFloor, elevatorStatus.getCurrentFloor());
        assertEquals(newDirection, elevatorStatus.getDirection());
    }
    @Test
    void testFloorChange() {
        ElevatorStatus elevatorStatus = new ElevatorStatus(1, Direction.STATIONARY, 1);
        int newFloor = 5;
        elevatorStatus.setCurrentFloor(newFloor);
        assertEquals(newFloor, elevatorStatus.getCurrentFloor());
    }

    @Test
    void testDirectionChange() {
        ElevatorStatus elevatorStatus = new ElevatorStatus(1, Direction.STATIONARY, 1);
        Direction newDirection = Direction.UP;
        elevatorStatus.setDirection(newDirection);
        assertEquals(newDirection, elevatorStatus.getDirection());
    }

}
