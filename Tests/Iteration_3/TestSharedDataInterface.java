package SYSC3303Project.Test;

import static org.junit.jupiter.api.Assertions.*;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.SharedDataInterface;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

class TestSharedDataInterface {

    // Test getMessage method
    @Test
    void testGetMessage() {
        try {
            // Create a mock implementation of SharedDataInterface
            SharedDataInterface sharedData = new SharedDataInterface() {
                @Override
                public FloorData getMessage() {
                    // Return a mock FloorData object for testing
                    return new FloorData("10:00", 1, Direction.UP, 5); // Example values
                }

                // Implement other methods (not relevant for this test)
                @Override
                public void addMessage(FloorData message) throws RemoteException {
                }

                @Override
                public int getSize() throws RemoteException {
                    return 0;
                }
            };

            // Call getMessage and assert that it returns a non-null value
            assertNotNull(sharedData.getMessage());
        } catch (RemoteException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    // Test addMessage method
    @Test
    void testAddMessage() {
        // Create a mock implementation of SharedDataInterface
        SharedDataInterface sharedData = new SharedDataInterface() {
            // Implement addMessage method (not relevant for this test)
            @Override
            public void addMessage(FloorData message) {
            }

            // Implement other methods (not relevant for this test)
            @Override
            public FloorData getMessage() throws RemoteException {
                return null;
            }

            @Override
            public int getSize() throws RemoteException {
                return 0;
            }
        };

        // Create a mock FloorData object to be added
        FloorData floorData = new FloorData("10:00", 1, Direction.UP, 5); // Example values

        // Call addMessage and assert that it does not throw an exception
        assertDoesNotThrow(() -> {
            sharedData.addMessage(floorData);
        });
    }

    // Test getSize method
    @Test
    void testGetSize() {
        try {
            // Create a mock implementation of SharedDataInterface
            SharedDataInterface sharedData = new SharedDataInterface() {
                // Implement getSize method to return a mock size value
                @Override
                public int getSize() {
                    return 5; // Example value
                }

                // Implement other methods (not relevant for this test)
                @Override
                public FloorData getMessage() throws RemoteException {
                    return null;
                }

                @Override
                public void addMessage(FloorData message) throws RemoteException {
                }
            };

            // Call getSize and assert that it returns the expected value
            assertEquals(5, sharedData.getSize());
        } catch (RemoteException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

}
