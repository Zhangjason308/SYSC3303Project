package SYSC3303Project.Test;

import SYSC3303Project.*;
import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.FloorSubsystem;
import SYSC3303Project.Scheduler.SchedulerSubsystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertTrue;

class U_Test {

    private static final String ELEVATOR_EVENTS_FILE = "ElevatorEvents.csv"; // filename

    @BeforeAll
    static void setup() {
        // If you need to perform any setup before all tests, such as configuring the test environment
    }

    @Test
    @DisplayName("U-Test 001 : Creation of Synchronizer class")
    void creationOfSynchronizerTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        assertTrue(synchronizer.getMAX_QUEUE_LENGTH() == 4);
    }

    @Test
    @DisplayName("U-Test 002 : Creation of elevatorSubsystem class")
    void creationOfElevatorSubsystemClassTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(synchronizer);
        assertTrue(elevatorSubsystem.getSynchronizer() != null);
    }

    @Test
    @DisplayName("U-Test 003 : Creation of floorSubsystem class")
    void creationOfFloorSubsystemClassTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, ELEVATOR_EVENTS_FILE);
        assertTrue(floorSubsystem.getSynchronizer() != null);
        assertTrue(floorSubsystem.getFileName().equals(ELEVATOR_EVENTS_FILE));
    }

    @Test
    @DisplayName("U-Test 004 : Creation of schedulerSubsystem class")
    void creationOfSchedulerSubsystemClassTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(synchronizer);
        assertTrue(schedulerSubsystem.getSynchronizer() != null);
    }

    @Test
    @DisplayName("U-Test 005 : Test parseInput function")
    void parseInputFunctionTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, ELEVATOR_EVENTS_FILE);
        FloorData floorData = floorSubsystem.parseInput("14:04:15.0 1 UP 4");
        assertTrue(floorData.getArrivalFloor() == 1);
        assertTrue(floorData.getDestinationFloor() == 4);
        assertTrue(floorData.getTime().equals("14:04:15.0"));
        assertTrue(floorData.getDirection() == DirectionEnum.UP);
    }

    @Test
    @DisplayName("U-Test 006 : Test sendInputLine function")
    void sendInputLineFunctionTest() {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, ELEVATOR_EVENTS_FILE);
        FloorData floorData = floorSubsystem.parseInput("14:04:15.0 1 UP 4");
        assertTrue(synchronizer.getElevatorCommands().isEmpty());
        synchronizer.sendInputLine(floorData);
        assertTrue(synchronizer.getElevatorCommands().size() == 1);
        assertTrue(synchronizer.getElevatorCommands().get(0).getArrivalFloor() == 1);
        assertTrue(synchronizer.getElevatorCommands().get(0).getDestinationFloor() == 4);
        assertTrue(synchronizer.getElevatorCommands().get(0).getTime().equals("14:04:15.0"));
        assertTrue(synchronizer.getElevatorCommands().get(0).getDirection() == DirectionEnum.UP);
    }

    @Test
    @DisplayName("U-Test 007 : Test retrieveCommand and processElevatorRequest function")
    void retrieveCommandAndProcessElevatorRequestFunctionTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, ELEVATOR_EVENTS_FILE);
        FloorData floorData = floorSubsystem.parseInput("14:04:15.0 1 UP 4");
        synchronizer.sendInputLine(floorData);
        synchronizer.retrieveCommand();
        assertTrue(synchronizer.getSelectedCommand() != null);
        int destination = synchronizer.processElevatorRequest();
        assertTrue(destination == 4);
        assertTrue(synchronizer.getSelectedCommand() == null);
    }

    @Test
    @DisplayName("U-Test 008 : Test the number of times the retrieveCommand and processElevatorRequest functions are called")
    void numberOfTimesOfRetrieveCommandAndProcessElevatorRequestTest() throws InterruptedException {
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, ELEVATOR_EVENTS_FILE);
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(synchronizer);
        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(synchronizer);
        
        Thread floor = new Thread(floorSubsystem, "floor subsystem");
        Thread elevator = new Thread(elevatorSubsystem, "elevator subsystem");
        Thread scheduler = new Thread(schedulerSubsystem, "scheduler subsystem");
        
        floor.start();
        elevator.start();
        scheduler.start();
        
        sleep(40000); //  buffer for processing
        
     }
}
