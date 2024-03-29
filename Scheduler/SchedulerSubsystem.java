package SYSC3303Project.Scheduler;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
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
import java.util.stream.Collectors;

/**
 * SchedulerSubsystem.java
 * This class represents the Scheduler subsystem, which receives commands from the Floor Subsystem, and sends commands to the Elevator Subsystem.
 * The Scheduler implements a state machine to keep track of its functions and state.
 */
public class SchedulerSubsystem implements Runnable {
    private Synchronizer synchronizer;
    private SchedulerStateMachine stateMachine;
    public SchedulerStateMachine getStateMachine() {return stateMachine;}

    private Map<Integer, ElevatorStatus> elevatorStatusMap = new HashMap<>();

    DatagramPacket receiveFSPacket, replyFSPacket, receiveESPacket, sendESPacket, receiveES1StatusPacket, receiveES2StatusPacket, receiveES3StatusPacket, receiveES4StatusPacket;
    DatagramSocket receiveFSSocket, replyFSSocket, receiveElevatorStatusSocket, sendESSocket, receiveESResponseSocket, receiveElevatorStatusSocket1, receiveElevatorStatusSocket2, receiveElevatorStatusSocket3, receiveElevatorStatusSocket4;
    SharedDataInterface sharedData;
    FloorData command;
    SimulatedClockSingleton clock;

