package SYSC3303Project.Elevator;

public interface ElevatorState {
    void displayState();
}
class Idle implements ElevatorState{
    public void receivedCommand(ElevatorSubsystem elevatorSubsystem){
        elevatorSubsystem.setState("Moving" + elevatorSubsystem.getDirection());
    }
    @Override
    public void displayState() {
        System.out.println("Idle");
    }
}

class MovingUp implements ElevatorState {
    public void arrivedAtFloor(ElevatorSubsystem elevatorSubsystem){
        elevatorSubsystem.setState("Stopped");
    }

    @Override
    public void displayState() {
        System.out.println("Moving Up");
    }
}

class MovingDown implements ElevatorState {
    public void arrivedAtFloor(ElevatorSubsystem elevatorSubsystem){
        elevatorSubsystem.setState("Stopped");
    }

    @Override
    public void displayState() {
        System.out.println("Moving Down");
    }
}

class Stopped implements ElevatorState {
    public void openedDoors(ElevatorSubsystem elevatorSubsystem){
        elevatorSubsystem.setState("DoorsOpen");
    }

    @Override
    public void displayState() {
        System.out.println("Stopped");
    }
}

class DoorsOpen implements ElevatorState{
    public void doorsClosed(ElevatorSubsystem elevatorSubsystem){
        elevatorSubsystem.setState("DoorsClose");
    }

    @Override
    public void displayState() {
        System.out.println("Doors Open");
    }
}

class DoorsClosed implements ElevatorState{
    public void signaledScheduler(ElevatorSubsystem elevatorSubsystem){
        elevatorSubsystem.setState("Idle");
    }

    @Override
    public void displayState() {
        System.out.println("Doors Closed");
    }
}