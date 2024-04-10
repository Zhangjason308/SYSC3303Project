package SYSC3303Project.Tests.Iteration_4;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTheDirection {
    static class TestSharedDataInterface implements SharedDataInterface {
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
        SharedDataInterface sharedData = new TestInitializationOfAnElevator.TestSharedDataInterface();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        assertEquals(elevatorSubsystem.determineDirection(1, 2), Direction.UP);
        assertEquals(elevatorSubsystem.determineDirection(2, 1), Direction.DOWN);
        assertEquals(elevatorSubsystem.determineDirection(1, 1), Direction.STATIONARY);
    }
}
