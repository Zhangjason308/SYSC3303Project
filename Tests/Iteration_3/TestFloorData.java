package SYSC3303Project.Test;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Floor.FloorData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FloorDataTest {

    // Test the constructor and getters
    @Test
    void testConstructorAndGetters() {
        String time = "10:30";
        int arrivalFloor = 5;
        Direction direction = Direction.UP;
        int destinationFloor = 2;
        FloorData floorData = new FloorData(time, arrivalFloor, direction, destinationFloor);
        assertEquals(time, floorData.getTime());
        assertEquals(arrivalFloor, floorData.getArrivalFloor());
        assertEquals(direction, floorData.getDirection());
        assertEquals(destinationFloor, floorData.getDestinationFloor());
    }

    // Test the stringByte method
    @Test
    void testStringByte() {
        String time = "10:30";
        int arrivalFloor = 5;
        Direction direction = Direction.UP;
        int destinationFloor = 2;
        FloorData floorData = new FloorData(time, arrivalFloor, direction, destinationFloor);
        String expected = "10:30,5,UP,2";
        String result = FloorData.stringByte(floorData);
        assertEquals(expected, result);
    }

    @Test
    void testToStringMethod() {
        String time = "10:30";
        int arrivalFloor = 5;
        Direction direction = Direction.UP;
        int destinationFloor = 2;
        FloorData floorData = new FloorData(time, arrivalFloor, direction, destinationFloor);
        String expected = "[Time: 10:30, Arrival Floor: 5, Direction: UP, Destination Floor: 2]";
        String result = floorData.toString();
        assertEquals(expected, result);
    }

    @Test
    void testDirectionEnum() {
        Direction direction = Direction.UP;
        assertEquals("UP", direction.toString());
        direction = Direction.DOWN;
        assertEquals("DOWN", direction.toString());
    }}
