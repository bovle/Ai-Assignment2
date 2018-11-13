package problem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import simulator.Simulator;

public class Main {
    private static DecimalFormat df = new DecimalFormat("#.####");

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        // randomValues(40, false, 0.3, false);
        ProblemSpec ps;
        try {
            ps = new ProblemSpec(args[0]);
            System.out.println(ps.toString());
            Utils.pSpec = ps;
            Sampler sampler = new Sampler();
            MCTS mcts = new MCTS(sampler);
            Simulator simulator = new Simulator(ps, args[1]);

            String car = Utils.pSpec.getFirstCarType();
            String driver = Utils.pSpec.getFirstDriver();
            Tire tireModel = Utils.pSpec.getFirstTireModel();
            State initState = new State(car, driver, tireModel, TirePressure.ONE_HUNDRED_PERCENT, ProblemSpec.FUEL_MAX,
                    0, 0);
            Node currentNode = new Node(null, null, initState, null);
            while (currentNode.state.cellIndex != Utils.pSpec.getN() - 1
                    && currentNode.state.timeStep < Utils.pSpec.getMaxT()) {
                Action nextAction = mcts.search(currentNode, 13);
                List<ActionType> nextFreeActions = mcts.getFreeActions(currentNode, nextAction);
                // lookahead
                if (nextFreeActions != null) {
                    State nextState = mcts.nonMoveTransition(currentNode.state, nextAction);
                    if (nextState.cellIndex == Utils.pSpec.getN() - 1 || nextState.timeStep >= Utils.pSpec.getMaxT())
                        break;
                    Node nextNode = new Node(null, null, nextState, nextFreeActions);
                    Action nextNextAction = mcts.search(nextNode, 1);
                    if (nextFreeActions.contains(nextNextAction.getActionType())) {
                        nextAction = combineActions(nextAction, nextNextAction, currentNode.state);
                    }
                }

                simulator.State simState = simulator.step(nextAction);
                if (simState == null)
                    break;

                Node nextNode = new Node(null, null, new State(simState, simulator.getSteps()), nextFreeActions);
                currentNode = nextNode;
            }
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");

    }

    private static Action combineActions(Action a1, Action a2, State state) {
        if (a1.getActionType() == ActionType.CHANGE_CAR) {
            return new Action(ActionType.CHANGE_CAR_AND_DRIVER, a1.getCarType(), a2.getDriverType());
        }
        if (a1.getActionType() == ActionType.CHANGE_DRIVER) {
            return new Action(ActionType.CHANGE_CAR_AND_DRIVER, a2.getCarType(), a1.getDriverType());
        }
        if (a1.getActionType() == ActionType.ADD_FUEL) {
            if (a2.getActionType() == ActionType.CHANGE_PRESSURE) {
                return new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, state.tireModel, a1.getFuel(),
                        a2.getTirePressure());
            }
            if (a2.getActionType() == ActionType.CHANGE_TIRES) {
                return new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, a2.getTireModel(), a1.getFuel(),
                        state.tirePressure);
            }
        }
        if (a1.getActionType() == ActionType.CHANGE_PRESSURE) {
            if (a2.getActionType() == ActionType.ADD_FUEL) {
                return new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, state.tireModel, a2.getFuel(),
                        a1.getTirePressure());
            }
            if (a2.getActionType() == ActionType.CHANGE_TIRES) {
                return new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, a2.getTireModel(), state.fuel,
                        a1.getTirePressure());
            }
        }
        if (a1.getActionType() == ActionType.CHANGE_TIRES) {
            if (a2.getActionType() == ActionType.ADD_FUEL) {
                return new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, a1.getTireModel(), a2.getFuel(),
                        state.tirePressure);
            }
            if (a2.getActionType() == ActionType.CHANGE_PRESSURE) {
                return new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, a1.getTireModel(), state.fuel,
                        a2.getTirePressure());
            }
        }
        return null;
    }

    private static void randomValues(int amount, boolean sumToOne, double maxValue, boolean isInt) {
        double[] randomValues = new double[amount];
        double sum = 0;
        for (int i = 0; i < amount; i++) {
            randomValues[i] = Math.random() * maxValue;
            sum += randomValues[i];
        }
        if (sumToOne) {
            for (int i = 0; i < amount; i++) {
                randomValues[i] = randomValues[i] / sum;

            }
        } else if (isInt) {
            for (int i = 0; i < amount; i++) {
                randomValues[i] = Math.round(randomValues[i]);

            }
        }
        for (double num : randomValues) {
            System.out.print(df.format(num).replace(",", "."));
            System.out.print(" ");
        }
        System.out.println();
    }
}

/*
 * generate random values for distributions double[] randomValues = new
 * double[12]; double sum = 0; for (int i = 0; i < 12; i++) { randomValues[i] =
 * Math.random(); sum += randomValues[i]; } for (int i = 0; i < 12; i++) {
 * randomValues[i] = randomValues[i] / sum;
 * 
 * } for (double num : randomValues) {
 * System.out.print(df.format(num).replace(",", ".")); System.out.print(" "); }
 * System.out.println();
 */
