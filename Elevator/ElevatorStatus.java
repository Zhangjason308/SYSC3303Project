package SYSC3303Project.Elevator;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class ElevatorStatus {
    private int currentFloor;
    private Direction direction;
    private int id;
    private String state;
    private List<SimpleEntry<Integer, Integer>> targetFloors; // Store pairs of arrival and destination floors

    public ElevatorStatus(int currentFloor, Direction direction, int id, String state) {
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.id = id;
        this.state = state;
        this.targetFloors = new ArrayList<>();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<SimpleEntry<Integer, Integer>> getTargetFloors() {
        return targetFloors;
    }

    public void setTargetFloors(List<SimpleEntry<Integer, Integer>> targetFloors) {
        this.targetFloors = targetFloors;
    }

    // Method to add a target floor pair
    public void addTargetFloorPair(int arrivalFloor, int destinationFloor) {
        targetFloors.add(new SimpleEntry<>(arrivalFloor, destinationFloor));
    }

    // Method to remove a target floor pair
    public void removeTargetFloorPair(SimpleEntry<Integer, Integer> floorPair) {
        targetFloors.remove(floorPair);
    }
}
