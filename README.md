# SYSC3303Project - Elevator Control System

## Description
Iteration_1 of SYSC3303 for Group 8.  
Based off Assignment_1, the current iteration models the operation of an elevator, handling floor requests, moving between floors, and scheduling these tasks using the 3 Subsystems outlined in the Project Specification orchestrated by the Java Monitor `Synchronizer.java` class.

## Installation
To import the project into IntelliJ IDEA from VCS:

1. Open IntelliJ IDEA and select **"Get from Version Control"** on the welcome screen.
2. In the URL field, enter `https://github.com/Zhangjason308/SYSC3303Project.git`.
3. Choose the directory where you want to clone the project and click **"Clone"**.
4. Once cloned, IntelliJ IDEA may automatically set up everything needed to run the project. If not, proceed to open the project in IntelliJ IDEA, and it should prompt you to import Gradle or Maven projects if applicable.
5. Ensure Java SDK is set up in the project structure. This project is developed with Java 11 but should be compatible with most Java versions.

## Project Structure
- `DirectionEnum.java`: Defines elevator movement directions.
- `ElevatorSubsystem.java`: Simulates elevator operations.
- `FloorData.java`: Data model for floor requests.
- `FloorSubsystem.java`: Manages floor request inputs.
- `SchedulerSubsystem.java`: Schedules elevator movements.
- `Synchronizer.java`: Coordinates communications between subsystems.
- `ThreadMain.java`: Entry point, initializes subsystems.
- `ElevatorEvents.csv`: Input file for floor subsystem.

## Sample Output
Elevator is moving to floor 1 to pickup passenger  
Elevator has arrived at floor 1  
Elevator is moving to floor 4  
Elevator has arrived at floor 4, passengers have been dropped off  
Elevator is moving to floor 2 to pickup passenger  
Elevator has arrived at floor 2  
Elevator is moving to floor 4  
Elevator has arrived at floor 4, passengers have been dropped off  
Elevator is moving to floor 3 to pickup passenger  
Elevator has arrived at floor 3  
Elevator is moving to floor 4  
Elevator has arrived at floor 4, passengers have been dropped off


## Contributions
- **Jason Zhang 101191526**
  - `DirectionEnum.java`
  - `ElevatorSubsystem.java`
  - `FloorData.java`
  - `FloorSubsystem.java`
  - `SchedulerSubsystem.java`
  - `Synchronizer.java`

- **Caleb Lui-Yee 101187217**
  - `ThreadMain.java`

- **HaoChen Hou 101077553**
  - Unit Testing

- **Yahya Khan 101073911**
  - UML Class Diagram
  - UML Sequence Diagram
  - Readme.txt

- **Bakri Al Rajab 10116420**
  - Unit Testing
