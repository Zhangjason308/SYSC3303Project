package SYSC3303Project;

public class SchedulerSubsystem implements Runnable {

    Synchronizer synchronizer;

    private DirectionEnum direction;

    private int destinationFloor;

    private int pickupFloor;
    public SchedulerSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }
    public void run(){
        FloorData floorData = synchronizer.retrieveCommand();
        direction = floorData.getDirection();
        destinationFloor = floorData.getDestinationFloor();
        pickupFloor = floorData.getArrivalFloor();
        synchronizer.requestElevator(direction, destinationFloor, pickupFloor);

        //set flag in synchronizer so elevator can grab stuff
    }




}