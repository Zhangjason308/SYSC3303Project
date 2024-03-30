package SYSC3303Project.Elevator;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.StringUtil;
import SYSC3303Project.SharedDataInterface;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;


/**
 * ElevatorSubsystem.java
 * This class represents the Elevator subsystem, which executes commands, based on information it receives from the Scheduler.
 * The Elevator implements a state machine to keep track of its functions and state.
 */

public class ElevatorSubsystem implements Runnable {

    int height = 0;
    private int currentFloor = 1; // Starting floor
    public int getCurrentFloor() {return currentFloor;}

    public void setCurrentFloor(int currentFloor) {this.currentFloor = currentFloor;}

    private Direction direction;
    private ElevatorStateMachine elevatorStateMachine;

    DatagramPacket sendPacket, receivePacket;

    private SharedDataInterface sharedData;
    public ElevatorStateMachine getElevatorStateMachine() {return elevatorStateMachine;}

    private DatagramSocket sendSocket,receiveSocket;

    private FloorData command = null;

    private int id;
    private final List<AbstractMap.SimpleEntry<Integer, Integer>> targetFloors =
            Collections.synchronizedList(new ArrayList<>());
    private List<DoorFault> doorFaults = new ArrayList<>();


    private boolean isDoorStuckOpen = false;
    private boolean isDoorStuckClosed = false;

    public String TestTime;
    public int TestArrivalFloor;
    public Direction TestDirection;
    public int TestDestinationFloor;
    public String TestString;
    public String TestMoveTo;


    public ElevatorSubsystem(SharedDataInterface sharedData, int id) {
        this.id = id;
        this.sharedData = sharedData;
        this.elevatorStateMachine = new ElevatorStateMachine();
        this.direction = Direction.STATIONARY;

        try {
            this.sendSocket = new DatagramSocket(id);
            System.out.println("ELEVATOR [" + id + "]: SENDING PACKETS ON PORT: " + sendSocket.getLocalPort());
            this.receiveSocket = new DatagramSocket(id + 1);
            System.out.println("ELEVATOR [" + id + "]: RECEIVING PACKETS ON PORT: " + receiveSocket.getLocalPort());
        }
        catch (SocketException se){
            se.printStackTrace();
        }
    }

    public String getElevatorStatus() {
        // Convert target floors list to a string representation
        String targetFloorsStr = targetFloors.isEmpty() ? "None" :
                targetFloors.stream()
                        .map(pair -> String.format("(%d->%d)", pair.getKey(), pair.getValue()))
                        .collect(Collectors.joining("+"));

        // Convert doorFaults list to a string representation
        String doorFaultsStr = doorFaults.isEmpty() ? "No Door Faults" :
                doorFaults.stream()
                        .map(fault -> String.format("%s:%d", fault.getFaultType(), fault.getRetryAttempts()))
                        .collect(Collectors.joining(", "));

        // Construct the full status string including the door faults
        return String.format("%d,%d,%s,%s,%s,%s",
                id,
                currentFloor,
                direction,
                elevatorStateMachine.getCurrentState().toString(),
                targetFloorsStr,
                doorFaultsStr);
    }

    // Task for sending and receiving commands
    private Runnable sendAndReceiveTask = () -> {
        try {
            // Continuously send and receive commands
            while (!Thread.currentThread().isInterrupted()) {
                sendAndReceive();
            }
        } catch (InterruptedException | UnknownHostException e) {
            e.printStackTrace();
        }
    };


