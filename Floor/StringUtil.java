package SYSC3303Project.Floor;

import SYSC3303Project.DirectionEnum;
import SYSC3303Project.Elevator.Direction;

import java.net.DatagramPacket;
import java.util.Arrays;

public class StringUtil {

    public static String getStringFormat (DatagramPacket packet) {
        byte[] newBytes = Arrays.copyOf(packet.getData(), packet.getData().length);
        for (int i = 0; i < newBytes.length; i++) {
            if (newBytes[i] <= 10) {
                newBytes[i] = (byte) (newBytes[i] + 48);
            }
        }
        return new String(newBytes);
    }

    public static FloorData parseInputWithComma (String inputLine) {
        String[] commands = inputLine.split(",");
        String time = commands[0];
        int floor = Integer.parseInt(commands[1]);
        Direction direction = Direction.valueOf(commands[2]);
        int button = Integer.parseInt(commands[3]);
        return new FloorData(time, floor, direction, button);
    }

    public static FloorData parseInput (String inputLine) {
        String[] commands = inputLine.split("\\s+");
        String time = commands[0];
        int floor = Integer.parseInt(commands[1]);
        Direction direction = Direction.valueOf(commands[2]);
        int button = Integer.parseInt(commands[3]);
        return new FloorData(time, floor, direction, button);
    }

    public static String getStringFormat(byte[] bytes, int length) {
        // First, find the actual length considering trailing zeros or specified padding
        int actualLength = length;
        for (int i = length - 1; i >= 0; i--) {
            if (bytes[i] != 0) {
                // +1 because actualLength should be the index of the last non-zero byte + 1
                actualLength = i + 1;
                break;
            }
        }

        // Create a new array with the actual length to remove trailing unneeded bytes
        byte[] newBytes = new byte[actualLength];
        System.arraycopy(bytes, 0, newBytes, 0, actualLength);

        // Optional: Perform additional processing on newBytes if needed
        // For example, the original method incremented bytes by 48 if they were <= 10.
        // This behavior has been commented out for clarity, as it might not be desired in all cases.
        // for (int i = 0; i < newBytes.length; i++) {
        //     if (newBytes[i] <= 10) {
        //         newBytes[i] = (byte) (newBytes[i] + 48);
        //     }
        // }

        return new String(newBytes);
    }
}
