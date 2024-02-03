package SYSC3303Project;

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
