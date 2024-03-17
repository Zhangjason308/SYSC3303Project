package SYSC3303Project.Elevator;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.StringUtil;
import SYSC3303Project.SharedDataInterface;
import SYSC3303Project.Synchronizer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.RemoteException;


/**
 * ElevatorSubsystem.java
 * This class represents the Elevator subsystem, which executes commands, based on information it receives from the Scheduler.
 * The Elevator implements a state machine to keep track of its functions and state.
 */

public class ElevatorSubsystem implements Runnable {
    private int currentFloor = 1; // Starting floor

    private Direction directionEnum;
    private ElevatorStateMachine elevatorStateMachine;

    DatagramPacket sendPacket, receivePacket;

    private SharedDataInterface sharedData;
    public ElevatorStateMachine getElevatorStateMachine() {return elevatorStateMachine;}

    private DatagramSocket sendSocket,receiveSocket;

    private FloorData command = null;



    public ElevatorSubsystem(SharedDataInterface sharedData) {
        this.sharedData = sharedData;
        this.elevatorStateMachine = new ElevatorStateMachine();
        this.directionEnum = Direction.STATIONARY;
        try {
            this.sendSocket = new DatagramSocket(10);
            System.out.println("ElevatorSubsystem SENDING PACKETS ON PORT: " + sendSocket.getLocalPort());
            this.receiveSocket = new DatagramSocket(11);
            System.out.println("ElevatorSubsystem RECEIVING PACKETS ON PORT: " + receiveSocket.getLocalPort());
        }
        catch (SocketException se){
            se.printStackTrace();
        }
    }

    public String getElevatorStatus() {
        return currentFloor + "," + directionEnum;
    }

