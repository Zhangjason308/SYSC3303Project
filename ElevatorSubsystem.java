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

    public void run() {
        while (true){
            try {
                int destinationFloor = synchronizer.processElevatorRequest();
                System.out.println("Elevator has arrived at floor " + destinationFloor + ", passengers have been dropped off");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}