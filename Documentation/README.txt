===============================================================================
SYSC3303Project - Elevator Control System
===============================================================================

DESCRIPTION:
Iteration_5 of SYSC3303 for Group 8.
Based off Assignment_1, Assignment_2, and Assignment_3 current iteration models the operation of an elevator, handling floor requests, moving between floors, and scheduling these tasks using the 3 Subsystems outlined in the Project Specification orchestrated by the Java Monitor Synchornizer.java class.
It also includes 4 elevators, that are scheduled such that the most optimal elevator takes the next request.

INSTALLATION:
To import the project into IntelliJ IDEA from VCS:

1. Open IntelliJ IDEA and select "Get from Version Control" on the welcome screen.
2. In the URL field, enter `https://github.com/Zhangjason308/SYSC3303Project.git`.
3. Choose the directory where you want to clone the project and click "Clone".
4. Once cloned, IntelliJ IDEA may automatically set up everything needed to run the project. If not, proceed to open the project in IntelliJ IDEA, and it should prompt you to import Gradle or Maven projects if applicable.
5. Ensure Java SDK is set up in the project structure. This project is developed with Java 11 but should be compatible with most Java versions.

PROJECT STRUCTURE:
- DirectionEnum.java: Defines elevator movement directions.
- ElevatorSubsystem.java: Simulates elevator operations.
- FloorData.java: Data model for floor requests.
- FloorSubsystem.java: Manages floor request inputs.
- SchedulerSubsystem.java: Schedules elevator movements.
- Synchronizer.java: Coordinates communications between subsystems. //Note this class is unused now
- ThreadMain.java: Entry point, initializes subsystems.
- ElevatorEvents.csv: Input file for floor subsystem.

SAMPLE OUTPUT:

ELEVATORSUBSYSTEM

Elevator12: Attempt 1
Packet Sent To Scheduler: 12,4000,UP
Packet Received From Scheduler: 14:05:15.0,2,UP,4000
---------- ELEVATOR SUBSYSTEM 12: Received Command :[Time: 14:05:15.0, Arrival Floor: 2, Direction: UP, Destination Floor: 4000] ----------

---------- ELEVATOR SUBSYSTEM: Processing Command :[Time: 14:05:15.0, Arrival Floor: 2, Direction: UP, Destination Floor: 4000] ----------

---------- ELEVATOR SUBSYSTEM: Moving from floor 4000 to floor 2 ----------

Elevator State Change [Processing Destination Request]: DoorsClosed -> MovingDown
Elevator State: MovingDown

---------- ELEVATOR SUBSYSTEM: Stopping at floor 2 ----------

FLOORSUBSYSTEM

---------- FLOOR SUBSYSTEM: SENT REQUEST: 14:04:15.0 1 UP 4 ----------

Floor: Attempt 1
Packet Sent To Scheduler: 14:04:15.0 1 UP 4
Packet Received From Scheduler: 200

SCHEDULERSUBSYSTEM
Scheduler State: Idle

Waiting to receive from Floor Subsystem.....
Received Packet From Floor: 14:04:15.0 1 UP 4
Replying to Floor Subsystem.....
Packet Sent To Floor: 200
Waiting to receive from Elevator Subsystem.....
Received Packet From Elevator: 12,1,STATIONARY
Received Packet From Elevator: 10,1,STATIONARY
Received Packet From Elevator: 16,1,STATIONARY
Received Packet From Elevator: 14,1,STATIONARY


TESTING:

To run the unit tests for the `U_Test` class:

1. Navigate to the `src/test/java/SYSC3303Project/Test` directory in the Project view.
2. Right-click on the `U_Test` class and select **Run 'U_Test'**.
3. IntelliJ IDEA will execute all the unit tests in the `U_Test` class, and you can view the results in the Run window.

CONTRIBUTIONS:
Jason Zhang 101191526
- Elevator.java
- FloorSubsystem.java
- UML Sequence Diagram
- StringUtil.java

Caleb Lui-Yee 101187217
- SchedulerSubsystem.Java
- Elevator Scheduling Algorithm
- readme.txt
- Direction.Java

HaoChen Hou 101077553
- SchedulerSubsystem.Java
- SharedDataImpl
- SharedDataInterface

Yahya Khan 101073911
- FloorSubsystem.Java
- Sequence Diagram
- Readme.txt

Bakri Al Rajab 10116420
- Unit Testing

===============================================================================