    public void sendAndReceive() throws UnknownHostException {
        int attempt = 0;
        boolean receivedResponse = false;

        while (attempt < 20 && !receivedResponse) { // Retry up to 3 times
            System.out.println(Thread.currentThread().getName() + ": Attempt " + (attempt + 1));
            // Attempt to send the FloorData packet to the Scheduler
            rpcSend(getElevatorStatus(), sendSocket, InetAddress.getLocalHost(), 4);
            // Attempt to receive the reply from Scheduler
            try {
                command = rpcReceive(receiveSocket,receivePacket, 20);
                receivedResponse = true;
            }  catch (SocketTimeoutException ste) {
                // Handle timeout exception
                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
                attempt++;
            }
        }

        if (!receivedResponse) {
            System.out.println(Thread.currentThread().getName() + ": No response after multiple attempts. Exiting.");
            return;
        }


    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {

                    sendAndReceive();

                    if (command != null) {
                        System.out.println("---------- ELEVATOR SUBSYSTEM: Received Command :" + command + " ----------\n");
                        processCommand(command);
                    }


                Thread.sleep(100); // Sleep to reduce CPU usage when idle
            } catch (InterruptedException | UnknownHostException e) {
                System.out.println("---------- ELEVATOR SUBSYSTEM INTERRUPTED ---------- ");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private FloorData rpcReceive(DatagramSocket socket, DatagramPacket packet, int byteArrSize) throws SocketTimeoutException {
        byte[] data = new byte[byteArrSize];
        packet = new DatagramPacket(data, data.length);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
            throw new SocketTimeoutException();
        }
        System.out.println("Packet Received From Scheduler: " + StringUtil.getStringFormat(packet));
        return StringUtil.parseInput(StringUtil.getStringFormat(packet));
    }
    private void rpcSend(String command, DatagramSocket socket, InetAddress address, int port) {
        try {
            byte[] floorDataBytes = command.getBytes(StandardCharsets.UTF_8);
            // Create a DatagramPacket with the FloorData bytes
            DatagramPacket packet = new DatagramPacket(floorDataBytes, floorDataBytes.length, address, port);

            // Send the packet
            socket.send(packet);
            System.out.println("Packet Sent To Scheduler: " + StringUtil.getStringFormat(packet));
        } catch (IOException e) {
            System.err.println("IOException in sendFloorData: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void processCommand(FloorData command) throws InterruptedException {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Processing Command :" + command + " ----------\n");
        int destinationFloor = command.getDestinationFloor();
        int arrivalFloor = command.getArrivalFloor();

        // Elevator moves to arrival floor to pickup passengers
        if (arrivalFloor != currentFloor) {
            moveToFloor(arrivalFloor,"boarding");
        }
        // Elevator is already at arrival floor to pickup passengers
        else {
            System.out.println("---------- ELEVATOR SUBSYSTEM: Already at floor " + currentFloor + " ----------\n");
            elevatorStateMachine.triggerEvent("stop");
            System.out.println("---------- ELEVATOR SUBSYSTEM: Passengers boarding ----------\n");
            elevatorStateMachine.triggerEvent("openDoors");
            System.out.println("---------- ELEVATOR SUBSYSTEM: Doors Closed ----------\n");
            elevatorStateMachine.triggerEvent("closeDoors");
        }

        // Move to the destination floor to deliver the passenger
        moveToFloor(destinationFloor,"departing");
        // Sets the destination sensor flag to true once the elevator has arrived to the destination floor, and notifies the Scheduler
        //synchronized (synchronizer) {
        //    synchronizer.setDestinationSensor(true);
        //    synchronizer.notifyAll();

        // Command is complete, return back to idle state
        //elevatorStateMachine.triggerEvent("idle");
    }

    private void moveToFloor(int destinationFloor, String action) {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Moving from floor " + currentFloor + " to floor " + destinationFloor + " ----------\n");
        // Move up is destination floor is higher than current floor
        if (currentFloor < destinationFloor) {
            goUp(destinationFloor);
        }
        // Move down is destination floor is lower than current floor
        else if (currentFloor > destinationFloor) {
            goDown(destinationFloor);
        }
        System.out.println("---------- ELEVATOR SUBSYSTEM: Stopping at floor " + currentFloor + " ----------\n");
        elevatorStateMachine.triggerEvent("stop");
        System.out.println("---------- ELEVATOR SUBSYSTEM: Passenger " + action + " ----------\n");
        elevatorStateMachine.triggerEvent("openDoors");
        System.out.println("---------- ELEVATOR SUBSYSTEM: Doors Closed ----------\n");
        elevatorStateMachine.triggerEvent("closeDoors");

    }

    private void goUp(int destinationFloor) {
        elevatorStateMachine.triggerEvent("moveUp");
        currentFloor = destinationFloor;
    }

    private void goDown(int destinationFloor) {
        elevatorStateMachine.triggerEvent("moveDown");
        currentFloor = destinationFloor;
    }

    private FloorData getFloorData(InetAddress address, int port) {
        byte[] receiveData = new byte[65535];

        FloorData command1 = null;
        try { // Receive packet from client
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("SYSC3303.ElevatorSubsystem: Waiting for Instruction from Scheduler...");
            receiveSocket.receive(receivePacket);
            //printPacketDetails("Received", receivePacket, receiveSocket.getLocalPort());
            // Parse the packet
            String receivedString = new String(receivePacket.getData(), StandardCharsets.UTF_8);
            String[] parts = receivedString.split(",");
            String time = parts[0];
            int arrivalFloor = Integer.parseInt(parts[1]);
            Direction direction = Direction.valueOf(parts[2]);
            int destinationFloor = Integer.parseInt(parts[3].trim());
            command1 = new FloorData(time, arrivalFloor, direction, destinationFloor);

            System.out.println(command1.toString());


        } catch (IOException e) {
            System.err.println("IOException in sendFloorData: " + e.getMessage());
            e.printStackTrace();
        }


        return command1;
    }
    public static void main(String args[]) {
        try {
            // Retrieve the shared data interface from the RMI registry
            SharedDataInterface sharedData = (SharedDataInterface) Naming.lookup("rmi://localhost/SharedData");
            // Create Elevator subsystem
            ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(sharedData);
            // Create Elevator thread
            Thread elevatorThread = new Thread(elevatorSubsystem, "Elevator");
            // Start the elevator thread
            elevatorThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}