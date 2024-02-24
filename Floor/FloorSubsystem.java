package SYSC3303Project.Floor;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Floor.FloorData;
import SYSC3303Project.Synchronizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/**
 * FloorSubsystem.java
 * This class is a floor subsystem for an elevator real-time system. This subsystem
 * is a client thread that reads events/commands from the server, in which then it can perform
 * the Elevator movement.
 */
public class FloorSubsystem implements Runnable {
    private final Synchronizer synchronizer;
    private String fileName;


    public FloorSubsystem(Synchronizer synchronizer, String fileName) {
        this.synchronizer = synchronizer;
        this.fileName = fileName;
    }

    public Synchronizer getSynchronizer() {return synchronizer;}
    public String getFileName() {return fileName;}

    public FloorData parseInput (String inputLine) {
        String[] commands = inputLine.split("\\s+");
        String time = commands[0];
        int floor = Integer.parseInt(commands[1]);
        DirectionEnum direction = DirectionEnum.valueOf(commands[2]);
        int button = Integer.parseInt(commands[3]);
        return new FloorData(time, floor, direction, button);
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
                FloorData inputLine = parseInput(line);
                Thread.sleep(200); // next line time - current line time
                System.out.println("---------- FLOOR SUBSYSTEM: SENT REQUEST: " + inputLine + " ----------\n");
                synchronizer.addFloorCommand(inputLine);

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

}