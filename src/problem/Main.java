package problem;

import java.io.IOException;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        ProblemSpec ps;
        try {
            ps = new ProblemSpec("examples/level_1/input1.txt");
            System.out.println(ps.toString());
            Utils.pSpec = ps;
            Sampler sampler = new Sampler();
            MCTS mcts = new MCTS(sampler);

            String car = (String) Utils.pSpec.carToIndex.keySet().toArray()[0];
            String driver = (String) Utils.pSpec.driverMoveProbability.keySet().toArray()[0];
            String tyreType = (String) Utils.pSpec.tyreModelMoveProbability.keySet().toArray()[0];
            State currentState = new State(car, driver, tyreType, "100", ProblemSpec.FUEL_MAX, 0, 0);
            while (currentState.cellIndex != Utils.pSpec.N - 1 && currentState.timeStep < Utils.pSpec.maxT) {
                ActionDetail nextAction = mcts.search(currentState, 15);
                System.out.println(
                        nextAction.action.toString() + " | " + currentState.cellIndex + " | " + currentState.timeStep);
                State nextState = mcts.transition(currentState, nextAction);
                currentState = nextState;
            }

        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");

    }
}
