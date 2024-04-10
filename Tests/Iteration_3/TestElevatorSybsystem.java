package SYSC3303Project.Test;

import static org.junit.jupiter.api.Assertions.*;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorSubsystem;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.SharedDataInterface;
import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

class ElevatorSubsystemTest {

    // Mock SharedDataInterface for testing
    static class MockSharedDataInterface implements SharedDataInterface {
        @Override
        public FloorData getMessage() throws RemoteException {
            return null;
        }

        @Override
        public void addMessage(FloorData message) throws RemoteException {

        }

        // Implement necessary methods for testing
        @Override
        public int getSize() {
            // Return a mock size value for testing
            return 5; // Example value
        }
    }

    // Test the constructor
    @Test
    void testConstructor() {
        SharedDataInterface sharedData = new MockSharedDataInterface();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        assertNotNull(elevatorSubsystem);
    }

    // Test the getElevatorStatus method
    @Test
    void testGetElevatorStatus() {
        SharedDataInterface sharedData = new MockSharedDataInterface();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        assertNotNull(elevatorSubsystem.getElevatorStatus());
    }

    // Test the processCommand method
    @Test
    void testProcessCommand() {
        SharedDataInterface sharedData = new MockSharedDataInterface();
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData, 10);
        // Create a mock FloorData
        FloorData floorData = new FloorData("10:00", 1, Direction.UP, 5);
        assertDoesNotThrow(() -> {
            elevatorSubsystem.processCommand(floorData);
        });
    }

    // Add more test cases as needed
}
