package SYSC3303Project;

/**
 * SharedDataImpl.java
 *
 * The SharedDataImpl class is an implementation of the SharedDataInterface
 * remote interface. It provides methods to interact with shared data in a
 * distributed system using Java RMI (Remote Method Invocation).
 */
import SYSC3303Project.Floor.FloorData;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;


public class SharedDataImpl extends UnicastRemoteObject implements SharedDataInterface {

    /** The queue to store messages as shared data. */
    private Queue<FloorData> messages;

    /**
     * Constructs a new SharedDataImpl instance. Initializes the message queue.
     *
     * @throws RemoteException if a communication-related exception occurs during the remote object export.
     */
    public SharedDataImpl() throws RemoteException {
        super();
        this.messages = new LinkedList<>();
    }

    /**
     * Retrieves a message from the shared data.
     *
     * @return a String representing the retrieved message.
     * @throws RemoteException if a communication-related exception occurs during the remote method invocation.
     */
    @Override
    public synchronized FloorData getMessage() throws RemoteException {
        return messages.remove();
    }

    /**
     * Adds a message to the shared data.
     *
     * @param message a String representing the message to be added to the shared data.
     * @throws RemoteException if a communication-related exception occurs during the remote method invocation.
     */
    @Override
    public synchronized void addMessage(FloorData message) throws RemoteException {
        messages.add(message);
    }
}