    public SchedulerSubsystem(SharedDataInterface sharedData) throws InterruptedException {
        this.clock = SimulatedClockSingleton.getInstance();
        clock.getInstance().printCurrentTime();
        this.sharedData = sharedData;
        this.stateMachine = new SchedulerStateMachine(); // Init the state machine
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

        ElevatorStatus newStatus = new ElevatorStatus(currentFloor, direction, elevatorId, state);
        newStatus.setTargetFloors(targetFloors);
        elevatorStatusMap.put(elevatorId, newStatus);
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
                    System.out.println("Scheduler: Waiting to receive from Floor Subsystem...");
                    receiveFSSocket.receive(receiveFSPacket); // Listen and receive data from floor subsystem

                    // Once a packet is received, parse it
                    FloorData floorData = StringUtil.parseInput(new String(receiveFSPacket.getData(), receiveFSPacket.getOffset(), receiveFSPacket.getLength()));
                    sharedData.addMessage(floorData); // Add the parsed data to shared data structure

                    System.out.println("Received from Floor: " + floorData);
                    process(floorData); // Process the received floor data as needed

                    // Retrieve command from shared data structure
                    command = sharedData.getMessage();
                    int elevatorId = chooseElevator(command);

                    // Here, we set the target floor pair for the chosen elevator
                    ElevatorStatus chosenElevatorStatus = elevatorStatusMap.get(elevatorId);
                    if (chosenElevatorStatus != null) {
                        chosenElevatorStatus.addTargetFloorPair(floorData.getArrivalFloor(), floorData.getDestinationFloor());
                        elevatorStatusMap.put(elevatorId, chosenElevatorStatus); // Update the map with the modified ElevatorStatus
                    }

                    byte[] replyToFloorData = FloorData.stringByte(command).getBytes();
                    sendESPacket = new DatagramPacket(replyToFloorData, replyToFloorData.length, InetAddress.getLocalHost(), elevatorId + 1);
                    printAllElevatorStatuses();
                    System.out.println("Replying to Elevator Subsystem.....");
                    rpc_reply(sendESPacket, sendESSocket, "Elevator " + elevatorId);

                    // Prepare and send acknowledgment back to the floor subsystem
                    byte[] replyData = "200 OK".getBytes(); // Acknowledgment message
                    DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, receiveFSPacket.getAddress(), receiveFSPacket.getPort());
                    replyFSSocket.send(replyPacket); // Send acknowledgment
                    System.out.println("Replied to Floor Subsystem with 200 OK.");
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



//        /////////////////////RPC Recieve from Floor/////////////////////
//        byte[] ReceiveFloorData = new byte[20];
//        receiveFSPacket = new DatagramPacket(ReceiveFloorData, ReceiveFloorData.length);
//        System.out.println("Waiting to receive from Floor Subsystem.....");
//        int attempt = 0;
//        boolean receivedResponse = false;
//        while(attempt < 1 && !receivedResponse) {
//            try {
//                FloorData floorData = StringUtil.parseInput(rpc_Receive(receiveFSPacket, receiveFSSocket, "Floor"));
//                receivedResponse = true;
//                sharedData.addMessage(floorData);
//            } catch (RuntimeException e) {
//                // Handle timeout exception
//                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
//                attempt++;
//            }
//        }
//
//        ///////////////////// RPC Reply to Floor /////////////////////
//        byte[] ReplyData = "200".getBytes();
//        replyFSPacket = new DatagramPacket(ReplyData, ReplyData.length, InetAddress.getLocalHost(),1);
//        System.out.println("Replying to Floor Subsystem.....");
//        rpc_reply(replyFSPacket, replyFSSocket, "Floor");


//        ///////////////////// RPC Receive from Elevator Subsystem /////////////////////
//        byte[] ReceiveElevatorRequest = new byte[20];
//        receiveES1StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 10);
//        receiveES2StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 12);
//        receiveES3StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 14);
//        receiveES4StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 16);
//
//        System.out.println("Waiting to receive from Elevator Subsystem.....");
//        int attempt2 = 0;
//        boolean receivedResponse2 = false;
//        String elevatorRequest1 = null;
//        String elevatorRequest2 = null;
//        String elevatorRequest3 = null;
//        String elevatorRequest4 = null;
//        while(attempt2 < 2 && !receivedResponse2) {
//            try {
//                elevatorRequest1 = rpc_Receive(receiveES1StatusPacket, receiveElevatorStatusSocket1, "Elevator 10");
//                elevatorRequest2 = rpc_Receive(receiveES2StatusPacket, receiveElevatorStatusSocket2, "Elevator 12");
//                elevatorRequest3 = rpc_Receive(receiveES3StatusPacket, receiveElevatorStatusSocket3, "Elevator 14");
//                elevatorRequest4 = rpc_Receive(receiveES4StatusPacket, receiveElevatorStatusSocket4, "Elevator 16");
//
//                addElevatorStatus(elevatorRequest1);
//                addElevatorStatus(elevatorRequest2);
//                addElevatorStatus(elevatorRequest3);
//                addElevatorStatus(elevatorRequest4);
//                receivedResponse2 = true;
//            } catch (RuntimeException e) {
//                // Handle timeout exception
//                attempt2++;
//            }
//        }
//
//        ///////////////////// RPC Reply to Elevator /////////////////////
//        command = sharedData.getMessage();
//        int elevatorId = chooseElevator(command);
//        byte[] ReplyToFloorData = FloorData.stringByte(command).getBytes();
//        sendESPacket = new DatagramPacket(ReplyToFloorData, ReplyToFloorData.length, InetAddress.getLocalHost(), elevatorId+1);
//        printAllElevatorStatuses();
//        System.out.println("Replying to Elevator Subsystem.....");
//        rpc_reply(sendESPacket, sendESSocket, "Elevator " + elevatorId);


//        ///////////////////// RPC Receive Reply from Elevator /////////////////////
//        byte[] ReceiveElevatorReply = new byte[3];
//        receiveESPacket = new DatagramPacket(ReceiveElevatorReply, ReceiveElevatorReply.length);
//        System.out.println("Waiting to receive reply from Elevator " + elevatorId + ".....");
//        int attempt3 = 0;
//        boolean receivedReply = false;
//        while(attempt3 < 3 && !receivedReply) {
//            try {
//                String elevatorReply = rpc_Receive(receiveESPacket, receiveESResponseSocket, "Elevator " + elevatorId);
//                receivedReply = true;
//            } catch (RuntimeException e) {
//                // Handle timeout exception
//                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
//                attempt3++;
//            }
//        }
//        process(command);
//        attempt2 = 0;
//        receivedResponse2 = false;
//        while(attempt2 < 2 && !receivedResponse2) {
//            try {
//                elevatorRequest1 = rpc_Receive(receiveES1StatusPacket, receiveElevatorStatusSocket1, "Elevator 10");
//                elevatorRequest2 = rpc_Receive(receiveES2StatusPacket, receiveElevatorStatusSocket2, "Elevator 12");
//                elevatorRequest3 = rpc_Receive(receiveES3StatusPacket, receiveElevatorStatusSocket3, "Elevator 14");
//                elevatorRequest4 = rpc_Receive(receiveES4StatusPacket, receiveElevatorStatusSocket4, "Elevator 16");
//                addElevatorStatus(elevatorRequest1);
//                addElevatorStatus(elevatorRequest2);
//                addElevatorStatus(elevatorRequest3);
//                addElevatorStatus(elevatorRequest4);
//                receivedResponse2 = true;
//            } catch (RuntimeException e) {
//                // Handle timeout exception
//                attempt2++;
//            }
//        }
    }


    public void process(FloorData command) throws RemoteException {
        String currentStateName = stateMachine.getCurrentState();
        // When in idle state and queue is not empty, change state to selected Command
        if ("Idle".equals(currentStateName) && sharedData.getSize() != 0) {
            stateMachine.triggerEvent("queueNotEmpty");
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

            stateMachine.triggerEvent("commandSent");
            // Pick up the passenger from the arrival floor
            stateMachine.triggerEvent("sensorArrival");
            // Wait until the destination sensor is triggered to changed state to command complete
            synchronized ("synchronizer") {
//                while (command.getDestinationFloor() == ) {
//                    try {
//                        command.wait();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
                stateMachine.triggerEvent("sensorDestination");
                //command.setDestinationSensor(false);
//                command.notifyAll();
            }
            // wait for another command from the synchronizer queue
            stateMachine.triggerEvent("reset");
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
        //synchronizer.addSchedulerCommand(command);
    }

    private int chooseElevator(FloorData command) throws RemoteException {
        List<ElevatorStatus> applicableElevators = new ArrayList<>();

        // Collect elevators that meet the criteria
        for (ElevatorStatus status : elevatorStatusMap.values()) {
            boolean isElevatorMovingCorrectly = (status.getDirection() == Direction.DOWN || status.getDirection() == Direction.STATIONARY) && status.getCurrentFloor() >= command.getArrivalFloor() && command.getArrivalFloor() >= command.getDestinationFloor();
            boolean isElevatorMovingCorrectlyUp = (status.getDirection() == Direction.UP || status.getDirection() == Direction.STATIONARY) && status.getCurrentFloor() <= command.getArrivalFloor() && command.getArrivalFloor() <= command.getDestinationFloor();

            if (isElevatorMovingCorrectly || isElevatorMovingCorrectlyUp) {
                applicableElevators.add(status);
            }
        }

        // Sort the list of applicable elevators based on some criteria, for example, proximity to the command's arrival floor
        Collections.sort(applicableElevators, new Comparator<ElevatorStatus>() {
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
            return applicableElevators.get(0).getId();
        }

        // Handle the case where no elevators are applicable
        // This might involve choosing any available elevator or throwing an exception
        return 10;
        //throw new IllegalStateException("No applicable elevators found for the command.");
    }


    private ArrayList<ElevatorStatus> sorter(ArrayList<ElevatorStatus> elevatorStatus, FloorData command){
        ArrayList<ElevatorStatus> tempArray = elevatorStatus;

        Collections.sort(tempArray, new Comparator<ElevatorStatus>() {
            @Override
            public int compare(ElevatorStatus es1, ElevatorStatus es2) {

                return Integer.compare(Math.abs(es1.getCurrentFloor() - command.getArrivalFloor()), Math.abs(es2.getCurrentFloor() - command.getArrivalFloor()));
            }
        });

        return tempArray;
    }

    private synchronized void printAllElevatorStatuses() {
        // Update headers to include "Target Floors"
        String[] headers = {"Elevator", "Level", "Direction", "State", "Target Floors"};
        // Adjust the maximum width for each column to accommodate the new "Target Floors" column
        int[] columnWidths = {10, 10, 10, 15, 20}; // Adjust the last column width as needed

        // Print header with separators
        for (int i = 0; i < headers.length; i++) {
            String headerFormat = "| %-" + columnWidths[i] + "s ";
            System.out.printf(headerFormat, headers[i]);
        }
        System.out.println("|");

        // Print a separator line
        for (int width : columnWidths) {
            System.out.print("+");
            for (int j = 0; j < width + 1; j++) { // Add one for the space after each column
                System.out.print("-");
            }
        }
        System.out.println("+");

        // Sort and print the rows of elevator status information including target floors
        elevatorStatusMap.values().stream()
                .sorted(Comparator.comparingInt(ElevatorStatus::getId))
                .forEach(status -> {
                    System.out.printf("| %-" + (columnWidths[0] - 1) + "d ", status.getId()); // Elevator ID
                    System.out.printf("| %-" + (columnWidths[1] - 1) + "d ", status.getCurrentFloor()); // Elevator Level
                    System.out.printf("| %-" + (columnWidths[2] - 1) + "s ", status.getDirection().toString()); // Elevator Direction
                    System.out.printf("| %-" + (columnWidths[3] - 1) + "s ", status.getState()); // Elevator State

                    // Convert target floors list to a string with pairs wrapped in parentheses and separated by ',+'
                    String targetFloorsStr = status.getTargetFloors().isEmpty() ? "None" :
                            status.getTargetFloors().stream()
                                    .map(pair -> "(" + pair.getKey() + "->" + pair.getValue() + ")")
                                    .collect(Collectors.joining(","));
                    System.out.printf("| %-" + (columnWidths[4] - 1) + "s ", targetFloorsStr); // Target Floors
                    System.out.println("|");
                });

        // Print bottom table border
        for (int width : columnWidths) {
            System.out.print("+");
            for (int j = 0; j < width + 1; j++) { // Add one for the space after each column
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
