package SYSC3303Project;

/**
 * SharedDataInterface.java
 * The SharedDataInterface is a remote interface that defines methods for
 * interacting with shared data in a distributed system using Java RMI (Remote
 * Method Invocation).
 */
import SYSC3303Project.Floor.FloorData;

import java.net.DatagramPacket;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SharedDataInterface extends Remote {

    /**
     * Retrieves a message from the shared data.
     *
     * @return a String representing the retrieved message.
     * @throws RemoteException if a communication-related exception occurs during the remote method invocation.
     */
    FloorData getMessage() throws RemoteException;

    /**
     * Adds a message to the shared data.
     *
     * @param message a String representing the message to be added to the shared data.
     * @throws RemoteException if a communication-related exception occurs during the remote method invocation.
     */
    void addMessage(FloorData message) throws RemoteException;
}

