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

    public static FloorData parseInput (String inputLine) {
        String[] commands = inputLine.split("\\s+");
        String time = commands[0];
        int floor = Integer.parseInt(commands[1]);
        Direction direction = Direction.valueOf(commands[2]);
        int button = Integer.parseInt(commands[3]);
        return new FloorData(time, floor, direction, button);
    }

    public static String getStringFormat (byte[] bytes, int length) {

        System.arraycopy(bytes, 0, new byte[length], 0, length);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] <= 10) {
                bytes[i] = (byte) (bytes[i] + 48);
            }
        }
        return new String(bytes);
    }
}
