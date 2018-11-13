package problem;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Sampler {

    Map<String, double[]> moveProbabilities;
    Random rand;
    State currentState;

    public Sampler() {
        moveProbabilities = new HashMap<>();
        rand = new Random();
    }

    public int sampleMove(State currentState) {
        this.currentState = currentState;

        double[] moveProbs = getMoveProbs();

        double p = Math.random();
        double pSum = 0;
        int move = 0;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            pSum += moveProbs[k];
            if (p <= pSum) {
                move = k;
                break;
            }
        }
        return move;
    }

    private double[] getMoveProbs() {

        // get parameters of current state
        Terrain terrain = Utils.pSpec.getEnvironmentMap()[currentState.cellIndex];
        int terrainIndex = Utils.pSpec.getTerrainIndex(terrain);
        String car = currentState.car;
        String driver = currentState.driver;
        Tire tire = currentState.tireModel;

        // calculate priors
        double priorK = 1.0 / ProblemSpec.CAR_MOVE_RANGE;
        double priorCar = 1.0 / Utils.pSpec.getCT();
        double priorDriver = 1.0 / Utils.pSpec.getDT();
        double priorTire = 1.0 / ProblemSpec.NUM_TYRE_MODELS;
        double priorTerrain = 1.0 / Utils.pSpec.getNT();
        double priorPressure = 1.0 / ProblemSpec.TIRE_PRESSURE_LEVELS;

        // get probabilities of k given parameter
        double[] pKGivenCar = Utils.pSpec.getCarMoveProbability().get(car);
        double[] pKGivenDriver = Utils.pSpec.getDriverMoveProbability().get(driver);
        double[] pKGivenTire = Utils.pSpec.getTireModelMoveProbability().get(tire);
        double pSlipGivenTerrain = Utils.pSpec.getSlipProbability()[terrainIndex];
        double[] pKGivenPressureTerrain = convertSlipProbs(pSlipGivenTerrain);

        // use bayes rule to get probability of parameter given k
        double[] pCarGivenK = bayesRule(pKGivenCar, priorCar, priorK);
        double[] pDriverGivenK = bayesRule(pKGivenDriver, priorDriver, priorK);
        double[] pTireGivenK = bayesRule(pKGivenTire, priorTire, priorK);
        double[] pPressureTerrainGivenK = bayesRule(pKGivenPressureTerrain, (priorTerrain * priorPressure), priorK);

        // use conditional probability formula on assignment sheet to get what
        // we want (but what is it that we want....)
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double kProbsSum = 0;
        double kProb;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProb = magicFormula(pCarGivenK[k], pDriverGivenK[k], pTireGivenK[k], pPressureTerrainGivenK[k], priorK);
            kProbsSum += kProb;
            kProbs[k] = kProb;
        }

        // Normalize
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProbs[k] /= kProbsSum;
        }

        return kProbs;
    }

    private double[] convertSlipProbs(double slipProb) {

        // Adjust slip probability based on tire pressure
        TirePressure pressure = currentState.tirePressure;
        if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            slipProb *= 2;
        } else if (pressure == TirePressure.ONE_HUNDRED_PERCENT) {
            slipProb *= 3;
        }
        // Make sure new probability is not above max
        if (slipProb > ProblemSpec.MAX_SLIP_PROBABILITY) {
            slipProb = ProblemSpec.MAX_SLIP_PROBABILITY;
        }

        // for each terrain, all other action probabilities are uniform over
        // remaining probability
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double leftOver = 1 - slipProb;
        double otherProb = leftOver / (ProblemSpec.CAR_MOVE_RANGE - 1);
        for (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) {
            if (i == Utils.pSpec.getIndexOfMove(ProblemSpec.SLIP)) {
                kProbs[i] = slipProb;
            } else {
                kProbs[i] = otherProb;
            }
        }

        return kProbs;
    }

    private double[] bayesRule(double[] condProb, double priorA, double priorB) {

        double[] swappedProb = new double[condProb.length];

        for (int i = 0; i < condProb.length; i++) {
            swappedProb[i] = (condProb[i] * priorA) / priorB;
        }
        return swappedProb;
    }

    private double magicFormula(double pA, double pB, double pC, double pD, double priorE) {
        return pA * pB * pC * pD * priorE;
    }
}
/*
 * public int SampleMove(State currentState) { double[] carProbabilities =
 * Utils.pSpec.getCarMoveProbability().get(currentState.car); double[]
 * driverProbabilities =
 * Utils.pSpec.getDriverMoveProbability().get(currentState.driver); double[]
 * tyreProbabilities =
 * Utils.pSpec.getTireModelMoveProbability().get(currentState.tireModel);
 * 
 * String comboString = currentState.car + currentState.driver +
 * currentState.tireModel;
 * 
 * if (!moveProbabilities.containsKey(comboString)) {
 * 
 * // SUM_OVER_ALL_K(P(K|D) * P(K|C) * P(K|T)) double probabilitySum = 0; for
 * (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) { probabilitySum +=
 * driverProbabilities[i] * carProbabilities[i] * tyreProbabilities[i]; }
 * 
 * // P(K_i|D) * P(K_i|C) * P(K_i|T) / SUM_OVER_ALL_K(P(K|D) * P(K|C) * P(K|T))
 * double[] resultProbability = new double[ProblemSpec.CAR_MOVE_RANGE]; for (int
 * i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) { resultProbability[i] =
 * (driverProbabilities[i] * carProbabilities[i] * tyreProbabilities[i]) /
 * probabilitySum; }
 * 
 * moveProbabilities.put(comboString, resultProbability); }
 * 
 * int terrainIndex =
 * Utils.pSpec.getTerrainIndex(Utils.pSpec.getEnvironmentMap()[currentState.
 * cellIndex]); double slipProbability =
 * Utils.pSpec.getSlipProbability()[terrainIndex]; switch
 * (currentState.tirePressure) { case SEVENTY_FIVE_PERCENT: slipProbability *=
 * 2; break; case ONE_HUNDRED_PERCENT: slipProbability *= 3; break; } double
 * randNumber = rand.nextDouble(); if (randNumber < slipProbability) { return
 * 10; }
 * 
 * randNumber = rand.nextDouble(); double accumulativeProbability = 0; double[]
 * moveProbability = moveProbabilities.get(comboString); for (int i = 0; i <
 * ProblemSpec.CAR_MOVE_RANGE; i++) { accumulativeProbability +=
 * moveProbability[i]; if (randNumber <= accumulativeProbability) { return i; }
 * } return 11;
 * 
 * }
 */