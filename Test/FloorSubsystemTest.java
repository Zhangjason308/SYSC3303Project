package SYSC3303Project.Test;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Floor.FloorSubsystem;

import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloorSubsystemTest {

    private static final long SOME_EXPECTED_VALUE = 0L;

    // Simulates the process of sending a floor request and receiving an acknowledgment
    // This is a simplistic version to demonstrate the concept
    public void testDoorOpensOnRequest() throws SocketException {
        // Setup - Create a FloorSubsystem instance with a dummy file (or mock data)
        String fileName = "test_data.txt"; // This file should contain test data formatted as expected by FloorSubsystem
        FloorSubsystem floorSubsystem = new FloorSubsystem(null, fileName); // Using null for SharedDataInterface for simplicity

        // Action - Simulate sending a request (this could be part of the run method or a separate method that you call)
        floorSubsystem.sendFloorRequests();

    }
    public void testParseTimeToMillis() throws SocketException {
        FloorSubsystem floorSubsystem = new FloorSubsystem(null, ""); // Arguments are placeholders
        long expectedMillis = SOME_EXPECTED_VALUE; // Calculate the expected milliseconds for a known input time
        long actualMillis = floorSubsystem.parseTimeToMillis("12:01:01.1");
        assertEquals(expectedMillis, actualMillis, "Time parsing should match expected milliseconds.");
    }
    public void testFormatFloorRequest() throws SocketException {
        FloorSubsystem floorSubsystem = new FloorSubsystem(null, ""); // Arguments are placeholders
        String inputLine = "12:01:01.1 2 UP 3"; // Example input line from the file
        String expectedFormat = "EXPECTED_FORMAT_STRING"; // The expected format after processing
        String actualFormat = floorSubsystem.formatFloorRequest(inputLine);
        assertEquals(expectedFormat, actualFormat, "Formatted floor request should match expected format.");
    }


    // Main method to run the test
    public static void main(String[] args) throws SocketException {
        FloorSubsystemTest test = new FloorSubsystemTest();
        test.testDoorOpensOnRequest();
    }
}
