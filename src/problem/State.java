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
}