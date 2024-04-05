package SYSC3303Project.Scheduler;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
import SYSC3303Project.Elevator.DoorFault;

//import SYSC3303Project.Elevator.ElevatorStatusReceiver;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.StringUtil;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;
import SYSC3303Project.SharedDataImpl;
import SYSC3303Project.SharedDataInterface;
import SYSC3303Project.Synchronizer;
import SYSC3303Project.SimulatedClockSingleton;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SchedulerSubsystem.java
 * This class represents the Scheduler subsystem, which receives commands from the Floor Subsystem, and sends commands to the Elevator Subsystem.
 * The Scheduler implements a state machine to keep track of its functions and state.
 */
public class SchedulerSubsystem implements Runnable {
    private ElevatorStatusGUI observer;

    private Map<FloorData, SchedulerStateMachine> commandStateMachines = new ConcurrentHashMap<>();

    private Map<Integer, ElevatorStatus> elevatorStatusMap = new HashMap<>();

    DatagramPacket receiveFSPacket, replyFSPacket, receiveESPacket, sendESPacket;
    DatagramSocket receiveFSSocket, replyFSSocket, receiveElevatorStatusSocket, sendESSocket, receiveESResponseSocket, receiveElevatorStatusSocket1, receiveElevatorStatusSocket2, receiveElevatorStatusSocket3, receiveElevatorStatusSocket4;
    SharedDataInterface sharedData;
    FloorData command;
    SimulatedClockSingleton clock;

    public String TestFault;
    public String TestInformationOfElevator;

