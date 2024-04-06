package SYSC3303Project.Elevator;

public class DoorFault {
    private String faultType;
    private int retryAttempts;

    @Override
    public String toString() {
        return "DoorFault{" +
                "faultType='" + faultType + '\'' +
                ", retryAttempts=" + retryAttempts +
                '}';
    }

    public DoorFault(String faultType, int retryAttempts) {
        this.faultType = faultType;
        this.retryAttempts = retryAttempts;
    }

    // Getters and Setters
    public String getFaultType() {
        return faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    // Method to decrement retry attempts
    public void decrementRetryAttempts() {
        if (retryAttempts > 0) {
            retryAttempts--;
        }
    }

}
