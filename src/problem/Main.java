package problem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

public class Main {
    private static DecimalFormat df = new DecimalFormat("#.####");

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        // randomValues(40, false, 0.3, false);
        ProblemSpec ps;
        try {
            ps = new ProblemSpec("examples/level_5/input1.txt");
            System.out.println(ps.toString());
            Utils.pSpec = ps;
            Sampler sampler = new Sampler();
            MCTS mcts = new MCTS(sampler);

            String car = (String) Utils.pSpec.carToIndex.keySet().toArray()[0];
            String driver = (String) Utils.pSpec.driverMoveProbability.keySet().toArray()[0];
            String tyreType = (String) Utils.pSpec.tyreModelMoveProbability.keySet().toArray()[0];
            State initState = new State(car, driver, tyreType, "100", ProblemSpec.FUEL_MAX, 0, 0);
            Node currentNode = new Node(null, null, initState, null);
            while (currentNode.state.cellIndex != Utils.pSpec.N - 1 && currentNode.state.timeStep < Utils.pSpec.maxT) {
                System.out.println();
                System.out.println(currentNode.state.toString());
                ActionDetail nextAction = mcts.search(currentNode, 15);
                Node nextNode = mcts.Simulate(currentNode, nextAction);
                currentNode = nextNode;
            }
            System.out.println();
            System.out.println(currentNode.state.toString());
            if (currentNode.state.cellIndex == Utils.pSpec.N - 1) {
                System.out.println("Success");
            } else {
                System.out.println("failure");
            }

        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");

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
