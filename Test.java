package SYSC3303Project;

import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Test {

    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 001 : Test parseInput function")
    void parseInputFunctionTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv");
        FloorData floorData = floorSubsystem.parseInput("14:04:15.0 1 UP 4");
        assertTrue(floorData.getArrivalFloor() == 1);
        assertTrue(floorData.getDestinationFloor() == 4);
        assertTrue(floorData.getTime().equals("14:04:15.0"));
        assertTrue(floorData.getDirection() == DirectionEnum.valueOf("UP"));
    }


    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 002 : Test sendInputLine function")
    void sendInputLineFunctionTest() {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv");
        FloorData floorData = floorSubsystem.parseInput("14:04:15.0 1 UP 4");
        assertTrue(synchronizer.getElevatorCommands().size() == 0);
        synchronizer.sendInputLine(floorData);
        assertTrue(synchronizer.getElevatorCommands().get(0).getArrivalFloor() == 1);
        assertTrue(synchronizer.getElevatorCommands().get(0).getDestinationFloor() == 4);
        assertTrue(synchronizer.getElevatorCommands().get(0).getTime().equals("14:04:15.0"));
        assertTrue(synchronizer.getElevatorCommands().get(0).getDirection() == DirectionEnum.valueOf("UP"));
    }
}