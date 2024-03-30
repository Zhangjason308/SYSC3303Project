package SYSC3303Project.Floor;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.SharedDataInterface;
import SYSC3303Project.SimulatedClockSingleton;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * FloorSubsystem.java
 * This class is a floor subsystem for an elevator real-time system. This subsystem
 * is a client thread that reads events/commands from the server, in which then it can perform
 * the Elevator movement.
 */
public class FloorSubsystem implements Runnable {

    DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendAndReceiveSocket;
    private String fileName;
    // Shared Interface to store FloorData Commands
    private SharedDataInterface sharedData;
    SimulatedClockSingleton clock;

    public FloorSubsystem(SharedDataInterface sharedData, String fileName) throws SocketException {
        this.clock = SimulatedClockSingleton.getInstance();
        clock.printCurrentTime();
        this.sharedData = sharedData;
        this.fileName = fileName;
        try {
            this.sendAndReceiveSocket = new DatagramSocket(1);

            System.out.println("FloorSubsystem socket is bound to port: " + sendAndReceiveSocket.getLocalPort());
            sendAndReceiveSocket.setSoTimeout(10000);
        } catch (SocketException se) {
            se.printStackTrace();
        }
    }

    public void run() {
        Thread senderThread = new Thread(this::sendFloorRequests);
        Thread receiverThread = new Thread(this::receiveAcks);

        senderThread.start();
        receiverThread.start();

        try {
            senderThread.join();
            receiverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendFloorRequests() {
        String filepath = fileName;
        long startTime = System.currentTimeMillis(); // Capture the start time of the program

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                // Parse the delay (offset) from the line
                String[] parts = line.split(" ");
                long offsetInMillis = parseTimeToMillis(parts[0]); // Convert the HH:mm:ss.S format to milliseconds

                // Calculate the absolute time when the request should be sent
                long targetTimeInMillis = startTime + offsetInMillis;

                // Calculate the wait time from now until the target time
                long currentTimeInMillis = System.currentTimeMillis();
                long waitTimeInMillis = targetTimeInMillis - currentTimeInMillis;

                if (waitTimeInMillis > 0) {
                    // Wait for the difference if the target time is in the future
                    Thread.sleep(waitTimeInMillis);
                }

                // Send the floor request after waiting
                rpcSend(line, sendAndReceiveSocket, InetAddress.getLocalHost(), 3);
            }
        } catch (IOException | InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }




    // This utility method assumes the input is a timestamp in "HH:mm:ss.S" format
    public long parseTimeToMillis(String timeStr) {
        String[] parts = timeStr.split("[:.]");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);
        long millis = Long.parseLong(parts[3]) * 100; // Convert tenths of a second to milliseconds
        return hours * 3600000 + minutes * 60000 + seconds * 1000 + millis;
    }


    public void receiveAcks() {
        while (true) {
            try {
                rpcReceive(sendAndReceiveSocket, receivePacket, 3);
            } catch (SocketTimeoutException ste) {
                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
            }
        }
    }

//    public long parseTimeToSeconds(String timeStr) {
//        String[] hms = timeStr.split("[:.]");
//        long hoursToSeconds = Integer.parseInt(hms[0]) * 3600;
//        long minutesToSeconds = Integer.parseInt(hms[1]) * 60;
//        long seconds = Integer.parseInt(hms[2]);
//        return hoursToSeconds + minutesToSeconds + seconds;
//    }

    private void rpcReceive(DatagramSocket socket, DatagramPacket packet, int byteArrSize) throws SocketTimeoutException {
        byte[] data = new byte[byteArrSize];
        packet = new DatagramPacket(data, data.length);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
            throw new SocketTimeoutException();
        }
        clock.printCurrentTime();
        System.out.println("Packet Received From Scheduler: " + StringUtil.getStringFormat(packet));
    }
    private void rpcSend (String inputLine, DatagramSocket socket, InetAddress address, int port) {
        try {
            byte[] floorDataBytes = inputLine.getBytes(StandardCharsets.UTF_8);

            // Create a DatagramPacket with the FloorData bytes
            DatagramPacket packet = new DatagramPacket(floorDataBytes, floorDataBytes.length, address, port);

            // Send the packet
            socket.send(packet);
            clock.printCurrentTime();
            System.out.println("Packet Sent To Scheduler: " + StringUtil.getStringFormat(packet));
        } catch (IOException e) {
            System.err.println("IOException in sendFloorData: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String formatFloorRequest(String inputLine) {
        String[] parts = inputLine.split(" ");
        String time = parts[0];
        String floor = parts[1];
        String direction = parts[2];
        String destination = parts[3];
        return time + " " + floor + " " + direction + " " + destination;
    }

    public static void main(String args[]) {
        try {
            SharedDataInterface sharedData = (SharedDataInterface) Naming.lookup("rmi://localhost/SharedData");
            FloorSubsystem floorSubsystem = new FloorSubsystem(sharedData, "./ElevatorEvents.csv");
            Thread floorThread = new Thread(floorSubsystem, "Floor");
            floorThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

