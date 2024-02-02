package SYSC3303Project;

public class SchedulerSubsystem implements Runnable {

    Synchronizer synchronizer;

    private DirectionEnum direction;

    private int finalDestinationFloor;

    private int pickupFloor;

    private int currentFloor;

    private String time;
    public SchedulerSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }
    public void run(){
        // retrieves a command from the Queue and sends the request to the elevator
        while(true){
            FloorData floorData = synchronizer.retrieveCommandRequestElevator();
            direction = floorData.getDirection();
            finalDestinationFloor = floorData.getDestinationFloor();
            pickupFloor = floorData.getArrivalFloor();
            time = floorData.getTime();
            currentFloor = synchronizer.retrieveElevatorNotification();
            move(pickupFloor);
            move(finalDestinationFloor);
        }


    }

    public void move(int destinationFloor) {
        int floorsToMove = Math.abs(currentFloor - destinationFloor);
        for(int i = 0; i < floorsToMove; i++) {
            System.out.println("Elevator is moving " + direction.toString() + "from floor " + currentFloor + " to floor " + destinationFloor);
            //Thread.sleep(floorTimes[(floorsToMove)]); // Time it takes for elevator to move to a floor
            currentFloor = (direction == DirectionEnum.UP) ? currentFloor+1 : currentFloor-1;
            synchronizer.notifyFloor(currentFloor);
        }
        System.out.println("Elevator has stopped at floor " + destinationFloor + "\nElevator doors are opening");
        //Thread.sleep(LOADING_TIME);
        System.out.println("Elevator doors have closed");

    }




}