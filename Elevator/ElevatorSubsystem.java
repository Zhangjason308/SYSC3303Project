package SYSC3303Project.Elevator;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.StringUtil;
import SYSC3303Project.SharedDataInterface;
import SYSC3303Project.SimulatedClockSingleton;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.util.*;
import java.util.concurrent.*;
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
    private boolean infiniteVoid = false;

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
    //private final CopyOnWriteArrayList<AbstractMap.SimpleEntry<Integer, Integer>> targetFloors = new CopyOnWriteArrayList<>();

    private final BlockingQueue<Map<Integer, Integer>> targetFloors = new LinkedBlockingQueue<>();

    private List<DoorFault> doorFaults = new ArrayList<>();
    private List<HardFault> hardFaults = new ArrayList<>();

    private boolean isDoorStuckOpen = false;
    private boolean isDoorStuckClosed = false;

    public String TestTime;
    public int TestArrivalFloor;
    public Direction TestDirection;
    public int TestDestinationFloor;
    public String TestString;
    public String TestMoveTo;

    SimulatedClockSingleton clock;



    public ElevatorSubsystem(SharedDataInterface sharedData, int id) {
        this.clock = SimulatedClockSingleton.getInstance();
        clock.printCurrentTime();
        this.id = id;
        this.sharedData = sharedData;
        this.elevatorStateMachine = new ElevatorStateMachine();
        this.direction = Direction.STATIONARY;

        try {
            this.sendSocket = new DatagramSocket(id);
            System.out.println("ELEVATOR [" + id + "]: SENDING PACKETS ON PORT: " + sendSocket.getLocalPort());
            this.receiveSocket = new DatagramSocket(id + 1);
            System.out.println("ELEVATOR [" + id + "]: RECEIVING PACKETS ON PORT: " + receiveSocket.getLocalPort());
            sendStatusUpdate();
        }
        catch (SocketException se){
            se.printStackTrace();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendStatusUpdate() throws UnknownHostException {
        String status = getElevatorStatus();
        rpcSend(status, sendSocket, InetAddress.getLocalHost(), id + 10); // Assuming the Scheduler is listening on id+10
    }

    public String getElevatorStatus() {
        StringBuilder targetFloorsStr = new StringBuilder();
        targetFloors.forEach(commandMap -> {
            commandMap.forEach((arrivalFloor, destinationFloor) -> {
                if (targetFloorsStr.length() > 0) {
                    targetFloorsStr.append("+");
                }
                targetFloorsStr.append(String.format("(%d->%d)", arrivalFloor, destinationFloor));
            });
        });

        // Handle case where there are no commands in the queue
        if (targetFloorsStr.length() == 0) {
            targetFloorsStr.append("None");
        }

        // Convert doorFaults list to a string representation
        String doorFaultsStr = doorFaults.isEmpty() ? "No Door Faults" :
                doorFaults.stream()
                        .map(fault -> String.format("%s:%d", fault.getFaultType(), fault.getRetryAttempts()))
                        .collect(Collectors.joining(", "));
        // Convert doorFaults list to a string representation
        String hardFaultsStr = hardFaults.isEmpty() ? "No Door Faults" :
                hardFaults.stream()
                        .map(fault -> String.format("%s:%d", fault.getFaultType()))
                        .collect(Collectors.joining(", "));

        // Construct the full status string including the door faults
        return String.format("%d,%d,%s,%s,%s,%s,%s",
                id,
                currentFloor,
                direction,
                elevatorStateMachine.getCurrentState().toString(),
                targetFloorsStr,
                doorFaultsStr,
                hardFaultsStr);
    }

    // Task for sending and receiving commands
    private Runnable sendAndReceiveTask = () -> {
        // Continuously send and receive commands
        while (!Thread.currentThread().isInterrupted()) {
            //sendAndReceive();
        }
    };


//    public void sendAndReceive() throws UnknownHostException, InterruptedException {
//        handleDoorFaults();
//            try {
//                // Assuming rpcReceive is now correctly designed to return a FloorData object
//                String rawData = rpcReceive(receiveSocket, new DatagramPacket(new byte[1024], 1024), 1024);
//
//                if (rawData.contains("DOOR_FAULT")) {
//                    // Handle the fault
//                    injectDoorFault(rawData);
//                    TestString = "---------- ELEVATOR [" + id + "]: Received Door Fault: " + rawData + " ----------\n";
//                    System.out.println("---------- ELEVATOR [" + id + "]: Received Door Fault: " + rawData + " ----------\n");
//                    rpcSend("DOOR_FAULT RECEIVED", sendSocket, InetAddress.getLocalHost(), id + 10);
//                } else {
//                    FloorData command = convertStringToFloorData(rawData);
//                    System.out.println("---------- ELEVATOR [" + id + "]: Received Command: " + command + " ----------\n");
//
//
//                    TestTime = command.getTime();
//                    TestArrivalFloor = command.getArrivalFloor();
//                    TestDirection = command.getDirection();
//                    TestDestinationFloor = command.getDestinationFloor();
//
//                    // Add to targetFloors list upon receiving a valid command
//                    synchronized (this) {
//                        targetFloors.add(new AbstractMap.SimpleEntry<>(command.getArrivalFloor(), command.getDestinationFloor()));
//                    }
//                    TestString = "---------- ELEVATOR [" + id + "]: Received Command: " + command + " ----------\n";
//                    //processCommand(command);
//                    // Indicate command received
//                    //rpcSend("200", sendSocket, InetAddress.getLocalHost(), 6);
//                    sendStatusUpdate();
//                }
//            } catch (SocketTimeoutException ste) {
//        }
//
//    }

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
            case "HARD_FAULT":
                this.infiniteVoid = true;
                System.out.println("ELEVATOR [" + id + "]: HARD FAULT INJECTED");

                break;
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
            switch (fault.getFaultType()) {
                case "STUCK_OPEN":
                    if (attemptToCloseDoors()) {
                        System.out.println("ELEVATOR [" + id + "]: Attempt to close doors...SUCCESS");
                        elevatorStateMachine.setState("DoorsClosed");
                    } else {
                        System.out.println("ELEVATOR [" + id + "]: Attempt to close doors...FAILED");
                    }
                    break;
                case "STUCK_CLOSED":
                    if (attemptToOpenDoors()) {
                        System.out.println("ELEVATOR [" + id + "]: Attempt to open doors...SUCCESS");
                        elevatorStateMachine.setState("DoorsOpen");
                    } else {
                        System.out.println("ELEVATOR [" + id + "]: Attempt to open doors...FAILED");
                    }
                    break;
                // No need to handle other fault types specifically
            }
            // Remove the fault after handling, regardless of the outcome
            iterator.remove();
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
            sleep(20000); // Simulate the time to close doors
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        // Assume a fixed chance of success/failure for simplicity; adjust as needed
        return true;
    }

    private boolean attemptToOpenDoors() {
        // Simulate door opening logic
        try {
            System.out.println("ELEVATOR [" + id + "]:Attempting to open doors...");
            sleep(20000); // Simulate the time to open doors
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        // Assume a fixed chance of success/failure for simplicity; adjust as needed
        return true;
    }



    private final Semaphore commandAvailable = new Semaphore(0);

    private final Object lock = new Object(); // Object used for synchronization

    @Override
    public void run() {
        Thread commandReceiver = new Thread(this::receiveCommands);
        commandReceiver.start();

        Thread commandProcessor = new Thread(this::processCommands);
        commandProcessor.start();

        try {
            commandReceiver.join();
            commandProcessor.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void receiveCommands() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Assuming rpcReceive is now designed to return a String representing the command
                    String rawData = rpcReceive(receiveSocket, new DatagramPacket(new byte[1024], 1024), 1024);
                    if (rawData.contains("FAULT")) {
                        // Handle the fault
                        injectDoorFault(rawData);
                        TestString = "---------- ELEVATOR [" + id + "]: Received  Fault: " + rawData + " ----------\n";
                        System.out.println("---------- ELEVATOR [" + id + "]: Received  Fault: " + rawData + " ----------\n");
                        rpcSend("FAULT RECEIVED", sendSocket, InetAddress.getLocalHost(), id + 10);
                    } else {
                        FloorData command = convertStringToFloorData(rawData);
                        if (command != null) {
                            // Map the arrival floor to the destination floor
                            Map<Integer, Integer> commandMap = Collections.singletonMap(command.getArrivalFloor(), command.getDestinationFloor());
                            targetFloors.put(commandMap);
                            System.out.println("Received and queued command: " + command);
                            commandAvailable.release(); // Signal that a command is available
                        }
                    }
                } catch (SocketTimeoutException ste) {
                    // Expected if there's no incoming command; can choose to log or silently ignore
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                    System.out.println("Interrupted while receiving commands.");
                } catch (IOException e) {
                    System.err.println("IOException in receiveCommands: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, "ReceiveCommandsThread").start();
    }

//    public void run() {
//        // Start the thread for sending and receiving commands
//        try {
//            sendStatusUpdate();
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
//
//        while (!Thread.currentThread().isInterrupted()) {
//
//                // Process the received command
//                try {
//                    command = attemptReceiveCommand();
//                    if (command != null) {
//                        System.out.println("Received Command: " + command);
//                        targetFloors.add(new AbstractMap.SimpleEntry<>(command.getArrivalFloor(), command.getDestinationFloor()));
//                        // Process the received command
//                        processCommands();
//                        // Reset command to null after processing
//                        command = null;
//                    }
//                } catch (InterruptedException | UnknownHostException e) {
//                    System.out.println("---------- ELEVATOR [" + id + "] SUBSYSTEM INTERRUPTED ---------- ");
//                    Thread.currentThread().interrupt();
//                    return;
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            // Reset command to null
//                command = null;
//
//        }
//    }

    private FloorData attemptReceiveCommand() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
        try {
            receiveSocket.setSoTimeout(100); // Set a short timeout to make the receive non-blocking
            String rawData = rpcReceive(receiveSocket, new DatagramPacket(new byte[1024], 1024), 1024);
            return convertStringToFloorData(rawData); // Assuming this method exists and parses the raw data
        } catch (SocketTimeoutException e) {
            // Expected if there's no data to receive
            return null;
        }
    }


    private String rpcReceive(DatagramSocket socket, DatagramPacket packet, int byteArrSize) throws SocketTimeoutException {
        byte[] data = new byte[byteArrSize];
        packet = new DatagramPacket(data, data.length);

        try {
            socket.setSoTimeout(2000);
            socket.receive(packet);
        } catch (IOException e) {
            throw new SocketTimeoutException();
        }
        // Assuming StringUtil.getStringFormat converts byte data to a String
        String receivedData = StringUtil.getStringFormat(packet.getData(), packet.getLength());
        clock.printCurrentTime();
        System.out.println(Thread.currentThread().getName() + ": Packet Received From Scheduler: " + receivedData);
        String name = Thread.currentThread().getName();
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
            clock.printCurrentTime();
            System.out.println( "ELEVATOR [" + id + "]: Packet Sent To Scheduler: " + StringUtil.getStringFormat(packet));
        } catch (IOException e) {
            System.err.println("ELEVATOR [" + id + "]: IOException in sendFloorData: " + e.getMessage());
            e.printStackTrace();
        }
    }



//    public void processCommands() throws InterruptedException, UnknownHostException {
//        System.out.println("---------- ELEVATOR [" + id + "]: Processing Commands ----------\n");
//
//        boolean processingCompleted = false;
//
//        while (!processingCompleted) {
//            // Direct iteration on CopyOnWriteArrayList is safe from ConcurrentModificationException
//            for (AbstractMap.SimpleEntry<Integer, Integer> entry : targetFloors) {
//                int destinationFloor = entry.getValue();
//                int arrivalFloor = entry.getKey(); // Assuming elevator is already at currentFloor for now
//                direction = determineDirection(arrivalFloor, destinationFloor);
//
//                // Elevator moves to arrival floor to pick up passengers
//                if (arrivalFloor != currentFloor) {
//                    moveToFloor(arrivalFloor);
//                } else {
//                    TestMoveTo = "---------- ELEVATOR [" + id + "]: Already at floor " + currentFloor + " ----------\n";
//                    System.out.println("---------- ELEVATOR [" + id + "]: Already at floor " + currentFloor + " ----------\n");
//                    performStopActions(); // For opening and closing doors
//                }
//
//                // Move to the destination floor if it's different from the arrival floor
//                if (destinationFloor != currentFloor) {
//                    moveToFloor(destinationFloor);
//                }
//
//                // The entry can be safely removed after its use
//                targetFloors.remove(entry);
//            }
//
//            // After processing all target floors, exit the loop
//            if (targetFloors.isEmpty()) {
//                processingCompleted = true;
//            }
//        }
//    }

    //private final BlockingQueue<Map<Integer, Integer>> commandQueue = new LinkedBlockingQueue<>();
    // Track in-transit state for commands

    private void processCommands() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Map<Integer, Integer> commandMap = null;

                // Wait for a new command if necessary
                commandAvailable.acquire();

                // Synchronize access to the command queue
                synchronized (lock) {
                    commandMap = targetFloors.peek(); // Look at the next command without removing it
                }

                if (commandMap != null) {
                    // Extracting the only entry in the map
                    Map.Entry<Integer, Integer> commandEntry = commandMap.entrySet().iterator().next();
                    int arrivalFloor = commandEntry.getKey();
                    int destinationFloor = commandEntry.getValue();

                    // Move to the arrival floor if not already there
                    if (currentFloor != arrivalFloor) {
                        moveToFloor(arrivalFloor);
                    }

                    // Once at the arrival floor, check if we also need to move to the destination floor
                    if (currentFloor != destinationFloor) {
                        moveToFloor(destinationFloor);
                    }

                    // After reaching the destination floor, complete the command's processing
                    if (currentFloor == destinationFloor) {
                        synchronized (lock) {
                            // Confirm the command to remove matches the one intended, to avoid race conditions
                            Map<Integer, Integer> confirmedCommandMap = targetFloors.peek();
                            if (confirmedCommandMap != null && confirmedCommandMap.equals(commandMap)) {
                                System.out.println("------------------0-------------------");
                                performStopActions(); // Actions to take at destination floor
                                targetFloors.poll(); // Removes the command as it's completed
                                rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
                            }
                        }
                    }
                } else {
                    // Optionally sleep to avoid busy waiting
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                break; // Exit the loop if the thread is interrupted
            } catch (Exception e) {
                e.printStackTrace(); // Handle other exceptions appropriately
            }
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
            if (shouldStopAtFloor(currentFloor) != null) {
                System.out.println("------------------1-------------------");
                performStopActions();
            }
        }
        TestMoveFromTo.add(currentFloor);
        // Perform actions at the destination floor
        System.out.println("------------------2-------------------");
        performStopActions();
    }

    private List<AbstractMap.SimpleEntry<Integer, Integer>> shouldStopAtFloor(int floor) {
        // Temporary storage for commands not related to the current stop
        List<Map<Integer, Integer>> tempCommands = new ArrayList<>();
        List<Map<Integer, Integer>> commandsForStop = new ArrayList<>();
        int earliestDestinationFloor = Integer.MAX_VALUE;
        boolean isArrivalFloorStop = false;

        // Drain the queue to process commands
        targetFloors.drainTo(tempCommands);

        // Identify if there's a need to stop at the current floor as an arrival or destination floor
        for (Map<Integer, Integer> command : tempCommands) {
            for (Map.Entry<Integer, Integer> entry : command.entrySet()) {
                int arrivalFloor = entry.getKey();
                int destinationFloor = entry.getValue();

                // Determine if the current floor is an arrival floor
                if (floor == arrivalFloor) {
                    isArrivalFloorStop = true;
                    commandsForStop.add(command);
                    break; // Stop further checks if the current floor is an arrival floor
                }

                // For destination floor, find the earliest one
                if (floor == destinationFloor && destinationFloor <= earliestDestinationFloor) {
                    earliestDestinationFloor = destinationFloor;
                    commandsForStop.add(command);
                }
            }

            if (isArrivalFloorStop) break; // If stopping for an arrival floor, no need to check further
        }

        // Re-queue commands that do not require stopping at the current floor
        for (Map<Integer, Integer> command : tempCommands) {
            if (!commandsForStop.contains(command)) {
                targetFloors.offer(command);
            } else if (isArrivalFloorStop && floor != earliestDestinationFloor) {
                // If the stop is not for an arrival floor or the earliest destination, re-queue it
                targetFloors.offer(command);
            }
        }
        List<AbstractMap.SimpleEntry<Integer, Integer>> entriesForStop = new ArrayList<>();

        // If a stop is required, return the corresponding entry
        if (!commandsForStop.isEmpty()) {
            for (Map<Integer, Integer> command : commandsForStop) {
                Map.Entry<Integer, Integer> entry = command.entrySet().iterator().next();
                entriesForStop.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
            return entriesForStop; // Return the list of stopping points

        }

        return null; // No stop at this floor
    }



//    private AbstractMap.SimpleEntry<Integer, Integer> shouldStopAtFloor(int floor) {
//        // Temporarily store commands that are not related to the current stop
//        List<Map<Integer, Integer>> tempCommands = new ArrayList<>();
//
//        Map<Integer, Integer> stopCommand = null;
//
//        // Drain the queue to process commands
//        targetFloors.drainTo(tempCommands);
//        boolean isDestination = false;
//
//        for (Map<Integer, Integer> command : tempCommands) {
//            for (Map.Entry<Integer, Integer> entry : command.entrySet()) {
//                isDestination = false;
//
//                int arrivalFloor = entry.getKey();
//                int destinationFloor = entry.getValue();
//                // Check if the elevator should stop at the current floor
//                if (floor == arrivalFloor || floor == destinationFloor) {
//                    if (floor == destinationFloor) {
//                        isDestination = true;
//                    }
//                    stopCommand = command;
//                    break;
//                }
//            }
//            if (stopCommand != null) {
//                break;
//            }
//        }
//
//        // Re-add the commands that are not related to the current stop back to the queue
//        for (Map<Integer, Integer> command : tempCommands) {
//            if (command != stopCommand || (command == stopCommand && !isDestination)) {
//                targetFloors.offer(command);
//            }
//        }
//
//        // Convert stopCommand to SimpleEntry if it's not null, indicating a stop is needed
//        if (stopCommand != null && !isDestination) {
//            Map.Entry<Integer, Integer> entry = stopCommand.entrySet().iterator().next();
//            return new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
//        }
//
//        return null; // No stop at this floor
//    }






    // Assuming targetFloors is a Map<Integer, Integer> where key is the passenger ID and value is the destination floor
    private void performStopActions() throws UnknownHostException, InterruptedException {
        System.out.println("ELEVATOR [" + id + "]: Stopping at floor " + currentFloor + " ----------\n");
        elevatorStateMachine.setState("Stopped");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        System.out.println("ELEVATOR [" + id + "]: Doors Opening " + "----------\n");
        long startTime = clock.getCurrentTime();
        if(!doorFaults.isEmpty()){
            sleep(3000);
        }
        sleep(3000); // Simulate time for doors opening (3 secs)
        long endTime = clock.getCurrentTime();
        if(startTime - endTime >= 6000){
            handleDoorFaults();
        }
        else{
            elevatorStateMachine.setState("DoorsOpen");
        }
        // Assume rpcSend is a method that sends the elevator's status somewhere
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        sleep(5000); // Simulate time for passengers to board (5 secs)

        List<Map<Integer, Integer>> toRequeue = new ArrayList<>();
        targetFloors.forEach(command -> {
            int arrivalFloor = command.keySet().iterator().next();
            int destinationFloor = command.values().iterator().next();

            // Check and re-queue commands that are not for the current floor or do not match the elevator's current direction
            if (!matchesCurrentStop(arrivalFloor, destinationFloor)) {
                toRequeue.add(command);
            }
        });

        // Clear the queue and re-queue commands that are not processed in this stop action
        targetFloors.clear();
        toRequeue.forEach(targetFloors::offer);
        System.out.println("ELEVATOR [" + id + "]: Doors Closing " + "----------\n");
        sleep(3000); // Simulate time for doors closing (3 secs)
        elevatorStateMachine.setState("DoorsClosed");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
//        if (targetFloors.isEmpty()) {
//            elevatorStateMachine.setState("Idle");
//            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
//        }
    }
    private boolean matchesCurrentStop(int arrivalFloor, int destinationFloor) {
        boolean isArrival = arrivalFloor == currentFloor;
        boolean isDestination = destinationFloor == currentFloor;
        boolean directionMatches = (direction == Direction.UP && currentFloor < destinationFloor) ||
                (direction == Direction.DOWN && currentFloor > destinationFloor);

        return isDestination && directionMatches;
    }



    public int TestWaitingTime = 0;
    private void goUp() throws InterruptedException, UnknownHostException {
        // Simulate the movement up by one floor with acceleration, cruising (if applicable), and deceleration
        long startTime = clock.getCurrentTime();
        direction = Direction.UP;
        elevatorStateMachine.setState("Moving");
        System.out.println("ELEVATOR [" + id + "]: Moving Up");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        if(!hardFaults.isEmpty()){
            sleep(5000); // Acceleration for half a floor
        }
        sleep(10000); // Acceleration for half a floor
        long endTime = clock.getCurrentTime();
        if (startTime - endTime >= 15000){
            handleHardFault(hardFaults.get(0).toString());
            return;
        }
        if (!infiniteVoid) {
            sleep(999999999);
            currentFloor++; // Successfully moved up by one floor
            System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
        }
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
    }

    private void goDown() throws InterruptedException, UnknownHostException {
        // Simulate the movement up by one floor with acceleration, cruising (if applicable), and deceleration
        long startTime = clock.getCurrentTime();
        direction = Direction.DOWN;
        elevatorStateMachine.setState("Moving");
        System.out.println("ELEVATOR [" + id + "]: Moving Down");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        if(!hardFaults.isEmpty()){
            sleep(5000); // Acceleration for half a floor
        }
        sleep(10000); // Acceleration for half a floor
        long endTime = clock.getCurrentTime();
        if (startTime - endTime >= 15000){
            handleHardFault(hardFaults.get(0).toString());
            return;
        }

        if (!infiniteVoid) {
            sleep(999999999);
            currentFloor--; // Successfully moved up by one floor
            System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
        }
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