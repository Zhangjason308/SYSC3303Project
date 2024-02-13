package SYSC3303Project.Floor;

import SYSC3303Project.DirectionEnum;

/**
 * FloorData.java
 * This class is the data structure used to parse the inputline from the text file
 * into a command for the elevator to use. The data structure consists of the time the button is pressed,
 * the floor number of button pressed, the direction of the button, and the destination floor.
 */
public class FloorData {

    private String time;
    private int arrivalFloor;
    private DirectionEnum direction;
    private int destinationFloor;


    public FloorData(String time, int arrivalFloor, DirectionEnum direction, int destinationFloor) {
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
    public DirectionEnum getDirection() {
        return direction;
    }
    public int getDestinationFloor() {
        return destinationFloor;
    }

}
