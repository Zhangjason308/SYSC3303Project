package SYSC3303Project.Test;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.SharedDataInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestElevatorRunningWithFault {
    static class TestSharedDataInterface1 implements SharedDataInterface {
        @Override
        public FloorData getMessage() throws RemoteException {return null;}
        @Override
        public void addMessage(FloorData message) throws RemoteException {}
        @Override
        public int getSize() {return 10;}
    }
    @Test
    @DisplayName("Test001 : The elevator received wrong information")
    void Test001() throws InterruptedException, IOException {
        DatagramSocket sendSocket = new DatagramSocket(8888);
        byte[] floorDataBytes = "00:00:07.0 DOOR_FAULT 10 STUCK_OPEN 2".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(floorDataBytes, floorDataBytes.length, InetAddress.getLocalHost(), 11);
        SharedDataInterface sharedData = new TestInitializationOfAnElevator.TestSharedDataInterface();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        Thread thread = new Thread(elevatorSubsystem);
        thread.start();
        sendSocket.send(sendPacket);
        Thread.sleep(1500);
        assertTrue(elevatorSubsystem.TestString.equals("---------- ELEVATOR [10]: Received Door Fault: 00:00:07.0 DOOR_FAULT 10 STUCK_OPEN 2 ----------\n"));
    }
}