    public void sendAndReceive() throws UnknownHostException, InterruptedException {
        int attempt = 0;
        boolean receivedResponse = false;
        boolean sentStatus = false;
        handleDoorFaults();
        while (attempt < 1 && !receivedResponse) {
            if (!sentStatus) {
                rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id + 10);
                sentStatus = true;
            }
            try {
                // Assuming rpcReceive is now correctly designed to return a FloorData object
                String rawData = rpcReceive(receiveSocket, new DatagramPacket(new byte[1024], 1024), 1024);
                receivedResponse = true;

                if (rawData.contains("DOOR_FAULT")) {
                    // Handle the fault
                    injectDoorFault(rawData);
                    TestString = "---------- ELEVATOR [" + id + "]: Received Door Fault: " + rawData + " ----------\n";
                    System.out.println("---------- ELEVATOR [" + id + "]: Received Door Fault: " + rawData + " ----------\n");
                    rpcSend("DOOR_FAULT RECEIVED", sendSocket, InetAddress.getLocalHost(), id + 10);
                } else {
                    System.out.println("---------- ELEVATOR [" + id + "]: Received  Command: " + command + " ----------\n");

                    FloorData command = convertStringToFloorData(rawData);

                    TestTime = command.getTime();
                    TestArrivalFloor = command.getArrivalFloor();
                    TestDirection = command.getDirection();
                    TestDestinationFloor = command.getDestinationFloor();

                    // Add to targetFloors list upon receiving a valid command
                    synchronized (this) {
                        targetFloors.add(new AbstractMap.SimpleEntry<>(command.getArrivalFloor(), command.getDestinationFloor()));
                    }
                    TestString = "---------- ELEVATOR [" + id + "]: Received Command: " + command + " ----------\n";
                    System.out.println("---------- ELEVATOR [" + id + "]: Received Command: " + command + " ----------\n");
                    //processCommand(command);
                    // Indicate command received
                    rpcSend("200", sendSocket, InetAddress.getLocalHost(), 6);
                }
            } catch (SocketTimeoutException ste) {
                attempt++;
            }
        }

