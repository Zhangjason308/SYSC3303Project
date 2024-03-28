package SYSC3303Project.Scheduler;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.StringUtil;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;
import SYSC3303Project.SharedDataImpl;
import SYSC3303Project.SharedDataInterface;
import SYSC3303Project.SimulatedClockSingleton;
import SYSC3303Project.Synchronizer;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * SchedulerSubsystem.java
 * This class represents the Scheduler subsystem, which receives commands from the Floor Subsystem, and sends commands to the Elevator Subsystem.
 * The Scheduler implements a state machine to keep track of its functions and state.
 */
public class SchedulerSubsystem implements Runnable {

    private Synchronizer synchronizer;
    private SchedulerStateMachine stateMachine;
    public SchedulerStateMachine getStateMachine() {return stateMachine;}

    private ArrayList<ElevatorStatus> elevatorStatusList;
    private FloorData sameElevator;

    private int elevatorNumber;
    DatagramPacket receiveFSPacket, replyFSPacket, receiveESPacket, sendESPacket, receiveES1StatusPacket, receiveES2StatusPacket, receiveES3StatusPacket, receiveES4StatusPacket;
    DatagramSocket receiveFSSocket, replyFSSocket, sendESSocket, receiveESResponseSocket, receiveElevatorStatusSocket1, receiveElevatorStatusSocket2, receiveElevatorStatusSocket3, receiveElevatorStatusSocket4;
    SharedDataInterface sharedData;
    private ConcurrentLinkedQueue<FloorData> floorDataQueue = new ConcurrentLinkedQueue<>();

    FloorData command;

    public SchedulerSubsystem(SharedDataInterface sharedData) throws InterruptedException {
        SimulatedClockSingleton clock = SimulatedClockSingleton.getInstance();
        clock.printCurrentTime();
        this.sharedData = sharedData;
        this.stateMachine = new SchedulerStateMachine(); // Initialize the state machine
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
        this.elevatorStatusList = new ArrayList<>();
        this.elevatorNumber = 0;
    }

    public void addElevatorStatus(String statusString) {
        String[] elevatorStatus = statusString.split(",");
        int elevatorId = Integer.parseInt(elevatorStatus[0]);
        int currentFloor = Integer.parseInt(elevatorStatus[1]);
        Direction direction = Direction.valueOf(elevatorStatus[2]);
        elevatorStatusList.add(new ElevatorStatus(currentFloor, direction, elevatorId));
    }


    @Override
    public void run () {
        new Thread(this::listenForFloorData).start();
        while (true) {
            try {
                SimulatedClockSingleton.getInstance().printCurrentTime();
                processRequests();
                Thread.sleep(100); // Adjust the sleep time as necessary
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.out.println("Scheduler thread was interrupted, failed to complete operation");
                break;
            }
        }
    }

    private FloorData receiveFloorData() throws RemoteException, UnknownHostException {
        FloorData floorData = null;
        /////////////////////RPC Recieve from Floor/////////////////////
        if (sharedData.getSize() < 2) {
            byte[] ReceiveFloorData = new byte[20];
            receiveFSPacket = new DatagramPacket(ReceiveFloorData, ReceiveFloorData.length);
            System.out.println("Waiting to receive from Floor Subsystem.....");
            int attempt = 0;
            boolean receivedResponse = false;
            while (attempt < 1 && !receivedResponse) {
                try {
                    floorData = StringUtil.parseInput(rpc_Receive(receiveFSPacket, receiveFSSocket, "Floor", "Floor Command"));
                    receivedResponse = true;
                    sharedData.addMessage(floorData);
                } catch (RuntimeException e) {
                    // Handle timeout exception
                    System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
                    attempt++;
                }
            }

            ///////////////////// RPC Reply to Floor /////////////////////
            byte[] ReplyData = "200".getBytes();
            replyFSPacket = new DatagramPacket(ReplyData, ReplyData.length, InetAddress.getLocalHost(), 1);
            System.out.println("Replying to Floor Subsystem.....");
            rpc_reply(replyFSPacket, replyFSSocket, "Floor");
        }
        return floorData;
    }

