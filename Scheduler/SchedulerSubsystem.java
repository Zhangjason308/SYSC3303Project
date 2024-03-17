package SYSC3303Project.Scheduler;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Floor.StringUtil;
import SYSC3303Project.Scheduler.StateMachine.SchedulerStateMachine;
import SYSC3303Project.SharedDataImpl;
import SYSC3303Project.SharedDataInterface;
import SYSC3303Project.Synchronizer;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Comparator;

import java.util.Collections;
import java.util.Comparator;

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

    DatagramPacket receiveFSPacket, replyFSPacket, receiveESPacket, sendESPacket;
    DatagramSocket receiveFSSocket, replyFSSocket, receiveESSocket, sendESSocket;
    SharedDataInterface sharedData;
    FloorData command;

    public SchedulerSubsystem(SharedDataInterface sharedData) throws InterruptedException {
        this.sharedData = sharedData;
        this.stateMachine = new SchedulerStateMachine(); // Initialize the state machine
        try {
            this.replyFSSocket = new DatagramSocket(2);
            this.receiveFSSocket = new DatagramSocket(3);
            this.receiveESSocket = new DatagramSocket(4);
            this.sendESSocket = new DatagramSocket(5);
            replyFSSocket.setSoTimeout(10000);
            sendESSocket.setSoTimeout(10000);
            receiveFSSocket.setSoTimeout(10000);
            receiveESSocket.setSoTimeout(10000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        this.elevatorStatusList = new ArrayList<>();
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
        while (true) {
            try {
                processRequests();
                Thread.sleep(100); // Adjust the sleep time as necessary
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.out.println("Scheduler thread was interrupted, failed to complete operation");
                break;
            }
        }
    }


    private void processRequests() throws Exception, InterruptedException {

        /////////////////////RPC Recieve from Floor/////////////////////
        byte[] ReceiveFloorData = new byte[20];
        receiveFSPacket = new DatagramPacket(ReceiveFloorData, ReceiveFloorData.length);
        System.out.println("Waiting to receive from Floor Subsystem.....");
        int attempt = 0;
        boolean receivedResponse = false;
        while(attempt < 10 && !receivedResponse) {
            try {
                FloorData floorData = StringUtil.parseInput(rpc_Receive(receiveFSPacket, receiveFSSocket, "Floor"));
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
        replyFSPacket = new DatagramPacket(ReplyData, ReplyData.length, InetAddress.getLocalHost(),1);
        System.out.println("Replying to Floor Subsystem.....");
        rpc_reply(replyFSPacket, replyFSSocket, "Floor");


        ///////////////////// RPC Receive from Elevator Subsystem /////////////////////
        byte[] ReceiveElevatorRequest = new byte[20];
        receiveESPacket = new DatagramPacket(ReceiveElevatorRequest, ReceiveElevatorRequest.length);
        System.out.println("Waiting to receive from Elevator Subsystem.....");
        int attempt2 = 0;
        int receivedResponse2 = 0;
        String elevatorRequest = null;
        while(attempt2 < 3 && receivedResponse2 < 4) {
            try {
                elevatorRequest = rpc_Receive(receiveESPacket, receiveESSocket, "Elevator");
                receivedResponse2++;
                addElevatorStatus(elevatorRequest);
            } catch (RuntimeException e) {
                // Handle timeout exception
                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
                attempt2++;
            }
        }


        ///////////////////// RPC Reply to Elevator /////////////////////
        command = sharedData.getMessage();
        int elevatorId = chooseElevator(command);
        byte[] ReplyToFloorData = FloorData.stringByte(command).getBytes();
        sendESPacket = new DatagramPacket(ReplyToFloorData, ReplyToFloorData.length, InetAddress.getLocalHost(), elevatorId+1);
        System.out.println("Replying to Elevator Subsystem.....");
        rpc_reply(sendESPacket, sendESSocket, "Elevator");


        ///////////////////// RPC Receive Reply from Elevator /////////////////////
        byte[] ReceiveElevatorReply = new byte[3];
        receiveESPacket = new DatagramPacket(ReceiveElevatorReply, ReceiveElevatorReply.length);
        System.out.println("Waiting to receive reply from Elevator.....");
        int attempt3 = 0;
        boolean receivedReply = false;
        while(attempt3 < 3 && !receivedReply) {
            try {
                String elevatorReply = rpc_Receive(receiveESPacket, receiveESSocket, "Elevator");
                receivedReply = true;
            } catch (RuntimeException e) {
                // Handle timeout exception
                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
                attempt3++;
            }
        }
        process(command);
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
            sorted = sorter(tempElevatorStatusListSequencial, command);
        }
        else{
            sorted = sorter(elevatorStatusList, command);
        }
        elevatorStatusList = new ArrayList<>();
        return sorted.get(0).getId();


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


    public static void main(String[] args) throws Exception{

        SharedDataImpl sharedData = new SharedDataImpl();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("SharedData", sharedData);

        SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem((sharedData));
        Thread thread = new Thread(schedulerSubsystem);
        thread.start();
    }
}
