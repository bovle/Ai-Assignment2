package problem;

class State {
    public String car;
    public String driver;
    public Tire tireModel;
    public TirePressure tirePressure;
    public int fuel;
    public int cellIndex;
    public int timeStep;

    public State(simulator.State simState, int timeStep) {
        this(simState.getCarType(), simState.getDriver(), simState.getTireModel(), simState.getTirePressure(),
                simState.getFuel(), simState.getPos() - 1, timeStep);
    }

    public State(String car, String driver, Tire tireModel, TirePressure tirePressure, int fuel, int cellIndex,
            int timeStep) {
        this.car = car;
        this.driver = driver;
        this.tireModel = tireModel;
        this.tirePressure = tirePressure;
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
        if (car.equals(other.car) && driver.equals(other.driver) && tireModel.equals(other.tireModel)
                && tirePressure.equals(other.tirePressure) && fuel == other.fuel && cellIndex == other.cellIndex
                && timeStep == other.timeStep) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(" + car + ", " + driver + ", " + tireModel + ", " + tirePressure + ", " + fuel + ", " + cellIndex
                + ", " + timeStep + ")";
    }
}