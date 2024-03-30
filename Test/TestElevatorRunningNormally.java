package SYSC3303Project.Test;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.FloorSubsystem;
import SYSC3303Project.Scheduler.SchedulerSubsystem;
import SYSC3303Project.SharedDataInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestElevatorRunningNormally {
    static class TestSharedDataInterface1 implements SharedDataInterface {
        @Override
        public FloorData getMessage() throws RemoteException {return null;}
        @Override
        public void addMessage(FloorData message) throws RemoteException {}
        @Override
        public int getSize() {return 10;}
    }
    @Test
    @DisplayName("Test001 : The elevator received normal information")
    void Test001() throws InterruptedException, IOException {
        DatagramSocket sendSocket = new DatagramSocket(8888);
        byte[] floorDataBytes = "00:00:10.0,2,UP,3".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(floorDataBytes, floorDataBytes.length, InetAddress.getLocalHost(), 11);
        SharedDataInterface sharedData = new TestInitializationOfAnElevator.TestSharedDataInterface();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        Thread thread = new Thread(elevatorSubsystem);
        thread.start();
        sendSocket.send(sendPacket);
        Thread.sleep(1500);
        assertTrue(elevatorSubsystem.TestTime.equals("00:00:10.0"));
        assertTrue(elevatorSubsystem.TestArrivalFloor == 2);
        assertTrue(elevatorSubsystem.TestDirection == Direction.UP);
        assertTrue(elevatorSubsystem.TestDestinationFloor == 3);
        assertTrue(elevatorSubsystem.TestString.equals("---------- ELEVATOR [10]: Received Command: [Time: 00:00:10.0, Arrival Floor: 2, Direction: UP, Destination Floor: 3] ----------\n"));
    }
}
