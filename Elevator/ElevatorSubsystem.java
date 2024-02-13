package SYSC3303Project.Elevator;

import SYSC3303Project.Synchronizer;

import java.util.*;

/**
 * ElevatorSubsystemClient.java
 * This class is a elevator subsystem for an elevator real-time system. This subsystem
 * is a client thread that makes calls to the server thread, in which then it can perform
 * the Elevator movement.
 */
public class ElevatorSubsystem implements Runnable {



    private String direction;
    private Map<String, ElevatorState> states;
    private ElevatorState currentState;
    private Synchronizer synchronizer;

    public ElevatorSubsystem(Synchronizer synchronizer) {

        this.synchronizer = synchronizer;
        states = new HashMap<>();
        // Add states to the map
        addState("Idle", new Idle());
        addState("MovingUp", new MovingUp());
        addState("MovingDown", new MovingDown());
        addState("Stopped", new Stopped());
        addState("DoorsOpen", new DoorsOpen());
        addState("DoorsClosed", new DoorsClosed());


    }

    public Synchronizer getSynchronizer() {return synchronizer;}
    public String getDirection() {
        return direction;
    }

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

    public void setState(String state){
        this.currentState = getState(state);
    }
    public void addState(String stateName, ElevatorState state) {
        states.put(stateName, state);
    }

    public ElevatorState getState(String stateName) {
        return states.get(stateName);
    }

    public void

}