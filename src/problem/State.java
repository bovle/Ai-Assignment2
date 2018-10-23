package problem;

class State {
    public String car;
    public String driver;
    public String tyreType;
    public String tyrePressure;
    public int fuel;
    public int cellIndex;
    public int timeStep;

    public State(String car, String driver, String tyreType, String tyrePressure, int fuel, int cellIndex,
            int timeStep) {
        this.car = car;
        this.driver = driver;
        this.tyreType = tyreType;
        this.tyrePressure = tyrePressure;
        this.fuel = fuel;
        this.cellIndex = cellIndex;
        this.timeStep = timeStep;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State)) {
            return false;
        }
        State other = (State) obj;
        if (car.equals(other.car) && driver.equals(other.driver) && tyreType.equals(other.tyreType)
                && tyrePressure.equals(other.tyrePressure) && fuel == other.fuel && cellIndex == other.cellIndex
                && timeStep == other.timeStep) {
            return true;
        } else {
            return false;
        }
    }
}