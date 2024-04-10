package SYSC3303Project.Test;

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

    private static final String TEST_FILE_1 = "./Test/Test_1.csv";
    private static final String TEST_FILE_2 = "./Test/Test_2.csv";

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
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, TEST_FILE_1);
        floorSubsystem.run();
        assertTrue(synchronizer.getFloorCommandQueue().size() == 0);
    }

    @Test
    @DisplayName("Test get command from file")
    void GetCommandFromFileTest(){
        Synchronizer synchronizer = new Synchronizer();
        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, "./ElevatorEvents.csv");
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

        FloorSubsystem floorSubsystem = new FloorSubsystem(synchronizer, TEST_FILE_2);
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
}
