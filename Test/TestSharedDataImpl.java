package SYSC3303Project.Test;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.SharedDataImpl;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class TestSharedDataImpl {

    // Test getMessage method
    @Test
    void testGetMessage() {
        try {
            // Create a new instance of SharedDataImpl
            SharedDataImpl sharedData = new SharedDataImpl();

            // Create a mock FloorData object to be added
            FloorData floorData = new FloorData("10:00", 1, Direction.UP, 5); // Example values

            // Add the mock FloorData object to the shared data
            sharedData.addMessage(floorData);

            // Retrieve the message from the shared data and assert that it matches the mock FloorData object
            assertEquals(floorData, sharedData.getMessage());
        } catch (RemoteException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    // Test addMessage method
    @Test
    void testAddMessage() {
        try {
            // Create a new instance of SharedDataImpl
            SharedDataImpl sharedData = new SharedDataImpl();

            // Create a mock FloorData object to be added
            FloorData floorData = new FloorData("10:00", 1, Direction.UP, 5); // Example values

            // Add the mock FloorData object to the shared data
            sharedData.addMessage(floorData);

            // Assert that the shared data size is 1 after adding the message
            assertEquals(1, sharedData.getSize());
        } catch (RemoteException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    // Test getSize method
    @Test
    void testGetSize() {
        try {
            // Create a new instance of SharedDataImpl
            SharedDataImpl sharedData = new SharedDataImpl();

            // Create some mock FloorData objects to be added to the shared data
            FloorData floorData1 = new FloorData("10:00", 1, Direction.UP, 5); // Example values
            FloorData floorData2 = new FloorData("11:00", 2, Direction.DOWN, 4); // Example values

            // Add the mock FloorData objects to the shared data
            sharedData.addMessage(floorData1);
            sharedData.addMessage(floorData2);

            // Assert that the shared data size is 2 after adding the messages
            assertEquals(2, sharedData.getSize());
        } catch (RemoteException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

}
