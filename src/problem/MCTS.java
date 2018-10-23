package problem;

import java.util.ArrayList;

class MCTS {

    Sampler sampler;

    public Action search(State initState, int maxTime) {
        sampler = new Sampler();
        long startTime = System.currentTimeMillis();
        // init tree
        Node root = new Node(null, null, initState);

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

        while (!isTerminalState(currentState)) {
            ActionDetail actionDetail = selectAction(currentNode);
            if (actionDetail.action == Action.CONTINUE_MOVING) {
                boolean hasChildWithMove = false;
                for (Node n : currentNode.children) {
                    if (n.actionDetail.action == Action.CONTINUE_MOVING) {
                        currentNode = n;
                        hasChildWithMove = true;
                        break;
                    }
                }
                if (!hasChildWithMove) {
                    Node andNode = new Node(currentNode, actionDetail, currentState);
                    rootNode.children.add(andNode);
                    currentNode = andNode;
                }
            }
            State newState = transition(currentState, actionDetail);
            boolean hasChildWithState = false;
            for (Node n : currentNode.children) {
                if (n.state.equals(newState)) {
                    currentNode = n;
                    currentState = n.state;
                    hasChildWithState = true;
                    break;
                }
            }
            if (hasChildWithState) {
                continue;
            } else {
                Node newNode = new Node(currentNode, actionDetail, newState);
                currentNode.children.add(newNode);
                currentNode = newNode;
                break;
            }
        }
        return currentNode;
    }

    private ActionDetail selectAction(Node n) {
        float maxv = Float.MIN_VALUE; // -inf
        int numChildren = n.numChildren();
        ArrayList<Integer> j = new ArrayList<>();
        for (int i = 0; i < numChildren; i++) {
            Node child = n.getChild(i);
            int childCount = child.count;
            float currentV = 0;

            if (childCount == 0) {
                currentV = Float.MAX_VALUE; // +inf
            } else {
                float f = (float)(2 * Math.sqrt((2 * Math.log(n.count)) / (childCount)));
                currentV = child.reward + f;
            }
            if (currentV > maxv) {
                maxv = currentV;
                j = new ArrayList<>();
                j.add(i);
            }
            else if (currentV == maxv) {
              j.add(i);
            }
        }
        int returnIndex;
        if (j.size() > 1) {
            // randomly pick an action
            returnIndex = (int) (Math.random() * (j.size() - 1));
        } else {
            returnIndex = j.get(0);
        }
        return n.getChild(returnIndex).actionDetail;
    }

    private State transition(State state, ActionDetail actionDetail) {
        switch (actionDetail.action) {
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
            return new State(actionDetail.stringValue, state.driver, state.tyreType, state.tyrePressure,
                    ProblemSpec.FUEL_MAX, state.cellIndex, state.timeStep + 1);
        case CHANGE_DRIVER:
            return new State(state.car, actionDetail.stringValue, state.tyreType, state.tyrePressure, state.fuel,
                    state.cellIndex, state.timeStep + 1);
        case CHANGE_TYRES:
            return new State(state.car, state.driver, actionDetail.stringValue, state.tyrePressure, state.fuel,
                    state.cellIndex, state.timeStep + 1);
        case ADD_FUEL:
            int fuel = state.fuel + 10;
            if (fuel > ProblemSpec.FUEL_MAX)
                fuel = ProblemSpec.FUEL_MAX;
            return new State(state.car, state.driver, state.tyreType, state.tyrePressure, fuel, state.cellIndex,
                    state.timeStep + 1);
        case CHANGE_PRESSURE:
            return new State(state.car, state.driver, state.tyreType, actionDetail.stringValue, state.fuel,
                    state.cellIndex, state.timeStep + 1);
        default:
            System.out.println("how did we get here 3?");
            break;
        }

        return state;
    }

    private float simulate(State state) {
        ActionDetail moveAction = new ActionDetail(Action.CONTINUE_MOVING, "");
        while (!isTerminalState(state)) {
            state = transition(state, moveAction);
        }
        return getReward(state);
    }

    private void backPropagate(Node node, float reward) {
        while (node.parent != null) {
            node.reward += reward;
            node.count += 1;
            node = node.parent;
        }
    }

    private ActionDetail bestAction(Node rootNode) {
        float bestValue = 0;
        ActionDetail bestAction;
        for (Node n : rootNode.children) {
            float childValue = n.reward / n.count;
            if (childValue > bestValue) {
                bestAction = childValue;
                bestAction = n.actionDetail;
            }
        }
        return bestAction;
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
