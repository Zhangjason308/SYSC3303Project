package SYSC3303Project.Elevator;

public class HardFault {

    private String faultType;

    @Override
    public String toString() {
        return "HardFault{" +
                "faultType='" + faultType +
                '}';
    }

    public HardFault(String faultType) {
        this.faultType = faultType;
    }

    // Getters and Setters
    public String getFaultType() {
        return faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }


}
