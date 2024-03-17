package SYSC3303Project.Elevator;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.StateMachine.ElevatorStateMachine;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Synchronizer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;


/**
 * ElevatorSubsystem.java
 * This class represents the Elevator subsystem, which executes commands, based on information it receives from the Scheduler.
 * The Elevator implements a state machine to keep track of its functions and state.
 */

public class ElevatorSubsystem implements Runnable {
    private int currentFloor = 1; // Starting floor
    private ElevatorStateMachine elevatorStateMachine;
    public ElevatorStateMachine getElevatorStateMachine() {return elevatorStateMachine;}

    private Synchronizer synchronizer;
    private DatagramSocket sendSocket,receiveSocket;



    public ElevatorSubsystem(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
        this.elevatorStateMachine = new ElevatorStateMachine();
        try {
            this.sendSocket = new DatagramSocket(100);
            System.out.println("ElevatorSubsystem SENDING PACKETS ON PORT: " + sendSocket.getLocalPort());
            this.receiveSocket = new DatagramSocket(101);
            System.out.println("ElevatorSubsystem RECEIVING PACKETS ON PORT: " + receiveSocket.getLocalPort());


        }
        catch (SocketException se){
            se.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (synchronizer.hasSchedulerCommands()) {
                    FloorData command = synchronizer.getNextSchedulerCommand();
                    FloorData command1 = getFloorData(InetAddress.getLocalHost(),88);

                    if (command != null) {
                        System.out.println("---------- ELEVATOR SUBSYSTEM: Received Command :" + command1 + " ----------\n");
                        processCommand(command1);
                    }
                }

                Thread.sleep(100); // Sleep to reduce CPU usage when idle
            } catch (InterruptedException | UnknownHostException e) {
                System.out.println("---------- ELEVATOR SUBSYSTEM INTERRUPTED ---------- ");
                Thread.currentThread().interrupt();
                break;
            }
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
        synchronized (synchronizer) {
            synchronizer.setDestinationSensor(true);
            synchronizer.notifyAll();
        }
        // Command is complete, return back to idle state
        elevatorStateMachine.triggerEvent("idle");
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

    private void printPacketDetails(String action, DatagramPacket packet, int localPort) {
        byte[] data = packet.getData();
        int length = packet.getLength();

        System.out.println(action + " packet:");

        if ("Received".equals(action)) {
            // For received packets, indicate the packet came from a remote address and was received on the local port
            System.out.println("From: " + packet.getAddress() + ":" + packet.getPort() + " On PORT: " + localPort);
        } else if ("Sent".equals(action)) {
            // For sent packets, indicate the packet is being sent to a remote address from the local port
            System.out.println("To: " + packet.getAddress() + ":" + packet.getPort() + " From PORT: " + localPort);
        }

        System.out.println("Length: " + length);
        System.out.println("Containing (String): " + new String(data, 0, length));

        // Print byte array representation
        System.out.print("Containing (Bytes): [");
        for (int i = 0; i < length; i++) {
            System.out.print(data[i] & 0xFF); // Use & 0xFF to convert to unsigned value for better readability
            if (i < length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");

        System.out.println("-----------------------------------------------");
    }

    private FloorData getFloorData(InetAddress address, int port) {
        byte[] receiveData = new byte[65535];

        FloorData command1 = null;
        try { // Receive packet from client
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("SYSC3303.ElevatorSubsystem: Waiting for Instruction from Scheduler...");
            receiveSocket.receive(receivePacket);
            printPacketDetails("Received", receivePacket, receiveSocket.getLocalPort());
            // Parse the packet
            String receivedString = new String(receivePacket.getData(), StandardCharsets.UTF_8);
            String[] parts = receivedString.split(",");
            String time = parts[0];
            int arrivalFloor = Integer.parseInt(parts[1]);
            DirectionEnum direction = DirectionEnum.valueOf(parts[2]);
            int destinationFloor = Integer.parseInt(parts[3].trim());
            command1 = new FloorData(time, arrivalFloor, direction, destinationFloor);

            System.out.println(command1.toString());


        } catch (IOException e) {
            System.err.println("IOException in sendFloorData: " + e.getMessage());
            e.printStackTrace();
        }


        return command1;
    }
}