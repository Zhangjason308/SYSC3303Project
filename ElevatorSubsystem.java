package SYSC3303Project;

import java.util.ArrayList;

/**
 * ElevatorSubsystemClient.java
 * This class is a elevator subsystem for an elevator real-time system. This subsystem
 * is a client thread that makes calls to the server thread, in which then it can perform
 * the Elevator movement.
 */
public class ElevatorSubsystem implements Runnable {
    private Synchronizer synchronizer;
    public ElevatorSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public Synchronizer getSynchronizer() {return synchronizer;}

    public void run() {
        while (synchronizer.getNumOfCallProcessElevatorRequest() < 3){
            try {
                int destinationFloor = synchronizer.processElevatorRequest();
                System.out.println("Elevator has arrived at floor " + destinationFloor + ", passengers have been dropped off");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("processElevatorRequest function was called " + synchronizer.getNumOfCallProcessElevatorRequest() + " times");
    }

}