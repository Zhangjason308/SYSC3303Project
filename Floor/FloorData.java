package SYSC3303Project.Floor;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.Direction;

/**
 * FloorData.java
 * This class is the data structure used to parse the inputline from the text file
 * into a command for the elevator to use. The data structure consists of the time the button is pressed,
 * the floor number of button pressed, the direction of the button, and the destination floor.
 */
public class FloorData {

    private String time;
    private int arrivalFloor;
    private Direction direction;
    private int destinationFloor;


    public FloorData(String time, int arrivalFloor, Direction direction, int destinationFloor) {
        this.time = time;
        this.arrivalFloor = arrivalFloor;
        this.direction = direction;
        this.destinationFloor = destinationFloor;
    }

    public String getTime() {
        return time;
    }
    public int getArrivalFloor() {
        return arrivalFloor;
    }
    public Direction getDirection() {
        return direction;
    }
    public int getDestinationFloor() {
        return destinationFloor;
    }

    @Override
    public String toString() {
        return "[Time: " + getTime() + ", Arrival Floor: " + getArrivalFloor() + ", Direction: " + getDirection() + ", Destination Floor: " + getDestinationFloor() +"]";
    }

    public static String stringByte(FloorData data) {
        return data.getTime() + "," + data.getArrivalFloor() + "," + data.getDirection() + "," + data.getDestinationFloor();
    }
}
