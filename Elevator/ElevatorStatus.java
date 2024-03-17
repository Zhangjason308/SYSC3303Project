package SYSC3303Project.Elevator;
    public class ElevatorStatus {
        private int currentFloor;
        private Direction direction;

        public ElevatorStatus(int currentFloor, Direction direction) {
            this.currentFloor = currentFloor;
            this.direction = direction;
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

