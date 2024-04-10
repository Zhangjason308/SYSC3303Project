package SYSC3303Project.Tests.Iteration_4;

import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.SharedDataInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMoveFromTo {
    static class TestSharedDataInterface implements SharedDataInterface {
        @Override
        public FloorData getMessage() throws RemoteException {return null;}
        @Override
        public void addMessage(FloorData message) throws RemoteException {}
        @Override
        public int getSize() {return 10;}
    }
    @Test
    @DisplayName("Test001")
    void Test001() throws InterruptedException, IOException {
        SharedDataInterface sharedData = new TestInitializationOfAnElevator.TestSharedDataInterface();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        elevatorSubsystem.setCurrentFloor(2);

        elevatorSubsystem.moveToFloor(1);
        elevatorSubsystem.moveToFloor(3);
        assertTrue(elevatorSubsystem.TestMoveFromTo.get(0).equals(2));
        assertTrue(elevatorSubsystem.TestMoveFromTo.get(1).equals(1));
        assertTrue(elevatorSubsystem.TestMoveFromTo.get(2).equals(1));
        assertTrue(elevatorSubsystem.TestMoveFromTo.get(3).equals(2));
        assertTrue(elevatorSubsystem.TestMoveFromTo.get(4).equals(3));

    }
}
