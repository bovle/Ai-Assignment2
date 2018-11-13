package problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.util.ElementScanner6;

public class Node {
    public Node parent;
    public Node[] children;
    public State state;
    public float reward;
    public int count;
    public Action action;
    public List<ActionType> freeActions;

    public Node(Node parent, Action action, State s, List<ActionType> freeActions) {
        this(parent, action, s, freeActions, false);
    }

    public Node(Node parent, Action action, State s, List<ActionType> freeActions, boolean andNode) {
        this.parent = parent;
        this.state = s;
        this.reward = 0;
        this.count = 0;
        this.action = action;
        this.freeActions = freeActions;
        if (andNode) {
            children = new Node[12];
        } else {
            int numTires = 4;
            int numPressure = 3;
            switch (Utils.pSpec.getLevel().getLevelNumber()) {
            case 1:
                int numCars = 2;
                int numDrivers = 2;
                children = new Node[1 + numCars - 1 + numDrivers - 1 + numTires - 1];
                break;
            case 2:
                numCars = 3;
                numDrivers = 2;
                children = new Node[1 + (numCars - 1) + (numDrivers - 1) + (numTires - 1) + 1 + (numPressure - 1)];
                break;
            case 3:
            case 4:
            case 5:
                numCars = 5;
                numDrivers = 5;
                children = new Node[1 + (numCars - 1) + (numDrivers - 1) + (numTires - 1) + 1 + (numPressure - 1)];
                break;
            default:
                break;
            }
        }
    }

    public int getIndexFromAction(Action a) {
        int levelNum = Utils.pSpec.getLevel().getLevelNumber();
        switch (a.getActionType()) {
        case MOVE:
            return 0;
        case CHANGE_CAR:
            List<String> cars = new ArrayList<>(Utils.pSpec.getCarOrder());
            cars.remove(this.state.car);
            return 1 + cars.indexOf(a.getCarType());
        case CHANGE_DRIVER:
            List<String> drivers = new ArrayList<>(Utils.pSpec.getDriverOrder());
            drivers.remove(this.state.driver);
            if (levelNum == 1)
                return 2 + drivers.indexOf(a.getDriverType());
            else if (levelNum == 2)
                return 3 + drivers.indexOf(a.getDriverType());
            else
                return 5 + drivers.indexOf(a.getDriverType());
        case CHANGE_TIRES:
            List<Tire> tires = new ArrayList<>(Utils.pSpec.getTireOrder());
            tires.remove(this.state.tireModel);
            if (levelNum == 1)
                return 3 + tires.indexOf(a.getTireModel());
            else if (levelNum == 2)
                return 4 + tires.indexOf(a.getTireModel());
            else
                return 9 + tires.indexOf(a.getTireModel());
        case ADD_FUEL:
            if (levelNum == 1)
                System.out.println("should not be able to add fuel at level 1");
            else if (levelNum == 2)
                return 7;
            else
                return 12;
        case CHANGE_PRESSURE:
            int pressureIndex = a.getTirePressure().ordinal();
            if (pressureIndex == 2)
                pressureIndex = 1;
            if (pressureIndex == 1) {
                if (this.state.tirePressure.ordinal() == 2)
                    pressureIndex = 1;
                else
                    pressureIndex = 0;
            }

            if (levelNum == 1)
                System.out.println("should not be able to add fuel at level 1");
            else if (levelNum == 2)
                return 8 + pressureIndex;
            else
                return 13 + pressureIndex;
        default:
            System.out.println("how did we get here 6?");
            System.exit(1);
        }
        return -1;
    }

    public Action initActionFromIndex(int index) {
        if (index == 0) {
            return new Action(ActionType.MOVE);
        }
        switch (Utils.pSpec.getLevel().getLevelNumber()) {
        case 1:
            if (index < 2) {
                index -= 1;
                ActionType at = ActionType.CHANGE_CAR;
                List<String> cars = new ArrayList<>(Utils.pSpec.getCarOrder());
                cars.remove(this.state.car);
                return new Action(at, cars.get(index));
            }
            if (index < 3) {
                index -= 2;
                ActionType at = ActionType.CHANGE_DRIVER;
                List<String> drivers = new ArrayList<>(Utils.pSpec.getDriverOrder());
                drivers.remove(this.state.driver);
                return new Action(at, drivers.get(index));
            } else {
                index -= 3;
                ActionType at = ActionType.CHANGE_TIRES;
                List<Tire> tires = new ArrayList<>(Utils.pSpec.getTireOrder());
                tires.remove(this.state.tireModel);
                return new Action(at, tires.get(index));
            }
        case 2:
            if (index < 3) {
                index -= 1;
                ActionType at = ActionType.CHANGE_CAR;
                List<String> cars = new ArrayList<>(Utils.pSpec.getCarOrder());
                cars.remove(this.state.car);
                return new Action(at, cars.get(index));
            }
            if (index < 4) {
                index -= 3;
                ActionType at = ActionType.CHANGE_DRIVER;
                List<String> drivers = new ArrayList<>(Utils.pSpec.getDriverOrder());
                drivers.remove(this.state.driver);
                return new Action(at, drivers.get(index));
            }
            if (index < 7) {
                index -= 4;
                ActionType at = ActionType.CHANGE_TIRES;
                List<Tire> tires = new ArrayList<>(Utils.pSpec.getTireOrder());
                tires.remove(this.state.tireModel);
                return new Action(at, tires.get(index));
            }

            if (index < 8) {
                ActionType at = ActionType.ADD_FUEL;
                int amount = ProblemSpec.FUEL_MAX - this.state.fuel;
                if (amount > 10)
                    amount = 10;
                return new Action(at, amount);
            } else {
                index -= 8;
                ActionType at = ActionType.CHANGE_PRESSURE;
                int currentPressureIndex = this.state.tirePressure.ordinal();
                int pressureIndex = (currentPressureIndex + index + 1) % 3;
                TirePressure pressure = TirePressure.values()[pressureIndex];
                return new Action(at, pressure);
            }
        case 3:
        case 4:
        case 5:
            if (index < 5) {
                index -= 1;
                ActionType at = ActionType.CHANGE_CAR;
                List<String> cars = new ArrayList<>(Utils.pSpec.getCarOrder());
                cars.remove(this.state.car);
                return new Action(at, cars.get(index));
            }
            if (index < 9) {
                index -= 5;
                ActionType at = ActionType.CHANGE_DRIVER;
                List<String> drivers = new ArrayList<>(Utils.pSpec.getDriverOrder());
                drivers.remove(this.state.driver);
                return new Action(at, drivers.get(index));
            }
            if (index < 12) {
                index -= 9;
                ActionType at = ActionType.CHANGE_TIRES;
                List<Tire> tires = new ArrayList<>(Utils.pSpec.getTireOrder());
                tires.remove(this.state.tireModel);
                return new Action(at, tires.get(index));
            }

            if (index < 13) {
                ActionType at = ActionType.ADD_FUEL;
                int amount = ProblemSpec.FUEL_MAX - this.state.fuel;
                if (amount > 10)
                    amount = 10;
                return new Action(at, amount);
            } else {
                index -= 13;
                ActionType at = ActionType.CHANGE_PRESSURE;
                int currentPressureIndex = this.state.tirePressure.ordinal();
                int pressureIndex = (currentPressureIndex + index + 1) % 3;
                TirePressure pressure = TirePressure.values()[pressureIndex];
                return new Action(at, pressure);
            }
        default:
            System.out.println("how did we get here 7?");
            System.exit(1);
        }
        return null;
    }

}
