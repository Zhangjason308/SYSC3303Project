package SYSC3303Project;

public class SimulatedClockSingleton {
    private static SimulatedClockSingleton instance;

    private SimulatedClockSingleton() {}

    public static synchronized SimulatedClockSingleton getInstance() {
        if (instance == null) {
            instance = new SimulatedClockSingleton();
        }
        return instance;
    }

    public void printCurrentTime() {
        // Get current system time in milliseconds since epoch
        long currentTimeMillis = System.currentTimeMillis();

        // Convert milliseconds to hours, minutes, seconds, and tenths of a second
        long hours = (currentTimeMillis / (3600 * 1000)) % 24; // Mod 24 for hours in a day
        long minutes = (currentTimeMillis / (60 * 1000)) % 60; // Mod 60 for minutes in an hour
        long seconds = (currentTimeMillis / 1000) % 60; // Mod 60 for seconds in a minute
        long tenths = (currentTimeMillis / 100) % 10; // Get tenths of a second

        // Format the time and print
        String timeFormatted = String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, tenths);
        System.out.print("[" + timeFormatted + "]");
    }

    public long getCurrentTime() {
        // Return current system time in milliseconds since epoch
        return System.currentTimeMillis();
    }
}
