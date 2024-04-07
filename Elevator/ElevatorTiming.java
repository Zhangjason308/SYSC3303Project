package SYSC3303Project.Elevator;

public class ElevatorTiming {

    // All times from the Data Collection are divided by 2 to be able to run the terminal correctly
    public static final int DOORS_OPENING = 2500; // represents time to open doors (5 secs from data collection)
    public static final int DOORS_CLOSING = 2500; // represents time to open doors (5 secs from data collection)
    public static final int TRANSIENT_FAULT = 20000; // represents time to open doors (5 secs from data collection)


    public static final int TRAVEL_TIME_1_FLOOR = 6945; // represents the time to move to one floor (13.89 secs from data collection)

    public static int getTravelTime (int floorNumbers) {
        return (5975 * floorNumbers + 8075) / 2;
    }

}