    private void listenForFloorData() {
        // Pseudo-code for listening for incoming floor data and adding to the queue
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Simulate receiving a FloorData object (replace with actual receiving logic)
                FloorData floorData = receiveFloorData();
                if (floorData != null) {
                    floorDataQueue.offer(floorData);
                }
            } catch (Exception e) {
                System.out.println("Error receiving floor data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void processRequests() throws Exception, InterruptedException {

        ///////////////////// RPC Receive from Elevator Subsystem /////////////////////
        byte[] ReceiveElevatorRequest = new byte[20];
        receiveES1StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 10);
        receiveES2StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 12);
        receiveES3StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 14);
        receiveES4StatusPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length, InetAddress.getLocalHost(), 16);

        System.out.println("Waiting to receive from Elevator Subsystem.....");
        int attempt2 = 0;
        boolean receivedResponse2 = false;
        String elevatorRequest1 = null;
        String elevatorRequest2 = null;
        String elevatorRequest3 = null;
        String elevatorRequest4 = null;
        while(attempt2 < 2 && !receivedResponse2) {
            try {
                elevatorRequest1 = rpc_Receive(receiveES1StatusPacket, receiveElevatorStatusSocket1, "Elevator 10", "Elevator Status");
                elevatorRequest2 = rpc_Receive(receiveES2StatusPacket, receiveElevatorStatusSocket2, "Elevator 12", "Elevator Status");
                elevatorRequest3 = rpc_Receive(receiveES3StatusPacket, receiveElevatorStatusSocket3, "Elevator 14", "Elevator Status");
                elevatorRequest4 = rpc_Receive(receiveES4StatusPacket, receiveElevatorStatusSocket4, "Elevator 16", "Elevator Status");
                addElevatorStatus(elevatorRequest1);
                addElevatorStatus(elevatorRequest2);
                addElevatorStatus(elevatorRequest3);
                addElevatorStatus(elevatorRequest4);
                receivedResponse2 = true;
            } catch (RuntimeException e) {
                // Handle timeout exception
                attempt2++;
            }
        }

        ///////////////////// RPC Reply to Elevator /////////////////////
        ArrayList<FloorData> floordataArray = chooseFloorData();
        int elevatorId;
        if(elevatorNumber != 0){
            command = sameElevator;
            elevatorId = elevatorNumber;
            elevatorNumber = 0;
        }
        else if(floordataArray.size() == 1){
            command = floordataArray.get(0);
            elevatorId = chooseElevator(command);
        }
        else {
            command = floordataArray.get(0);
            elevatorId = chooseElevator(command);
            sameElevator = floordataArray.get(1);
            elevatorNumber = elevatorId;
        }
        floordataArray = new ArrayList<>();

        byte[] ReplyToFloorData = FloorData.stringByte(command).getBytes();
        sendESPacket = new DatagramPacket(ReplyToFloorData, ReplyToFloorData.length, InetAddress.getLocalHost(), elevatorId+1);
        System.out.println("Replying to Elevator Subsystem.....");
        rpc_reply(sendESPacket, sendESSocket, "Elevator " + elevatorId);


        ///////////////////// RPC Receive Reply from Elevator /////////////////////
        byte[] ReceiveElevatorReply = new byte[3];
        receiveESPacket = new DatagramPacket(ReceiveElevatorReply, ReceiveElevatorReply.length);
        System.out.println("Waiting to receive reply from Elevator " + elevatorId + ".....");
        int attempt3 = 0;
        boolean receivedReply = false;
        while(attempt3 < 3 && !receivedReply) {
            try {
                String elevatorReply = rpc_Receive(receiveESPacket, receiveESResponseSocket, "Elevator " + elevatorId, "Reply");
                receivedReply = true;
            } catch (RuntimeException e) {
                // Handle timeout exception
                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
                attempt3++;
            }
        }
        process(command);
        attempt2 = 0;
        receivedResponse2 = false;
        while(attempt2 < 2 && !receivedResponse2) {
            try {
                elevatorRequest1 = rpc_Receive(receiveES1StatusPacket, receiveElevatorStatusSocket1, "Elevator 10", "Elevator Status");
                elevatorRequest2 = rpc_Receive(receiveES2StatusPacket, receiveElevatorStatusSocket2, "Elevator 12", "Elevator Status");
                elevatorRequest3 = rpc_Receive(receiveES3StatusPacket, receiveElevatorStatusSocket3, "Elevator 14", "Elevator Status");
                elevatorRequest4 = rpc_Receive(receiveES4StatusPacket, receiveElevatorStatusSocket4, "Elevator 16", "Elevator Status");
                addElevatorStatus(elevatorRequest1);
                addElevatorStatus(elevatorRequest2);
                addElevatorStatus(elevatorRequest3);
                addElevatorStatus(elevatorRequest4);
                receivedResponse2 = true;
            } catch (RuntimeException e) {
                // Handle timeout exception
                attempt2++;
            }
        }
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


    public String rpc_Receive(DatagramPacket packet, DatagramSocket socket, String receiver, String packetType) {
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException ste){
            System.out.println("Time out and Send again.....");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Received " + packetType + " Packet From " + receiver + ": " + StringUtil.getStringFormat(packet.getData(), packet.getLength()));
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
        synchronizer.addSchedulerCommand(command);
    }

    private int chooseElevator(FloorData command) throws RemoteException {
        ArrayList<ElevatorStatus> tempElevatorStatusListSequencial = new ArrayList<>();
        ArrayList<ElevatorStatus> sorted;
        for (int i = 0; i < elevatorStatusList.size(); i++){
            if (elevatorStatusList.get(i).getCurrentFloor() >= command.getArrivalFloor() && command.getArrivalFloor() >= command.getDestinationFloor() && (elevatorStatusList.get(i).getDirection() == Direction.DOWN || elevatorStatusList.get(i).getDirection() == Direction.STATIONARY)){
                tempElevatorStatusListSequencial.add(elevatorStatusList.get(i));
            }
            else if(elevatorStatusList.get(i).getCurrentFloor() <= command.getArrivalFloor() && command.getArrivalFloor() <= command.getDestinationFloor() && (elevatorStatusList.get(i).getDirection() == Direction.UP || elevatorStatusList.get(i).getDirection() == Direction.STATIONARY)){
                tempElevatorStatusListSequencial.add(elevatorStatusList.get(i));
            }
        }
        if (!tempElevatorStatusListSequencial.isEmpty()){
            sorted = sorterElevator(tempElevatorStatusListSequencial, command);
        }
        else{
            sorted = sorterElevator(elevatorStatusList, command);
        }
        elevatorStatusList = new ArrayList<>();
        return sorted.get(0).getId();

    }

    private ArrayList<FloorData> chooseFloorData() throws RemoteException {
        ArrayList<FloorData> tempFloorData = new ArrayList<>();
        tempFloorData.add(sharedData.getMessage());
        if (sharedData.getSize() > 1){
            tempFloorData.add(sharedData.getMessage());
            tempFloorData = sorterFloorData(tempFloorData);

            if((tempFloorData.get(0).getDirection() == tempFloorData.get(1).getDirection())) {
                if (((tempFloorData.get(0).getDirection() == Direction.UP && tempFloorData.get(0).getDestinationFloor() <= tempFloorData.get(1).getArrivalFloor()) || (tempFloorData.get(0).getDirection() == Direction.DOWN && tempFloorData.get(0).getDestinationFloor() >= tempFloorData.get(1).getArrivalFloor()))) {
                    return tempFloorData;
                }
            }
            sharedData.addMessage(tempFloorData.get(1));
            tempFloorData.remove(1);
        }
        return tempFloorData;
    }

    private ArrayList<FloorData> sorterFloorData(ArrayList<FloorData> floorData){
        ArrayList<FloorData> tempArray = floorData;

        Collections.sort(tempArray, new Comparator<FloorData>() {
            @Override
            public int compare(FloorData fd1, FloorData fd2) {

                return Integer.compare(Integer.parseInt(fd1.getTime()), Integer.parseInt(fd2.getTime()));
            }
        });

        return tempArray;
    }

    private ArrayList<ElevatorStatus> sorterElevator(ArrayList<ElevatorStatus> elevatorStatus, FloorData command){
        ArrayList<ElevatorStatus> tempArray = elevatorStatus;

        Collections.sort(tempArray, new Comparator<ElevatorStatus>() {
            @Override
            public int compare(ElevatorStatus es1, ElevatorStatus es2) {

                return Integer.compare(Math.abs(es1.getCurrentFloor() - command.getArrivalFloor()), Math.abs(es2.getCurrentFloor() - command.getArrivalFloor()));
            }
        });

        return tempArray;
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
