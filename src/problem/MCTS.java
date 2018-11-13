package problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class MCTS {

    Sampler sampler;

    public MCTS(Sampler sampler) {
        this.sampler = sampler;
    }

    public List<ActionType> getFreeActions(Node node, Action action) {
        if (node.freeActions != null && node.freeActions.contains(action.getActionType())) {
            return null;
        } else {
            return Utils.getFreeActions(action.getActionType());
        }
    }

    public Action search(Node root, int maxTime) {
        long startTime = System.currentTimeMillis();
        // init tree

        while ((System.currentTimeMillis() - startTime) < maxTime * 1000) {
            Node leafNode = findLeaf(root);
            float reward = simulate(leafNode.state);
            backPropagate(leafNode, reward);
        }
        System.out.println(root.count);
        return bestAction(root);

    }

    // find a leaf node by traversing the tree using UCT
    private Node findLeaf(Node rootNode) {
        State currentState = rootNode.state;
        Node currentNode = rootNode;

        while (!isTerminalState(currentState, false)) {
            Action action = selectAction(currentNode);
            if (action.getActionType() == ActionType.MOVE) {
                if (currentNode.children[0] == null) {
                    Node andNode = new Node(currentNode, action, currentState, null, true);
                    currentNode.children[0] = andNode;
                }
                currentNode = currentNode.children[0];

                int k = sampler.sampleMove(currentState);

                if (currentNode.children[k] != null) {
                    currentNode = currentNode.children[k];
                    currentState = currentNode.state;
                    continue;
                } else {
                    State newState = moveTransition(currentState, k);
                    Node newNode = new Node(currentNode, action, newState, null);
                    currentNode.children[k] = newNode;
                    return newNode;
                }
            } else {
                int childIndex = currentNode.getIndexFromAction(action);
                if (currentNode.children[childIndex] != null) {
                    currentNode = currentNode.children[childIndex];
                    currentState = currentNode.state;
                    continue;
                } else {
                    State newState = nonMoveTransition(currentState, action);
                    if (currentNode.freeActions != null && currentNode.freeActions.contains(action.getActionType())) {
                        newState.timeStep -= 1;
                    }
                    Node newNode;
                    if (currentNode.freeActions != null && currentNode.freeActions.contains(action.getActionType())) {
                        newNode = new Node(currentNode, action, newState, null);

                    } else {
                        newNode = new Node(currentNode, action, newState, Utils.getFreeActions(action.getActionType()));
                    }
                    currentNode.children[childIndex] = newNode;
                    return newNode;
                }
            }
        }
        return currentNode;
    }

    private Action selectAction(Node n) {

        int i = 0;
        while (i < n.children.length && n.children[i] != null) {
            i++;
        }

        if (i >= n.children.length) {
            Node bestNode = null;
            float maxv = Float.MIN_VALUE; // -inf
            for (int j = 0; j < n.children.length; j++) {
                Node child = n.children[j];
                int childCount = child.count;

                float f = (float) (Math.sqrt((2 * Math.log(n.count)) / (childCount)));
                float currentV = (child.reward / childCount) + f;

                if (currentV > maxv) {
                    maxv = currentV;
                    bestNode = child;
                }
            }
            return bestNode.action;
        } else {
            Action action = n.initActionFromIndex(i);
            return action;
        }
    }

    private State moveTransition(State state, int k) {
        int fuelUsage = 0;
        if (Utils.pSpec.getLevel().getLevelNumber() != 1) {
            fuelUsage = getFuelUsage(state);
        }
        if (state.fuel < fuelUsage) {
            return state;
        }
        if (k < 10) {
            int newCellIndex = state.cellIndex + k - 4;
            if (newCellIndex < 0)
                newCellIndex = 0;
            if (newCellIndex > Utils.pSpec.getN() - 1)
                newCellIndex = Utils.pSpec.getN() - 1;
            return new State(state.car, state.driver, state.tireModel, state.tirePressure, state.fuel - fuelUsage,
                    newCellIndex, state.timeStep + 1);
        } else if (k == 10) {
            return new State(state.car, state.driver, state.tireModel, state.tirePressure, state.fuel - fuelUsage,
                    state.cellIndex, state.timeStep + Utils.pSpec.getSlipRecoveryTime());
        } else if (k == 11) {
            return new State(state.car, state.driver, state.tireModel, state.tirePressure, state.fuel - fuelUsage,
                    state.cellIndex, state.timeStep + Utils.pSpec.getRepairTime());
        } else {
            System.out.println("how did we end up here? 2");
            System.exit(1);
            return null;
        }
    }

    public State nonMoveTransition(State state, Action action) {
        switch (action.getActionType()) {
        case MOVE:
            System.out.println("how did we end up here? 5");
            System.exit(1);
            break;
        case CHANGE_CAR:
            return new State(action.getCarType(), state.driver, state.tireModel, TirePressure.ONE_HUNDRED_PERCENT,
                    ProblemSpec.FUEL_MAX, state.cellIndex, state.timeStep + 1);
        case CHANGE_DRIVER:
            return new State(state.car, action.getDriverType(), state.tireModel, state.tirePressure, state.fuel,
                    state.cellIndex, state.timeStep + 1);
        case CHANGE_TIRES:
            return new State(state.car, state.driver, action.getTireModel(), TirePressure.ONE_HUNDRED_PERCENT,
                    state.fuel, state.cellIndex, state.timeStep + 1);
        case ADD_FUEL:
            int fuel = state.fuel + 10;
            if (fuel > ProblemSpec.FUEL_MAX)
                fuel = ProblemSpec.FUEL_MAX;
            return new State(state.car, state.driver, state.tireModel, state.tirePressure, fuel, state.cellIndex,
                    state.timeStep + 1);
        case CHANGE_PRESSURE:
            return new State(state.car, state.driver, state.tireModel, action.getTirePressure(), state.fuel,
                    state.cellIndex, state.timeStep + 1);
        default:
            System.out.println("how did we get here 3?");
            break;
        }

        return state;
    }

    private float simulate(State state) {
        Action moveAction = new Action(ActionType.MOVE);
        while (!isTerminalState(state, true)) {
            int k = sampler.sampleMove(state);
            state = moveTransition(state, k);
        }
        return getReward(state);
    }

    private void backPropagate(Node node, float reward) {
        while (node != null) {
            node.reward += reward;
            node.count += 1;
            node = node.parent;
        }
    }

    private Action bestAction(Node rootNode) {
        float bestValue = Float.MIN_VALUE;
        Action bestAction = null;
        List<Node> children = new ArrayList<>(Arrays.asList(rootNode.children));
        Collections.sort(children, (a, b) -> {
            float res = (b.reward / b.count) - (a.reward / a.count);
            if (res > 0)
                return 1;
            else if (res < 0)
                return -1;
            else
                return 0;
        });

        for (int i = 0; i < 3; i++) {
            Node n = children.get(i);
            System.out.println(n.action.getText() + ", " + n.count + ", " + n.reward / n.count);
        }
        bestAction = children.get(0).action;
        if (bestAction == null) {
            System.err.println("no best action?");
        }
        return bestAction;
    }

    private boolean isTerminalState(State state, boolean isSimulation) {

        if (state.cellIndex == Utils.pSpec.getN() - 1) {
            return true;
        }
        if (state.timeStep >= Utils.pSpec.getMaxT()) {
            return true;
        }

        if (isSimulation && state.fuel < getFuelUsage(state)) {
            return true;
        }

        return false;
    }

    private float getReward(State state) {
        if (state.cellIndex == Utils.pSpec.getN() - 1) {
            return 1;
        } else {
            return (state.cellIndex * 0.5f) / (Utils.pSpec.getN() - 1);
        }

    }

    private int getFuelUsage(State state) {
        int carIndex = Utils.pSpec.getCarIndex(state.car);
        int terrainIndex = Utils.pSpec.getTerrainIndex(Utils.pSpec.getEnvironmentMap()[state.cellIndex]);
        int fuelUsage = Utils.pSpec.getFuelUsage()[terrainIndex][carIndex];
        switch (state.tirePressure) {
        case SEVENTY_FIVE_PERCENT:
            fuelUsage *= 2;
            break;
        case FIFTY_PERCENT:
            fuelUsage *= 3;
            break;
        }
        return fuelUsage;
    }

}
