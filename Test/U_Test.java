package SYSC3303Project.Test;

import SYSC3303Project.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertTrue;

class U_Test {

    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 001 : Creation of Synchronizer class")
    void creationOfSynchronizerTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        assertTrue(synchronizer.getMAX_QUEUE_LENGTH() == 4);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 002 : Creation of elevatorSubsystem class")
    void creationOfElevatorSubsystemClassTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(synchronizer);
        assertTrue(elevatorSubsystem.getSynchronizer() != null);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 003 : Creation of floorSubsystem class")
    void creationOfFloorSubsystemClassTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv");
        assertTrue(floorSubsystem.getSynchronizer() != null);
        assertTrue(floorSubsystem.getFileName() != null);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 004 : Creation of schedulerSubsystem class")
    void creationOfSchedulerSubsystemClassTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(synchronizer);
        assertTrue(schedulerSubsystem.getSynchronizer() != null);
    }


    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 005 : Test parseInput function")
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
    @DisplayName("U-Test 006 : Test sendInputLine function")
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


    @org.junit.jupiter.api.Test
    @DisplayName("U-Test 007 : Test retrieveCommand and processElevatorRequest function")
    void retrieveCommandAndProcessElevatorRequestFunctionTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv");
        FloorData floorData = floorSubsystem.parseInput("14:04:15.0 1 UP 4");
        synchronizer.sendInputLine(floorData);
        synchronizer.retrieveCommand();
        assertTrue(synchronizer.getSelectedCommand() != null);
        int des = synchronizer.processElevatorRequest();
        assertTrue(des == 4);
        assertTrue(synchronizer.getSelectedCommand() == null);
    }


    @Test
    @DisplayName("U_Test 008 : Test the number of times the retrieveCommand and processElevatorRequest functions are called")
    void numberOfTimesOfRetrieveCommandAndProcessElevatorRequestTest() throws InterruptedException {
        Thread floor, elevator, scheduler;
        Synchronizer synchronizer;
        synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv");
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(synchronizer);
        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(synchronizer);
        floor = new Thread(floorSubsystem, "floor subsystem");
        elevator = new Thread(elevatorSubsystem, "elevator subsystem");
        scheduler = new Thread(schedulerSubsystem, "scheduler subsystem");
        floor.start();
        elevator.start();
        scheduler.start();
        sleep(40000);
        assertTrue(synchronizer.getNumOfCallProcessElevatorRequest() == 3);
        assertTrue(synchronizer.getNumOfCallRetrieveCommand() == 3);
    }
}