    public SchedulerSubsystem(SharedDataInterface sharedData) throws InterruptedException {
        this.clock = SimulatedClockSingleton.getInstance();
        clock.getInstance().printCurrentTime();
        this.sharedData = sharedData;
        try {
            this.replyFSSocket = new DatagramSocket(2);
            this.receiveFSSocket = new DatagramSocket(3);
            this.sendESSocket = new DatagramSocket(5);
            this.receiveESResponseSocket = new DatagramSocket(6);
            this.receiveElevatorStatusSocket1 = new DatagramSocket(20);
            this.receiveElevatorStatusSocket2 = new DatagramSocket(22);
            this.receiveElevatorStatusSocket3 = new DatagramSocket(24);
            this.receiveElevatorStatusSocket4 = new DatagramSocket(26);
            replyFSSocket.setSoTimeout(10000);
            sendESSocket.setSoTimeout(10000);
            receiveFSSocket.setSoTimeout(10000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        //this.elevatorStatusMap = new ArrayList<>();
    }

    public void setObserver(ElevatorStatusGUI observer) {
        this.observer = observer;
    }

    protected void notifyObserver() {
        if (observer != null) {
            observer.updateElevatorStatuses(getElevatorStatusMap());
        }
    }
    public Map<Integer, ElevatorStatus> getElevatorStatusMap() {
        return elevatorStatusMap;
    }

    public void addElevatorStatus(String statusString) {
        // Check if the statusString is null or empty
        if (statusString == null || statusString.isEmpty()) {
            System.err.println("Error: Empty or null statusString received.");
            return;
        }

        String[] elevatorStatus = statusString.split(",");

        // Check if the elevatorStatus array has the expected number of elements
        if (elevatorStatus.length < 4) {
            System.err.println("Error: Invalid statusString format. Insufficient elements.");
            return;
        }

        int elevatorId;
        int currentFloor;
        Direction direction;
        String state;

        try {
            elevatorId = Integer.parseInt(elevatorStatus[0]);
            currentFloor = Integer.parseInt(elevatorStatus[1]);
            direction = Direction.valueOf(elevatorStatus[2]);
            state = elevatorStatus[3];
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Failed to parse elevator status data.");
            e.printStackTrace();
            return;
        }

        List<AbstractMap.SimpleEntry<Integer, Integer>> targetFloors = new ArrayList<>();

        // Process target floors if they exist
        if (elevatorStatus.length > 4 && !elevatorStatus[4].equals("None")) {
            String[] floorPairs = elevatorStatus[4].split("\\+");
            for (String floorPairStr : floorPairs) {
                if (floorPairStr.startsWith("(") && floorPairStr.endsWith(")")) {
                    String[] floorPair = floorPairStr.substring(1, floorPairStr.length() - 1).split("->");
                    if (floorPair.length == 2) {
                        try {
                            int arrivalFloor = Integer.parseInt(floorPair[0]);
                            int destinationFloor = Integer.parseInt(floorPair[1]);
                            targetFloors.add(new AbstractMap.SimpleEntry<>(arrivalFloor, destinationFloor));
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Failed to parse target floor data.");
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        System.err.println("Error: Invalid target floor format.");
                        return;
                    }
                } else {
                    System.err.println("Error: Invalid target floor format.");
                    return;
                }
            }
        }


        List<DoorFault> doorFaults = new ArrayList<>();

        // Assuming the door faults are the last part of the status string
        if (elevatorStatus.length > 5 && !elevatorStatus[5].equals("No Door Faults")) {
            String faultsStr = elevatorStatus[5];
            String[] faults = faultsStr.split(", ");
            for (String fault : faults) {
                String[] faultDetails = fault.split(":");
                if (faultDetails.length == 2) {
                    try {
                        String faultType = faultDetails[0];
                        int retryAttempts = Integer.parseInt(faultDetails[1]);
                        doorFaults.add(new DoorFault(faultType, retryAttempts));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Failed to parse door fault data.");
                        e.printStackTrace();
                        return;
                    }
                } else {
                    System.err.println("Error: Invalid door fault format.");
                    return;
                }
            }
        }

        ElevatorStatus newStatus = new ElevatorStatus(currentFloor, direction, elevatorId, state);
        newStatus.setTargetFloors(targetFloors);
        newStatus.setDoorFaults(doorFaults); // Set the parsed door faults
        elevatorStatusMap.put(elevatorId, newStatus);
        notifyObserver();

    }

    public void completeCommand(FloorData command) {
        commandStateMachines.remove(command);
        // Perform any additional cleanup or notification needed
    }

    public void triggerCommandEvent(FloorData command, String event) {
        SchedulerStateMachine stateMachine = commandStateMachines.get(command);
        if (stateMachine != null) {
            stateMachine.triggerEvent(event);
        } else {
        }
    }


    @Override
    public void run () {
        Thread receiveFloorThread = new Thread(receiveFromFloorAndReplyTask);
        receiveFloorThread.start();

        Thread elevatorSubsystem1TaskThread = new Thread(elevatorSubsystem1Task);
        elevatorSubsystem1TaskThread.start();

        Thread elevatorSubsystem2TaskThread = new Thread(elevatorSubsystem2Task);
        elevatorSubsystem2TaskThread.start();

        Thread elevatorSubsystem3TaskThread = new Thread(elevatorSubsystem3Task);
        elevatorSubsystem3TaskThread.start();

        Thread elevatorSubsystem4TaskThread = new Thread(elevatorSubsystem4Task);
        elevatorSubsystem4TaskThread.start();

        while (true) {
            try {

                processRequests();
                //Thread.sleep(100); // Adjust the sleep time as necessary
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.out.println("Scheduler thread was interrupted, failed to complete operation");
                //break;
            }
        }
    }
    Runnable receiveFromFloorAndReplyTask = () -> {
        try {
            byte[] receiveFloorData = new byte[200]; // Buffer size for incoming data
            DatagramPacket receiveFSPacket = new DatagramPacket(receiveFloorData, receiveFloorData.length);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Receive data from the floor subsystem
                    clock.printCurrentTime();
                    System.out.println("Scheduler: Waiting to receive from Floor Subsystem...");
                    receiveFSSocket.receive(receiveFSPacket); // Listen and receive data from floor subsystem

                    String dataStr = new String(receiveFSPacket.getData(), receiveFSPacket.getOffset(), receiveFSPacket.getLength());

                    if (dataStr.contains("FAULT")){
                        clock.printCurrentTime();
                        System.out.println("Scheduler: RECEIVED FUALT " + dataStr);
                        String[] faultParts = dataStr.split(" ");
                        int elevatorId = Integer.parseInt(faultParts[2]);
                        String faultType = faultParts[3]; // Extracting fault type

                        // Prepare the fault message for the elevator
                        String faultMessage = dataStr;
                        byte[] faultDataBytes = faultMessage.getBytes();

                        // Send the fault message to the specified elevator
                        DatagramPacket faultPacket = new DatagramPacket(faultDataBytes, faultDataBytes.length, InetAddress.getLocalHost(), elevatorId + 1); // Assuming port is based on elevatorId
                        clock.printCurrentTime();
                        TestFault = "Sending FAULT to Elevator " + elevatorId + ": " + faultMessage;
                        System.out.println("Sending FAULT to Elevator " + elevatorId + ": " + faultMessage);
                        sendESSocket.send(faultPacket);
                    }
                    else {
                        // Once a packet is received, parse it
                        FloorData floorData = StringUtil.parseInput(dataStr);

                        SchedulerStateMachine stateMachine = new SchedulerStateMachine(floorData);
                        commandStateMachines.put(floorData, stateMachine);
                        sharedData.addMessage(floorData); // Add the parsed data to shared data structure

                        triggerCommandEvent(floorData, "commandReceived");
                        clock.printCurrentTime();
                        System.out.println("Received from Floor: " + floorData);
                        //process(floorData); // Process the received floor data as needed

                        // Retrieve command from shared data structure
                        command = sharedData.getMessage();
                        int elevatorId = chooseElevator(command);

                        // Here, we set the target floor pair for the chosen elevator
                        ElevatorStatus chosenElevatorStatus = elevatorStatusMap.get(elevatorId);
//                        if (chosenElevatorStatus != null) {
//                            chosenElevatorStatus.addTargetFloorPair(floorData.getArrivalFloor(), floorData.getDestinationFloor());
//                            elevatorStatusMap.put(elevatorId, chosenElevatorStatus); // Update the map with the modified ElevatorStatus
//                        }
                        System.out.println("Sending COMMAND to Elevator " + elevatorId + ": " + command);

                        //checkElevatorAndTransitionState(chosenElevatorStatus, floorData);


                        byte[] replyToFloorData = FloorData.stringByte(command).getBytes();
                        sendESPacket = new DatagramPacket(replyToFloorData, replyToFloorData.length, InetAddress.getLocalHost(), elevatorId + 1);
                        printAllElevatorStatuses();
                        clock.printCurrentTime();
                        System.out.println("Replying to Elevator Subsystem.....");
                        rpc_reply(sendESPacket, sendESSocket, "Elevator " + elevatorId);

                        // Prepare and send acknowledgment back to the floor subsystem
                        byte[] replyData = "200 OK".getBytes(); // Acknowledgment message
                        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, receiveFSPacket.getAddress(), receiveFSPacket.getPort());
                        replyFSSocket.send(replyPacket); // Send acknowledgment
                        System.out.println("Replied to Floor Subsystem with 200 OK.");
                    }
                } catch (SocketTimeoutException timeoutException) {
                    // Retry the operation if a timeout occurs
                    System.out.println("FS Socket timeout occurred. Retrying...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // Interrupt the thread on I/O errors
        }
    };

    private void checkElevatorAndTransitionState(ElevatorStatus chosenElevator, FloorData floorData) {

        if (chosenElevator.getCurrentFloor() == floorData.getArrivalFloor()) {
            // The chosen elevator has reached the arrival floor.
            // Now, transition the associated state machine to WaitingForDestinationSensor.
            SchedulerStateMachine stateMachine = commandStateMachines.get(floorData);
            if (stateMachine != null) {
                triggerCommandEvent(floorData, "sensorArrival");
            }
        }

        if (chosenElevator.getCurrentFloor() == floorData.getDestinationFloor()) {
            // The chosen elevator has reached the destination floor.
            // Now, transition the associated state machine to CommandCompleteState.
            SchedulerStateMachine stateMachine = commandStateMachines.get(floorData);
            if (stateMachine != null) {
                triggerCommandEvent(floorData, "sensorDestination");
                triggerCommandEvent(floorData, "reset");
                commandStateMachines.remove(floorData);
            }
        }
    }


    // Elevator Subsystem 1 Task
    Runnable elevatorSubsystem1Task = () -> {
        try {
            byte[] receiveData = new byte[200];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (!Thread.currentThread().isInterrupted()) {
                receivePacket.setLength(receiveData.length);
                receiveElevatorStatusSocket1.receive(receivePacket);
                String elevatorRequest = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                addElevatorStatus(elevatorRequest);
                clock.getInstance().printCurrentTime();
                TestInformationOfElevator = "Received from Elevator Subsystem 10: " + elevatorRequest;
                System.out.println("Received from Elevator Subsystem 10: " + elevatorRequest);
                printAllElevatorStatuses();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    // Elevator Subsystem 2 Task
    Runnable elevatorSubsystem2Task = () -> {
        try {
            byte[] receiveData = new byte[200];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (!Thread.currentThread().isInterrupted()) {
                receivePacket.setLength(receiveData.length);
                receiveElevatorStatusSocket2.receive(receivePacket);
                String elevatorRequest = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                addElevatorStatus(elevatorRequest);
                clock.getInstance().printCurrentTime();
                System.out.println("Received from Elevator Subsystem 12: " + elevatorRequest);
                printAllElevatorStatuses();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    // Elevator Subsystem 3 Task
    Runnable elevatorSubsystem3Task = () -> {
        try {
            byte[] receiveData = new byte[200];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (!Thread.currentThread().isInterrupted()) {
                receivePacket.setLength(receiveData.length);
                receiveElevatorStatusSocket3.receive(receivePacket);
                String elevatorRequest = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                addElevatorStatus(elevatorRequest);
                clock.getInstance().printCurrentTime();
                System.out.println("Received from Elevator Subsystem 14: " + elevatorRequest);
                printAllElevatorStatuses();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    // Elevator Subsystem 4 Task
    Runnable elevatorSubsystem4Task = () -> {
        try {
            byte[] receiveData = new byte[200];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (!Thread.currentThread().isInterrupted()) {
                receivePacket.setLength(receiveData.length);
                receiveElevatorStatusSocket4.receive(receivePacket);
                String elevatorRequest = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                addElevatorStatus(elevatorRequest);
                clock.getInstance().printCurrentTime();
                System.out.println("Received from Elevator Subsystem 16: " + elevatorRequest);
                printAllElevatorStatuses();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };




    private void processRequests() throws Exception, InterruptedException {

    }


    public void process(FloorData command) throws RemoteException {
        String currentStateName = commandStateMachines.get(command).getCurrentState();
        // When in idle state and queue is not empty, change state to selected Command
        if ("Idle".equals(currentStateName) && sharedData.getSize() != 0) {
            triggerCommandEvent(command, "commandReceived");
        }
        // When in command selected state, selected a command from queue
        else if ("CommandSelected".equals(currentStateName)) {
            // FloorData command = synchronizer.getNextFloorCommand();
            // Send the command to the elevator
            try {
                dispatchToElevator(command);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            triggerCommandEvent(command, "commandSent");
            // Pick up the passenger from the arrival floor

            triggerCommandEvent(command, "sensorArrival");
            // Wait until the destination sensor is triggered to changed state to command complete
            synchronized ("synchronizer") {
//                while (command.getDestinationFloor() == ) {
//                    try {
//                        command.wait();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
                commandStateMachines.get(command).triggerEvent("sensorDestination");
                //command.setDestinationSensor(false);
//                command.notifyAll();
            }
            // wait for another command from the synchronizer queue
            commandStateMachines.get(command).triggerEvent("reset");
        }
        try {
            Thread.sleep(100); // Adjust the sleep time as necessary
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, failed to complete operation");
        }
    }


    public String rpc_Receive(DatagramPacket packet, DatagramSocket socket, String receiver) {
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException ste){
            System.out.println("Time out and Send again.....");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Received Packet From " + receiver + ": " + StringUtil.getStringFormat(packet.getData(), packet.getLength()));

        return StringUtil.getStringFormat(packet.getData(), packet.getLength());

    }


    public void rpc_reply(DatagramPacket packet, DatagramSocket socket, String receiver){
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Packet Sent To " + receiver + ": " + StringUtil.getStringFormat(packet.getData(), packet.getLength()));
    }


    // Send the Command Request to the Elevator
    private void dispatchToElevator (FloorData command) throws InterruptedException {
        System.out.println("---------- SCHEDULER SUBSYSTEM: Dispatching Floor Request to Elevator: " + command + " ----------\n");
    }

    private int chooseElevator(FloorData command) throws RemoteException {
        List<ElevatorStatus> applicableElevators = new ArrayList<>();
        List<ElevatorStatus> allElevators = new ArrayList<>();

        // Collect elevators that meet the criteria
        for (ElevatorStatus status : elevatorStatusMap.values()) {
            boolean isElevatorMovingCorrectly = ((status.getDirection() == command.getDirection() && status.getDirection() == Direction.DOWN)  || status.getDirection() == Direction.STATIONARY) && status.getCurrentFloor() >= command.getArrivalFloor();
            boolean isElevatorMovingCorrectlyUp = ((status.getDirection() == command.getDirection() && status.getDirection() == Direction.UP) || status.getDirection() == Direction.STATIONARY) && status.getCurrentFloor() <= command.getArrivalFloor();

            if (isElevatorMovingCorrectly || isElevatorMovingCorrectlyUp) {
                applicableElevators.add(status);
            }
            allElevators.add(status);
        }

        // Sort the list of applicable elevators based on some criteria, for example, proximity to the command's arrival floor
        applicableElevators.sort(new Comparator<ElevatorStatus>() {
            @Override
            public int compare(ElevatorStatus o1, ElevatorStatus o2) {
                // This example sorts based on proximity to the arrival floor
                // Adjust according to your specific criteria
                return Integer.compare(Math.abs(o1.getCurrentFloor() - command.getArrivalFloor()),
                        Math.abs(o2.getCurrentFloor() - command.getArrivalFloor()));
            }
        });

        //matchingElevators.sort(Comparator.comparingInt(e -> Math.abs(e.getCurrentFloor() - command.getArrivalFloor())));

        //sorts allElevators which is chosen if there are no elevators headed in the same direction
        allElevators.sort(new Comparator<ElevatorStatus>() {
            @Override
            public int compare(ElevatorStatus o1, ElevatorStatus o2) {
                // This example sorts based on proximity to the arrival floor
                // Adjust according to your specific criteria
                return Integer.compare(Math.abs(o1.getCurrentFloor() - command.getArrivalFloor()),
                        Math.abs(o2.getCurrentFloor() - command.getArrivalFloor()));
            }
        });

        if (!applicableElevators.isEmpty()) {
            // Return the id of the first elevator in the sorted list
            return applicableElevators.getFirst().getId();
        }

        // Handle the case where no elevators are applicable
        // This might involve choosing any available elevator or throwing an exception
        for(ElevatorStatus es : allElevators){
            if (es.getDirection() == command.getDirection() || es.getDirection() == Direction.STATIONARY){
                return es.getId();
            }
        }
        return allElevators.getFirst().getId();
        //throw new IllegalStateException("No applicable elevators found for the command.");
    }


    private synchronized void printAllElevatorStatuses() {
        // Update headers to include "Door Faults"
        String[] headers = {"Elevator", "Level", "Direction", "State", "Target Floors", "Door Faults"};
        // Adjust the maximum width for each column to accommodate the new "Door Faults" column
        int[] columnWidths = {10, 10, 10, 15, 20, 15}; // Adjust the last column width as needed for door faults
        clock.printCurrentTime();

        // Print header with separators
        for (int i = 0; i < headers.length; i++) {
            String headerFormat = "| %-" + columnWidths[i] + "s ";
            System.out.printf(headerFormat, headers[i]);
        }
        System.out.println("|");
        clock.printCurrentTime();

        // Print a separator line
        for (int width : columnWidths) {
            System.out.print("+");
            for (int j = 0; j < width + 1; j++) {
                System.out.print("-");
            }
        }


        System.out.println("+");
        clock.printCurrentTime();

        // Sort and print the rows of elevator status information including target floors and door faults
        elevatorStatusMap.values().stream()
                .sorted(Comparator.comparingInt(ElevatorStatus::getId))
                .forEach(status -> {

                    System.out.printf("| %-" + (columnWidths[0] - 1) + "d ", status.getId()); // Elevator ID
                    System.out.printf("| %-" + (columnWidths[1] - 1) + "d ", status.getCurrentFloor()); // Elevator Level
                    System.out.printf("| %-" + (columnWidths[2] - 1) + "s ", status.getDirection().toString()); // Elevator Direction
                    System.out.printf("| %-" + (columnWidths[3] - 1) + "s ", status.getState()); // Elevator State

                    // Target Floors formatting
                    String targetFloorsStr = status.getTargetFloors().isEmpty() ? "None" :
                            status.getTargetFloors().stream()
                                    .map(pair -> "(" + pair.getKey() + "->" + pair.getValue() + ")")
                                    .collect(Collectors.joining(","));


                    System.out.printf("| %-" + (columnWidths[4] - 1) + "s ", targetFloorsStr);

                    // Door Faults formatting
                    String doorFaultsStr = status.getDoorFaults().isEmpty() ? "None" :
                            status.getDoorFaults().stream()
                                    .map(fault -> fault.getFaultType() + ":" + fault.getRetryAttempts())
                                    .collect(Collectors.joining(", "));

                    System.out.printf("| %-" + (columnWidths[5] - 1) + "s ", doorFaultsStr); // Door Faults

                    System.out.println("|");
                    clock.printCurrentTime();

                });

        // Print bottom table border
        for (int width : columnWidths) {
            System.out.print("+");
            for (int j = 0; j < width + 1; j++) {
                System.out.print("-");
            }
        }
        System.out.println("+");
    }

    public static void main(String[] args) throws Exception{

        SharedDataImpl sharedData = new SharedDataImpl();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("SharedData", sharedData);

        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem((sharedData));
        Thread thread = new Thread(schedulerSubsystem);
        thread.start();
    }
}
