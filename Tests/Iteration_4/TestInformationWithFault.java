package SYSC3303Project.Tests.Iteration_4;

import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.FloorSubsystem;
import SYSC3303Project.Scheduler.SchedulerSubsystem;
import SYSC3303Project.SharedDataInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestInformationWithFault {
    static class TestSharedDataInterface1 implements SharedDataInterface {
        @Override
        public FloorData getMessage() throws RemoteException {return null;}
        @Override
        public void addMessage(FloorData message) throws RemoteException {}
        @Override
        public int getSize() {return 10;}
    }
    @Test
    @DisplayName("Test001 : Test when Scheduler Subsystem receives information with Fault")
    void Test001() throws InterruptedException, SocketException {
        SharedDataInterface sharedData = new TestInitializationOfAnElevator.TestSharedDataInterface();
        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(sharedData);
        FloorSubsystem floorSubsystem = new FloorSubsystem(sharedData, "Test/SchedulerSubsystemTest1.csv");
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        Thread thread1 = new Thread(schedulerSubsystem);
        Thread thread2 = new Thread(floorSubsystem);
        Thread thread3 = new Thread(elevatorSubsystem);
        thread1.start();
        thread2.start();
        thread3.start();
        Thread.sleep(8000);
        //assertTrue(schedulerSubsystem.TestFault.contains("Sending FAULT to Elevator 10: 00:00:07.0 DOOR_FAULT 10 STUCK_OPEN 2"));
    }
}
