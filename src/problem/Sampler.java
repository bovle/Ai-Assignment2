package problem;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Sampler {

    Map<String, float[]> moveProbabilities;
    Random rand;

    public Sampler() {
        moveProbabilities = new HashMap<>();
        rand = new Random();
    }

    public int SampleMove(State currentState) {
        float[] carProbabilities = Utils.pSpec.carMoveProbability.get(currentState.car);
        float[] driverProbabilities = Utils.pSpec.driverMoveProbability.get(currentState.driver);
        float[] tyreProbabilities = Utils.pSpec.tyreModelMoveProbability.get(currentState.tyreType);

        String comboString = currentState.car + currentState.driver + currentState.tyreType;

        if (!moveProbabilities.containsKey(comboString)) {

            // SUM_OVER_ALL_K(P(K|D) * P(K|C) * P(K|T))
            float probabilitySum = 0;
            for (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) {
                probabilitySum += driverProbabilities[i] * carProbabilities[i] * tyreProbabilities[i];
            }

            // P(K_i|D) * P(K_i|C) * P(K_i|T) / SUM_OVER_ALL_K(P(K|D) * P(K|C) * P(K|T))
            float[] resultProbability = new float[ProblemSpec.CAR_MOVE_RANGE];
            for (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) {
                resultProbability[i] = (driverProbabilities[i] * carProbabilities[i] * tyreProbabilities[i])
                        / probabilitySum;
            }

            moveProbabilities.put(comboString, resultProbability);
        }

        int carIndex = Utils.pSpec.carToIndex.get(currentState.car);
        int terrainIndex = Utils.pSpec.terrainToIndex.get(Utils.pSpec.environmentMap[currentState.cellIndex]);
        float slipProbability = Utils.pSpec.slipProbability[terrainIndex][carIndex];
        switch (currentState.tyrePressure) {
        case "75":
            slipProbability *= 2;
            break;
        case "100":
            slipProbability *= 3;
            break;
        }
        float randNumber = rand.nextFloat();
        if (randNumber < slipProbability) {
            return 10;
        }

        randNumber = rand.nextFloat();
        float accumulativeProbability = 0;
        float[] moveProbability = moveProbabilities.get(comboString);
        for (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) {
            accumulativeProbability += moveProbability[i];
            if (randNumber <= accumulativeProbability) {
                return i;
            }
        }
        return 11;

    }

}