        if (!receivedResponse) {
            System.out.println("ELEVATOR [" + id + "]: No request from Scheduler");
        }
    }

    private FloorData convertStringToFloorData(String rawData) {
        try {
            String[] parts = rawData.split(",");
            String time = parts[0];
            int arrivalFloor = Integer.parseInt(parts[1]);
            Direction direction = Direction.valueOf(parts[2].toUpperCase()); // Assuming 'parts[2]' is the direction string
            int destinationFloor = Integer.parseInt(parts[3]);
            return new FloorData(time, arrivalFloor, direction, destinationFloor);
        } catch (Exception e) {
            System.err.println("Error parsing FloorData from received string: " + rawData);
            return null;
        }
    }


    private void injectDoorFault(String doorFault) {

        String[] parts = doorFault.split(" ");

        if (parts.length < 5 || !parts[1].equals("DOOR_FAULT")) {
            System.out.println("Invalid fault string: " + doorFault);
            return;
        }

        // Extract the components of the fault string
        String faultType = parts[3]; // Combines to form something like "STUCK_OPEN"
        int retryAttempts = Integer.parseInt(parts[4]); // Number of retry attempts
        doorFaults.add(new DoorFault(faultType, retryAttempts));
        System.out.println("ELEVATOR [" + id + "]: Injected fault: " + faultType + " with " + retryAttempts + " retry attempts");
        // Set the state machine's state based on the fault type
        switch (faultType.toUpperCase()) {
            case "STUCK_OPEN":
                elevatorStateMachine.setState("DoorsOpen");
                break;
            case "STUCK_CLOSED":
                elevatorStateMachine.setState("DoorsClosed");
                break;
            // Handle other fault types as necessary
            default:
                System.out.println("ELEVATOR [" + id + "]: Unknown or unhandled fault type for state transition: " + faultType);
                break;
        }
    }
    public void handleDoorFaults() throws UnknownHostException {

        Iterator<DoorFault> iterator = doorFaults.iterator();
        while (iterator.hasNext()) {
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

            DoorFault fault = iterator.next();
            boolean resolved = false;

            switch (fault.getFaultType()) {
                case "STUCK_OPEN":
                    resolved = attemptToCloseDoors();
                    if (resolved) {
                        System.out.println("ELEVATOR [" + id + "]:Attempt to close doors...SUCCESS");
                        elevatorStateMachine.setState("DoorsClosed");
                    }
                    break;
                case "STUCK_CLOSED":
                    resolved = attemptToOpenDoors();
                    if (resolved) {
                        System.out.println("ELEVATOR [" + id + "]:Attempt to open doors...SUCCESS");
                        elevatorStateMachine.setState("DoorsOpen");
                    }
                    break;
                // Add other fault types and their resolutions here
            }

            fault.decrementRetryAttempts();

            if (resolved) {
                iterator.remove(); // Remove the fault if resolved
            } else if (fault.getRetryAttempts() <= 0) {
                // If no retry attempts left and not resolved, handle as a HardFault
                handleHardFault(fault.getFaultType());
                iterator.remove(); // Optionally remove the fault after handling as a HardFault
            }
        }
    }

    private void handleHardFault(String hardFault) {
        // Logic to handle a HardFault
        System.out.println("HardFault detected: " + hardFault + ". Manual intervention required.");
        // Transition the state machine to a fault state or take other appropriate actions
        elevatorStateMachine.setState("Disabled");

    }

    private boolean attemptToCloseDoors() {
        // Simulate door closing logic
        try {
            System.out.println("ELEVATOR [" + id + "]:Attempting to close doors...");
            sleep(ElevatorTiming.DOORS_CLOSING); // Simulate the time to close doors
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        // Assume a fixed chance of success/failure for simplicity; adjust as needed
        return Math.random() > 0.5;
    }

    private boolean attemptToOpenDoors() {
        // Simulate door opening logic
        try {
            System.out.println("ELEVATOR [" + id + "]:Attempting to open doors...");
            sleep(ElevatorTiming.DOORS_OPENING); // Simulate the time to open doors
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        // Assume a fixed chance of success/failure for simplicity; adjust as needed
        return Math.random() > 0.5;
    }




    private final Object lock = new Object(); // Object used for synchronization

    public void run() {
        // Start the thread for sending and receiving commands
        Thread sendAndReceiveThread = new Thread(sendAndReceiveTask);
        sendAndReceiveThread.start();

        while (!Thread.currentThread().isInterrupted()) {
            synchronized (lock) {
                // Process the received command
                try {

                    processCommands();
                    sleep(1000);
                } catch (InterruptedException | UnknownHostException e) {
                    System.out.println("---------- ELEVATOR [" + id + "] SUBSYSTEM INTERRUPTED ---------- ");
                    Thread.currentThread().interrupt();
                    return;
                }

                // Reset command to null
                command = null;
            }
        }
    }


    private String rpcReceive(DatagramSocket socket, DatagramPacket packet, int byteArrSize) throws SocketTimeoutException {
        byte[] data = new byte[byteArrSize];
        packet = new DatagramPacket(data, data.length);

        try {
            socket.setSoTimeout(5000);
            socket.receive(packet);
        } catch (IOException e) {
            throw new SocketTimeoutException();
        }
        // Assuming StringUtil.getStringFormat converts byte data to a String
        String receivedData = StringUtil.getStringFormat(packet.getData(), packet.getLength());
        System.out.println(Thread.currentThread().getName() + ": Packet Received From Scheduler: " + receivedData);
        return receivedData; // Return the raw data as String
    }

    private void rpcSend(String command, DatagramSocket socket, InetAddress address, int port) {
        try {

            // Convert the updated command string to bytes
            byte[] floorDataBytes = command.getBytes(StandardCharsets.UTF_8);

            // Create a DatagramPacket with the updated FloorData bytes
            DatagramPacket packet = new DatagramPacket(floorDataBytes, floorDataBytes.length, address, port);

            // Send the packet
            socket.send(packet);

            // Log the action
            System.out.println("ELEVATOR [" + id + "]: Packet Sent To Scheduler: " + StringUtil.getStringFormat(packet));
        } catch (IOException e) {
            System.err.println("ELEVATOR [" + id + "]: IOException in sendFloorData: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void processCommands() throws InterruptedException, UnknownHostException {
        System.out.println("---------- ELEVATOR [" + id + "]: Processing Commands ----------\n");

        boolean processingCompleted = false;

        while (!processingCompleted) {
            synchronized (targetFloors) {
                for (AbstractMap.SimpleEntry<Integer, Integer> entry : targetFloors) {
                    int destinationFloor = entry.getKey();
                    int arrivalFloor = entry.getValue(); // Assuming elevator is already at currentFloor for now
                    direction = determineDirection(arrivalFloor, destinationFloor);

                    // Elevator moves to arrival floor to pickup passengers
                    if (arrivalFloor != currentFloor) {
                        moveToFloor(arrivalFloor);
                    } else {
                        TestMoveTo = "---------- ELEVATOR [" + id + "]: Already at floor " + currentFloor + " ----------\n";
                        System.out.println("---------- ELEVATOR [" + id + "]: Already at floor " + currentFloor + " ----------\n");
                        performStopActions(); // Use performStopActions for opening and closing doors
                    }

                    // Now move to the destination floor if it's different from the arrival floor
                    if (destinationFloor != currentFloor) {
                        moveToFloor(destinationFloor);
                    }

                    // Additional processing can be done here if needed, such as finalizing the trip
                }
                targetFloors.clear(); // Clear the list after processing
            }

            // After processing all target floors, set processingCompleted to true
            processingCompleted = true;
        }
    }


    public Direction determineDirection(int currentFloor, int destinationFloor) {
        if (currentFloor < destinationFloor) {
            return Direction.UP;
        } else if (currentFloor > destinationFloor) {
            return Direction.DOWN;
        } else {
            return Direction.STATIONARY;
        }
    }

    public List<Integer> TestMoveFromTo = new ArrayList<>();
    public void moveToFloor(int destinationFloor) throws UnknownHostException, InterruptedException {
        System.out.println("---------- ELEVATOR [" + id + "]: Moving from floor " + currentFloor + " to floor " + destinationFloor + " ----------\n");

        while (currentFloor != destinationFloor) {
            TestMoveFromTo.add(currentFloor);
            if (currentFloor < destinationFloor) {
                goUp();
            } else {
                goDown();
            }

            // Check for intermediary stops
            if (shouldStopAtFloor(currentFloor)) {
                performStopActions();
            }
        }
        TestMoveFromTo.add(currentFloor);
        // Perform actions at the destination floor
        performStopActions();
    }

    private boolean shouldStopAtFloor(int floor) {
        for (AbstractMap.SimpleEntry<Integer, Integer> entry : targetFloors) {
            int arrivalFloor = entry.getKey();
            int destinationFloor = entry.getValue();

            // For UP direction, check if the elevator is moving towards a request, and the floor is a valid stop
            if (direction == Direction.UP && floor >= arrivalFloor && floor <= destinationFloor) {
                return true; // A valid stop found
            }

            // For DOWN direction, check if the elevator is moving towards a request, and the floor is a valid stop
            if (direction == Direction.DOWN && floor <= arrivalFloor && floor >= destinationFloor) {
                return true; // A valid stop found
            }
        }
        return false; // No valid stop found
    }





    // Assuming targetFloors is a Map<Integer, Integer> where key is the passenger ID and value is the destination floor
    private void performStopActions() throws UnknownHostException, InterruptedException {
        System.out.println("ELEVATOR [" + id + "]: Stopping at floor " + currentFloor + " ----------\n");
        elevatorStateMachine.setState("DoorsOpen");
        // Assume rpcSend is a method that sends the elevator's status somewhere
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        sleep(10000); // Simulate time for passengers to alight and board

        // Remove passengers whose destination is the current floor and whose travel direction matches the elevator's direction
        targetFloors.removeIf(entry -> {
            int arrivalFloor = entry.getKey();
            int destinationFloor = entry.getValue();

            // For UP direction, only remove if the elevator is going from a lower to a higher floor
            // and the current floor matches their destination floor
            boolean removeForUp = direction == Direction.UP && arrivalFloor < destinationFloor && destinationFloor == currentFloor;

            // For DOWN direction, only remove if the elevator is going from a higher to a lower floor
            // and the current floor matches their destination floor
            boolean removeForDown = direction == Direction.DOWN && arrivalFloor > destinationFloor && destinationFloor == currentFloor;

            return removeForUp || removeForDown;
        });

        elevatorStateMachine.setState("DoorsClosed");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
    }



    public int TestWaitingTime = 0;
    private void goUp() throws InterruptedException, UnknownHostException {
        // Simulate the movement up by one floor with acceleration, cruising (if applicable), and deceleration
        direction = Direction.UP;
        elevatorStateMachine.setState("Accelerating");
        System.out.println("ELEVATOR [" + id + "]: Accelerating Upward");
        sleep(1500); // Acceleration for half a floor
        TestWaitingTime += 1500;
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

        // Assuming cruising for half a floor if needed (can adjust based on actual mechanics or omit if not needed)
        System.out.println("ELEVATOR [" + id + "]: Cruising Upward");
        elevatorStateMachine.setState("Cruising");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

        sleep(10000); // Cruising for half a floor
        TestWaitingTime += 1000;
        elevatorStateMachine.setState("Decelerating");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

        System.out.println("ELEVATOR [" + id + "]: Decelerating Upward");
        sleep(15000); // Deceleration for half a floor
        TestWaitingTime += 1500;
        elevatorStateMachine.setState("Stopped");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

        currentFloor++; // Successfully moved up by one floor
        System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
    }

    private void goDown() throws InterruptedException, UnknownHostException {
        // Simulate the movement down by one floor with acceleration, cruising (if applicable), and deceleration
        direction = Direction.DOWN;

        System.out.println("ELEVATOR [" + id + "]: Accelerating Downward");
        sleep(1500); // Acceleration for half a floor
        TestWaitingTime += 1500;

        // Assuming cruising for half a floor if needed (can adjust based on actual mechanics or omit if not needed)
        System.out.println("ELEVATOR [" + id + "]: Cruising Downward");
        sleep(10000); // Cruising for half a floor
        TestWaitingTime += 1000;

        System.out.println("ELEVATOR [" + id + "]: Decelerating Downward");
        sleep(1500); // Deceleration for half a floor
        TestWaitingTime += 1500;

        currentFloor--; // Successfully moved down by one floor
        System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
    }

    public void addDoorFault(DoorFault fault) {
        this.doorFaults.add(fault);
    }

    public List<DoorFault> getDoorFaults() {
        return doorFaults;
    }

    public void simulateHandleDoorFaults() {
        this.doorFaults.clear(); // Simplistically assuming all faults are "resolved" immediately for testing
    }

    public static void main(String args[]) {
        try {
            // Retrieve the shared data interface from the RMI registry
            SharedDataInterface sharedData = (SharedDataInterface) Naming.lookup("rmi://localhost/SharedData");
            // Create Elevator subsystem
            ElevatorSubsystem elevatorSubsystem10 = new ElevatorSubsystem(sharedData, 10);
            ElevatorSubsystem elevatorSubsystem12 = new ElevatorSubsystem(sharedData, 12);
            ElevatorSubsystem elevatorSubsystem14 = new ElevatorSubsystem(sharedData, 14);
            ElevatorSubsystem elevatorSubsystem16 = new ElevatorSubsystem(sharedData, 16);
            // Create Elevator thread
            Thread elevatorThread10 = new Thread(elevatorSubsystem10, "Elevator10");
            Thread elevatorThread12 = new Thread(elevatorSubsystem12, "Elevator12");
            Thread elevatorThread14 = new Thread(elevatorSubsystem14, "Elevator14");
            Thread elevatorThread16 = new Thread(elevatorSubsystem16, "Elevator16");


            // Start the elevator thread
            elevatorThread10.start();
            elevatorThread12.start();
            elevatorThread14.start();
            elevatorThread16.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}