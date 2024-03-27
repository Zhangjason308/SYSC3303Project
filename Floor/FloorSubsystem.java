package SYSC3303Project.Floor;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.SharedDataInterface;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.util.Arrays;

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

    public FloorSubsystem(SharedDataInterface sharedData, String fileName) throws SocketException {
        this.sharedData = sharedData;
        this.fileName = fileName;
        try {
            this.sendAndReceiveSocket = new DatagramSocket(1);
            System.out.println("FloorSubsystem socket is bound to port: " + sendAndReceiveSocket.getLocalPort());
            sendAndReceiveSocket.setSoTimeout(10000);
        }
        catch (SocketException se){
            se.printStackTrace();
        }
    }


    public void run() {
        String filepath = fileName;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filepath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;

        try {

            while (((line = bufferedReader.readLine()) != null)) {
                Thread.sleep(5000); // next line time - current line time
                System.out.println("---------- FLOOR SUBSYSTEM: SENT REQUEST: " + line + " ----------\n");

                //int attempt = 0;
                boolean receivedResponse = false;
                boolean sentRequest = false;

                while (!receivedResponse) { // Retry up to 3 times
                    //System.out.println(Thread.currentThread().getName() + ": Attempt " + (attempt + 1));
                    // Attempt to send the FloorData packet to the Scheduler
                    if (!sentRequest) {
                        rpcSend(line, sendAndReceiveSocket, InetAddress.getLocalHost(),3);
                        sentRequest = true;
                    }
                    // Attempt to receive the reply from Scheduler
                    try {
                        rpcReceive(sendAndReceiveSocket, receivePacket, 3);
                        receivedResponse = true;
                    }  catch (SocketTimeoutException ste) {
                        // Handle timeout exception
                        System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
                        //attempt++;
                    }
                }

                if (!receivedResponse) {
                    System.out.println(Thread.currentThread().getName() + ": No response after multiple attempts. Exiting.");
                    return;
                }
            }
        } catch (IOException | InterruptedException ie) {
            throw new RuntimeException(ie);
        }
        //call scheduler and give it the line
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
    }

    private void rpcReceive(DatagramSocket socket, DatagramPacket packet, int byteArrSize) throws SocketTimeoutException {
        byte[] data = new byte[byteArrSize];
        packet = new DatagramPacket(data, data.length);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
            throw new SocketTimeoutException();
        }
        System.out.println("Packet Received From Scheduler: " + StringUtil.getStringFormat(packet));
    }
    private void rpcSend (String inputLine, DatagramSocket socket, InetAddress address, int port) {
        try {
            byte[] floorDataBytes = inputLine.getBytes(StandardCharsets.UTF_8);

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

