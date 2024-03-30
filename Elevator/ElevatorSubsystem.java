package SYSC3303Project.Elevator;

import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.StringUtil;
import SYSC3303Project.SharedDataInterface;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;


/**
 * ElevatorSubsystem.java
 * This class represents the Elevator subsystem, which executes commands, based on information it receives from the Scheduler.
 * The Elevator implements a state machine to keep track of its functions and state.
 */

public class ElevatorSubsystem implements Runnable {

    int height = 0;
    private int currentFloor = 1; // Starting floor

    private Direction direction;
    private ElevatorStateMachine elevatorStateMachine;

    DatagramPacket sendPacket, receivePacket;

    private SharedDataInterface sharedData;
    public ElevatorStateMachine getElevatorStateMachine() {return elevatorStateMachine;}

    private DatagramSocket sendSocket,receiveSocket;

    private FloorData command = null;

    private int id;
    private List<AbstractMap.SimpleEntry<Integer, Integer>> targetFloors = new ArrayList<>();



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

        return id + "," + currentFloor + "," + direction + "," + elevatorStateMachine.getCurrentState().toString() + "," + targetFloorsStr;
    }



    public void sendAndReceive() throws UnknownHostException, InterruptedException {
        int attempt = 0;
        boolean receivedResponse = false;
        boolean sentStatus = false;

        while (attempt < 1 && !receivedResponse) {
            if (!sentStatus) {
                rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id + 10);
                sentStatus = true;
            }
            try {
                // Assuming rpcReceive is now correctly designed to return a FloorData object
                command = rpcReceive(receiveSocket, new DatagramPacket(new byte[1024], 1024), 1024);
                receivedResponse = true;

                if (command != null) {
                    // Add to targetFloors list upon receiving a valid command
                    synchronized (this) {
                        targetFloors.add(new AbstractMap.SimpleEntry<>(command.getArrivalFloor(), command.getDestinationFloor()));
                    }
                    System.out.println("---------- ELEVATOR [" + id + "]: Received Command: " + command + " ----------\n");
                    processCommand(command);
                    // Indicate command processed
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


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                sendAndReceive();
            } catch (InterruptedException | UnknownHostException e) {
                System.out.println("---------- ELEVATOR [" + id + "] SUBSYSTEM INTERRUPTED ---------- ");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private FloorData rpcReceive(DatagramSocket socket, DatagramPacket packet, int byteArrSize) throws SocketTimeoutException {
        byte[] data = new byte[byteArrSize];
        packet = new DatagramPacket(data, data.length);

        try {
            socket.setSoTimeout(5000);
            socket.receive(packet);
        } catch (IOException e) {
            throw new SocketTimeoutException();
        }
        System.out.println(Thread.currentThread().getName() + ": Packet Received From Scheduler: " + StringUtil.getStringFormat(packet.getData(), packet.getLength()));
        return StringUtil.parseInputWithComma(StringUtil.getStringFormat(packet.getData(), packet.getLength()));
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



    public void processCommand(FloorData command) throws InterruptedException, UnknownHostException {
        System.out.println("---------- ELEVATOR [" + id + "]: Processing Command :" + command + " ----------\n");
        int destinationFloor = command.getDestinationFloor();
        int arrivalFloor = command.getArrivalFloor();
        direction = command.getDirection();

        // Elevator moves to arrival floor to pickup passengers
        if (arrivalFloor != currentFloor) {
            moveToFloor(arrivalFloor,"boarding");
        }
        // Elevator is already at arrival floor to pickup passengers
        else {
            System.out.println("---------- ELEVATOR [" + id + "]: Already at floor " + currentFloor + " ----------\n");
            elevatorStateMachine.triggerEvent("stop");
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

            System.out.println("---------- ELEVATOR [" + id + "]: Passengers boarding ----------\n");
            elevatorStateMachine.triggerEvent("openDoors");
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

            System.out.println("---------- ELEVATOR [" + id + "]: Doors Closed ----------\n");
            elevatorStateMachine.triggerEvent("closeDoors");
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

        }

        // Move to the destination floor to deliver the passenger
        moveToFloor(destinationFloor,"departing");
        // Sets the destination sensor flag to true once the elevator has arrived to the destination floor, and notifies the Scheduler
        //synchronized (synchronizer) {
        //    synchronizer.setDestinationSensor(true);
        //    synchronizer.notifyAll();

        // Command is complete, return back to idle state
        //elevatorStateMachine.triggerEvent("idle");
        this.currentFloor = destinationFloor;
    }


    private void moveToFloor(int destinationFloor, String action) throws UnknownHostException, InterruptedException {
        System.out.println("---------- ELEVATOR [" + id + "]: Moving from floor " + currentFloor + " to floor " + destinationFloor + " ----------\n");
        // Move up is destination floor is higher than current floor
        if (currentFloor < destinationFloor) {
            goUp(destinationFloor, destinationFloor - currentFloor);
        }
        // Move down is destination floor is lower than current floor
        else if (currentFloor > destinationFloor) {
            goDown(destinationFloor, currentFloor - destinationFloor);
        }
        System.out.println("---------- ELEVATOR [" + id + "]: Stopping at floor " + currentFloor + " ----------\n");
        elevatorStateMachine.triggerEvent("stop");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

        System.out.println("---------- ELEVATOR [" + id + "]:" + action + " ----------\n");
        elevatorStateMachine.triggerEvent("openDoors");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

        System.out.println("---------- ELEVATOR [" + id + "]: Doors Closed ----------\n");
        elevatorStateMachine.triggerEvent("closeDoors");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);


    }

    public long getTimePerFloor(int choice) {//right now the accelerate/decelerate time for half a floor is the same as crusing for a whole floor
        final double floorHeight = 3.912; // height in meters
        final double speed = 0.2784; // meters per second
        final double acceleration = 0.01981; // meters per second square
        final long timePerFloorCruise = (long) (floorHeight / speed * 100); // t = d/t in millisecond
        final long halfAndAccelerateTime = (long) (1.4); // t = root(2d/a) in milliseconds
        final long timePerHalfFloorCruise = (long) (0.7);
        final long timePerFloor1Floor = (long) (2.8);
        if(choice == 0){//only going 1 floor so its the time to accelerate then descelerate right after
            return timePerFloor1Floor;
        }
        else if(choice == 1){//accelerating/decelerating
            return  halfAndAccelerateTime;
        }
        else if(choice == 2){//going half a floor crusing
            return timePerHalfFloorCruise;
        }
        else {//just cruising a floor
            return timePerFloorCruise;
        }

    }


    //the order is half floor acceleration, half floor deceleration, add 1 to floor, do rest of floors as cruise till last then half floor where you cruise half a floor then decelerate a floor
    private void goUp(int destinationFloor, int travelDistance) throws UnknownHostException, InterruptedException {

        //long startTime = System.currentTimeMillis();

        elevatorStateMachine.triggerEvent("accelerate");
        long timeTracker = System.currentTimeMillis();//variable not used right now but was used to store the time to compare with the current time
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        this.direction = Direction.UP;
        Thread.sleep(getTimePerFloor(1));
        timeTracker = System.currentTimeMillis();
        if(Math.abs(destinationFloor - travelDistance) == 1){//if you only go 1 floor so like 1 - 2 or 2 - 3, do this as you cruise for basically no time
            elevatorStateMachine.triggerEvent("cruise");
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
            Thread.sleep(getTimePerFloor(2));
            elevatorStateMachine.triggerEvent("decelerate");
            currentFloor++;
            stopAtFloor(targetFloors.get(0));
            System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        }
        else{//this is for everything else that isn't traveling 1 floor
            elevatorStateMachine.triggerEvent("cruise");
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
            Thread.sleep(getTimePerFloor(2));
            timeTracker = System.currentTimeMillis();
            currentFloor++;
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
            boolean travel2FloorTotal = false;
            while (currentFloor < destinationFloor) {//if you are traveling 2 floors total like 1-3 you want to skip the if statement below because you do half floor acceleration into half floor cruise into half floor cruise into half floor deceleration
                if(Math.abs(currentFloor - destinationFloor) != 1 || travel2FloorTotal){
                    Thread.sleep(getTimePerFloor(3));
                    timeTracker = System.currentTimeMillis();
                    currentFloor++;
                    travel2FloorTotal = true;
                    rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
                }

                // Check if the current floor is a target floor
                Iterator<AbstractMap.SimpleEntry<Integer, Integer>> iterator = targetFloors.iterator();
                while (iterator.hasNext()) {
                    AbstractMap.SimpleEntry<Integer, Integer> floorPair = iterator.next();
                    if (floorPair.getValue() - 1 == currentFloor) { // if the floor before arival floor is target
                        // Simulate stopping at this floor
                        Thread.sleep(getTimePerFloor(2));
                        timeTracker = System.currentTimeMillis();
                        elevatorStateMachine.triggerEvent("decelerate");
                        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
                        Thread.sleep(getTimePerFloor(1));
                        stopAtFloor(floorPair);
                        iterator.remove(); // Remove from target floors after stopping
                    }
                }

//            while (System.currentTimeMillis() - startTime < timePerFloor) {
//                if (Thread.currentThread().isInterrupted()) {
//                    return;
//                }
//            }

                System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
                rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

            }
        }

    }

    private void goDown(int destinationFloor, int travelDistance) throws UnknownHostException, InterruptedException {
        //long startTime = System.currentTimeMillis();

        elevatorStateMachine.triggerEvent("accelerate");
        long timeTracker = System.currentTimeMillis();
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        this.direction = Direction.DOWN;
        Thread.sleep(getTimePerFloor(1));
        timeTracker = System.currentTimeMillis();
        if(Math.abs(destinationFloor - travelDistance) == 1){
            elevatorStateMachine.triggerEvent("cruise");
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
            Thread.sleep(getTimePerFloor(2));
            elevatorStateMachine.triggerEvent("decelerate");
            currentFloor--;
            stopAtFloor(targetFloors.get(0));
            System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
        }
        else{
            elevatorStateMachine.triggerEvent("cruise");
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
            Thread.sleep(getTimePerFloor(2));
            timeTracker = System.currentTimeMillis();
            currentFloor--;
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
            boolean travel2FloorTotal = false;
            while (currentFloor > destinationFloor) {
                if(Math.abs(currentFloor - destinationFloor) != 1 || travel2FloorTotal){
                    Thread.sleep(getTimePerFloor(3));
                    timeTracker = System.currentTimeMillis();
                    currentFloor--;
                    travel2FloorTotal = true;
                    rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
                }

                // Check if the current floor is a target floor
                Iterator<AbstractMap.SimpleEntry<Integer, Integer>> iterator = targetFloors.iterator();
                while (iterator.hasNext()) {
                    AbstractMap.SimpleEntry<Integer, Integer> floorPair = iterator.next();
                    if (floorPair.getValue() + 1 == currentFloor) { // If arrival floor is a target
                        // Simulate stopping at this floor
                        Thread.sleep(getTimePerFloor(2));
                        timeTracker = System.currentTimeMillis();
                        elevatorStateMachine.triggerEvent("decelerate");
                        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);
                        Thread.sleep(getTimePerFloor(1));
                        stopAtFloor(floorPair);
                        iterator.remove(); // Remove from target floors after stopping
                    }
                }

//            while (System.currentTimeMillis() - startTime < timePerFloor) {
//                if (Thread.currentThread().isInterrupted()) {
//                    return;
//                }
//            }

                System.out.println("ELEVATOR [" + id + "]: Reached floor " + currentFloor);
                rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id+10);

            }
        }
    }

    private void stopAtFloor(AbstractMap.SimpleEntry<Integer, Integer> floorPair) throws UnknownHostException {
        System.out.println("ELEVATOR [" + id + "]: Stopping at floor " + floorPair.getKey());
        // Simulate actions taken at a stop, e.g., opening doors, waiting, then closing doors
        elevatorStateMachine.triggerEvent("openDoors");
        // Assume there's a brief delay to simulate doors open, passengers boarding/alighting
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        elevatorStateMachine.triggerEvent("closeDoors");
        rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), id + 10);
    }



    private FloorData getFloorData(InetAddress address, int port) {
        byte[] receiveData = new byte[65535];

        FloorData command = null;
        try {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("ELEVATOR [" + id + "]: Waiting for Instruction from Scheduler...");
            receiveSocket.receive(receivePacket);
            // Parse the packet
            String receivedString = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
            String[] parts = receivedString.split(",");
            String time = parts[0];
            int arrivalFloor = Integer.parseInt(parts[1]);
            Direction direction = Direction.valueOf(parts[2]);
            int destinationFloor = Integer.parseInt(parts[3].trim());
            command = new FloorData(time, arrivalFloor, direction, destinationFloor);

            System.out.println("ELEVATOR [" + id + "]: Received Command: " + command);

            // Add to targetFloors lis



        } catch (IOException e) {
            System.err.println("ELEVATOR [" + id + "]: IOException in getFloorData: " + e.getMessage());
            e.printStackTrace();
        }

        return command;
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