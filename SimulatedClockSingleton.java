package SYSC3303Project;

public class SimulatedClockSingleton {
    private static SimulatedClockSingleton instance;
    private long currentTime = 0;
    private final long tickInterval = 1000; // Example: 1 second
    private boolean running = true;

    private SimulatedClockSingleton() {}

    public static synchronized SimulatedClockSingleton getInstance() {
        if (instance == null) {
            instance = new SimulatedClockSingleton();
            instance.startClock();
        }
        return instance;
    }

    public void printCurrentTime() {
        long hours = currentTime / 3600;
        long minutes = (currentTime % 3600) / 60;
        long seconds = currentTime % 60;

        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        System.out.println("Current Simulated Time: " + timeFormatted);
    }

    private void startClock() {
        Thread clockThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(tickInterval);
                    currentTime++;
                    // Notify listeners or just update the time
                } catch (InterruptedException e) {
                    running = false;
                    Thread.currentThread().interrupt();
                }
            }
        });
        clockThread.start();
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void stopClock() {
        running = false;
    }
}


