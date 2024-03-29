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
import java.util.concurrent.CyclicBarrier;

/**
 * ElevatorSubsystem.java
 * This class represents the Elevator subsystem, which executes commands, based on information it receives from the Scheduler.
 * The Elevator implements a state machine to keep track of its functions and state.
 */

public class ElevatorSubsystem implements Runnable {

    private CyclicBarrier cyclicBarrier;

    private int currentFloor = 1; // Starting floor

    private Direction direction;
    private ElevatorStateMachine elevatorStateMachine;

    DatagramPacket sendPacket, receivePacket;

    private SharedDataInterface sharedData;
    public ElevatorStateMachine getElevatorStateMachine() {return elevatorStateMachine;}

    private DatagramSocket sendSocket,receiveSocket;

    private FloorData command = null;

    private int id;



    public ElevatorSubsystem(SharedDataInterface sharedData, int id) {
        this.id = id;
        this.sharedData = sharedData;
        this.elevatorStateMachine = new ElevatorStateMachine();
        this.direction = Direction.STATIONARY;
        try {
            this.sendSocket = new DatagramSocket(id);
            System.out.println("ElevatorSubsystem SENDING PACKETS ON PORT: " + sendSocket.getLocalPort());
            this.receiveSocket = new DatagramSocket(id + 1);
            System.out.println("ElevatorSubsystem RECEIVING PACKETS ON PORT: " + receiveSocket.getLocalPort());
        }
        catch (SocketException se){
            se.printStackTrace();
        }
        sendStatusUpdate();
    }

    public String getElevatorStatus() {
        return  id + "," + currentFloor + "," + direction;
    }
    public void sendAndReceive() {
        // Then, focus on listening for and processing commands from the Scheduler
        while (!Thread.currentThread().isInterrupted()) {
            try {
                command = rpcReceive(receiveSocket, receivePacket, 20); // Update the packet size as needed
                System.out.println("---------- ELEVATOR SUBSYSTEM " + id + ": Received Command :" + command + " ----------\n");
                processCommand(command);
                // reply to the Scheduler indicating command processed
                rpcSend("200", sendSocket, InetAddress.getLocalHost(), 6);
            } catch (SocketTimeoutException ste) {
                // Handle timeout or no command received - Elevator could be idle or keep moving based on last command
            } catch (UnknownHostException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            sendAndReceive();
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


    public void processCommand(FloorData command) throws InterruptedException {
        System.out.println("---------- ELEVATOR SUBSYSTEM: Processing Command :" + command + " ----------\n");
        int destinationFloor = command.getDestinationFloor();
        int arrivalFloor = command.getArrivalFloor();
        direction = command.getDirection();

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
        this.currentFloor = destinationFloor;
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
        while (currentFloor < destinationFloor) {
            try {
                Thread.sleep((ElevatorTiming.getTravelTime(destinationFloor-currentFloor))/(destinationFloor-currentFloor)); // Simulate the time it takes to move up one floor
                currentFloor++;
                System.out.println("Elevator " + id + " is now at floor " + currentFloor);

                // Send status update to Scheduler
                sendStatusUpdate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }


    private void goDown(int destinationFloor) {
        elevatorStateMachine.triggerEvent("moveDown");
        while (currentFloor > destinationFloor) {
            try {
                Thread.sleep((ElevatorTiming.getTravelTime(destinationFloor-currentFloor))/(destinationFloor-currentFloor)); // Simulate the time it takes to move up one floor
                currentFloor--;
                System.out.println("Elevator " + id + " is now at floor " + currentFloor);

                // Send status update to Scheduler
                sendStatusUpdate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void sendStatusUpdate() {
        String status = getElevatorStatus();
        try {
            // Assuming the Scheduler listens for status updates on a specific port
            rpcSend(status, sendSocket, InetAddress.getLocalHost(), id+10);
        } catch (UnknownHostException e) {
            System.err.println("Unknown Host Exception: " + e.getMessage());
        }
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