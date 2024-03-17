package SYSC3303Project.Elevator;
    public class ElevatorStatus {
        private int currentFloor;
        private Direction direction;

        private int id;

        public ElevatorStatus(int currentFloor, Direction direction, int id) {
            this.currentFloor = currentFloor;
            this.direction = direction;
            this.id = id;
        }

        public int getId() {
            return id;
        }
        // Getters and setters
        public int getCurrentFloor() {
            return currentFloor;
        }

        public void setCurrentFloor(int currentFloor) {
            this.currentFloor = currentFloor;
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }
    }

