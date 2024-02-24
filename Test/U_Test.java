package SYSC3303Project.Test;

<<<<<<< Updated upstream
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
=======
import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.FloorSubsystem;
import SYSC3303Project.Scheduler.SchedulerSubsystem;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;
import SYSC3303Project.Synchronizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;
import static java.lang.System.setOut;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

class U_Test {

    @Test
    @DisplayName("Test the initial floor")
    void InitialFloorTest(){
        Synchronizer synchronizer = new Synchronizer();
        assertTrue(synchronizer.getCurrentFloor() == 1);
    }

    @Test
    @DisplayName("Test received empty file")
    void EmptyFileTest(){
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/Test/Test_1.csv");
        floorSubsystem.run();
        assertTrue(synchronizer.getFloorCommandQueue().size() == 0);
    }

    @Test
    @DisplayName("Test get command from file")
    void GetCommandFromFileTest(){
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/ElevatorEvents.csv");
        floorSubsystem.run();
        assertTrue(synchronizer.getFloorCommandQueue().size() == 3);

    }

    @Test
    @DisplayName("Test initialize the elevator state machine")
    void InitializeTheElevatorStateMachineTest(){
        ElevatorStateMachine elevatorStateMachine = new ElevatorStateMachine();
        assertTrue(elevatorStateMachine.getStates().size() == 6);
        assertTrue(elevatorStateMachine.getCurrentState().equals("Idle"));
    }

    @Test
    @DisplayName("Test initialize the scheduler state machine")
    void InitializeTheSchedulerStateMachineTest(){
        SchedulerStateMachine schedulerStateMachine = new SchedulerStateMachine();
        assertTrue(schedulerStateMachine.getStates().size() == 5);
        assertTrue(schedulerStateMachine.getCurrentState().equals("Idle"));
    }

    @Test
    @DisplayName("Testing a state machine when it runs one command")
    void OneCommandTest() throws InterruptedException {

        Synchronizer synchronizer = new Synchronizer();
        assertTrue(synchronizer.getCurrentFloor() == 1);

        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "SYSC3303Project/Test/Test_2.csv");
        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(synchronizer);
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(synchronizer);

        new Thread(floorSubsystem, "floor subsystem").start();

        new Thread(schedulerSubsystem,"scheduler subsystem").start();
        new Thread(elevatorSubsystem,"elevator subsystem").start();
        sleep(25000);

        // The schedule state machine has experienced 5 state changes
        assertTrue(schedulerSubsystem.getStateMachine().getTriggerTime() == 5);
        //state transition process
        assertTrue(schedulerSubsystem.getStateMachine().getStateChange().get(0).equals("queueNotEmpty")
                && schedulerSubsystem.getStateMachine().getStateChange().get(1).equals("commandSent")
                && schedulerSubsystem.getStateMachine().getStateChange().get(2).equals("sensorArrival")
                && schedulerSubsystem.getStateMachine().getStateChange().get(3).equals("sensorDestination")
                && schedulerSubsystem.getStateMachine().getStateChange().get(4).equals("reset"));

        // The elevator state machine has experienced 8 state changes
        assertTrue(elevatorSubsystem.getElevatorStateMachine().getTriggerTime() == 8);
        //state transition process
        assertTrue(elevatorSubsystem.getElevatorStateMachine().getStateChange().get(0).equals("stop")
                && elevatorSubsystem.getElevatorStateMachine().getStateChange().get(1).equals("openDoors")
                && elevatorSubsystem.getElevatorStateMachine().getStateChange().get(2).equals("closeDoors")
                && elevatorSubsystem.getElevatorStateMachine().getStateChange().get(3).equals("moveUp")
                && elevatorSubsystem.getElevatorStateMachine().getStateChange().get(4).equals("stop")
                && elevatorSubsystem.getElevatorStateMachine().getStateChange().get(5).equals("openDoors")
                && elevatorSubsystem.getElevatorStateMachine().getStateChange().get(6).equals("closeDoors")
                && elevatorSubsystem.getElevatorStateMachine().getStateChange().get(7).equals("idle"));
    }


>>>>>>> Stashed changes
}