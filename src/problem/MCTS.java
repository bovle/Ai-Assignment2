package problem;

class MCTS {

    Sampler sampler;

    public Action Search(State initState, int maxTime) {
        sampler = new Sampler();
        long startTime = System.currentTimeMillis();
        // init tree

        while ((System.currentTimeMillis() - startTime) < maxTime * 1000) {
            findLeaf();
        }

        return Action.CONTINUE_MOVING;

    }

    // find a leaf node by traversing the tree using UCT
    private void findLeaf() {

    }

    private ActionDetail selectAction(Node n) {
      float maxv = Float.MIN_VALUE; // -inf
      int numChildren = n.numChildren();
      ArrayList<int> j = new ArrayList<>();
      for (int i=0; i<numChildren; i++) {
        Node child = getChild(i);
        int childCount = child.count;
        float currentV = 0;

        if (childCount == 0) {
          v = Float.MAX_VALUE; // +inf
        } else {
          currentV = child.reward + 2*Math.sqrt((2*Math.ln(n.count))/(childCount))
        }
        if (currentV >= maxv) {
          maxv = currentV;
          j.add(i);
        }
      }
      int returnIndex;
      if (j.length() > 1) {
        // randomly pick an action
        int returnIndex = (int)(Math.random() * (j.length()-1));
      }
      else {
        returnIndex = j.get(0);
      }
      return n.getChild(returnIndex).actionDetail;
    }

    private State transition(State state, Action action, String stringValue) {
        switch (action) {
        case CONTINUE_MOVING:
            int k = sampler.SampleMove(state);
            int fuelUsage = getFuelUsage(state);
            if (k < 10) {
                return new State(state.car, state.driver, state.tyreType, state.tyrePressure, state.fuel - fuelUsage,
                        state.cellIndex + k - 4, state.timeStep + 1);
            } else if (k == 10) {
                return new State(state.car, state.driver, state.tyreType, state.tyrePressure, state.fuel - fuelUsage,
                        state.cellIndex, state.timeStep + Utils.pSpec.slipRecoveryTime);
            } else if (k == 11) {
                return new State(state.car, state.driver, state.tyreType, state.tyrePressure, state.fuel - fuelUsage,
                        state.cellIndex, state.timeStep + Utils.pSpec.repairTime);
            } else {
                System.out.println("how did we end up here? 2");
                System.exit(1);
            }
            break;
        case CHANGE_CAR:
            return new State(stringValue, state.driver, state.tyreType, state.tyrePressure, ProblemSpec.FUEL_MAX,
                    state.cellIndex, state.timeStep + 1);
        case CHANGE_DRIVER:
            return new State(state.car, stringValue, state.tyreType, state.tyrePressure, state.fuel, state.cellIndex,
                    state.timeStep + 1);
        case CHANGE_TYRES:
            return new State(state.car, state.driver, stringValue, state.tyrePressure, state.fuel, state.cellIndex,
                    state.timeStep + 1);
        case ADD_FUEL:
            int fuel = state.fuel + 10;
            if (fuel > ProblemSpec.FUEL_MAX)
                fuel = ProblemSpec.FUEL_MAX;
            return new State(state.car, state.driver, state.tyreType, state.tyrePressure, fuel, state.cellIndex,
                    state.timeStep + 1);
        case CHANGE_PRESSURE:
            return new State(state.car, state.driver, state.tyreType, stringValue, state.fuel, state.cellIndex,
                    state.timeStep + 1);
        default:
            break;
        }

        return state;
    }

    private boolean isTerminalState(State state) {

        if (state.cellIndex == Utils.pSpec.N) {
            return true;
        }
        if (state.timeStep >= Utils.pSpec.maxT) {
            return true;
        }

        if (state.fuel < getFuelUsage(state)) {
            return true;
        }

        return false;
    }

    private float getReward(State state) {
        return Utils.pSpec.N / state.cellIndex;
    }

    private int getFuelUsage(State state) {
        int carIndex = Utils.pSpec.carToIndex.get(state.car);
        int terrainIndex = Utils.pSpec.terrainToIndex.get(Utils.pSpec.environmentMap[state.cellIndex]);
        int fuelUsage = Utils.pSpec.fuelUsage[terrainIndex][carIndex];
        switch (state.tyrePressure) {
        case "75":
            fuelUsage *= 2;
            break;
        case "50":
            fuelUsage *= 3;
            break;
        }
        return fuelUsage;
    }

}
