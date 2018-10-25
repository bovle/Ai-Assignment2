package problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MCTS {

    Sampler sampler;

    public MCTS(Sampler sampler) {
        this.sampler = sampler;
    }

    public Node Simulate(Node node, ActionDetail actionDetail) {

        Node nextNode = null;
        if (actionDetail.action == Action.CONTINUE_MOVING) {
            for (Node n : node.children) {
                if (n.actionDetail.action == Action.CONTINUE_MOVING) {
                    node = n;
                    break;
                }
            }
        }

        State nextState = transition(node.state, actionDetail);

        if (node.freeActions != null && node.freeActions.contains(actionDetail.action)) {
            nextState.timeStep -= 1;
        }

        for (Node n : node.children) {
            if (n.state.equals(nextState)) {
                nextNode = n;
            }
        }

        return new Node(null, null, nextNode.state, nextNode.freeActions);
    }

    public ActionDetail search(Node root, int maxTime) {
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
                    Node andNode = new Node(currentNode, actionDetail, currentState, null);
                    currentNode.children.add(andNode);
                    currentNode = andNode;
                }
            }
            State newState = transition(currentState, actionDetail);
            if (currentNode.freeActions != null && currentNode.freeActions.contains(actionDetail.action)) {
                newState.timeStep -= 1;
            }
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
                Node newNode;
                if (currentNode.freeActions != null && currentNode.freeActions.contains(actionDetail.action)) {
                    if (currentNode.freeActions.size() == 1) {
                        newNode = new Node(currentNode, actionDetail, newState, null);
                    } else {
                        List<Action> freeAs = new ArrayList<>();
                        freeAs.remove(actionDetail.action);
                        newNode = new Node(currentNode, actionDetail, newState, freeAs);
                    }

                } else {
                    newNode = new Node(currentNode, actionDetail, newState, Utils.getFreeActions(actionDetail.action));
                }

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
        List<ActionDetail> unexploredActions = new ArrayList<>();

        for (ActionDetail ad : Utils.pSpec.actionDetails) {
            if (!((ad.action == Action.CHANGE_CAR && ad.stringValue.equals(n.state.car))
                    || (ad.action == Action.CHANGE_DRIVER && ad.stringValue.equals(n.state.driver))
                    || (ad.action == Action.CHANGE_TYRES && ad.stringValue.equals(n.state.tyreType))
                    || (ad.action == Action.CHANGE_PRESSURE && ad.stringValue.equals(n.state.tyrePressure))
                    || (ad.action == Action.CONTINUE_MOVING && n.state.fuel < getFuelUsage(n.state)))) {
                unexploredActions.add(ad);
            }
        }

        if (numChildren < unexploredActions.size()) {
            for (int i = 0; i < numChildren; i++) {
                Node child = n.getChild(i);
                if (unexploredActions.contains(child.actionDetail)) {
                    unexploredActions.remove(child.actionDetail);
                }
            }
            int returnIndex = (int) (Math.random() * (unexploredActions.size() - 1));
            return unexploredActions.get(returnIndex);
        }

        for (int i = 0; i < numChildren; i++) {
            Node child = n.getChild(i);
            int childCount = child.count;
            float currentV = 0;

            float f = (float) (Math.sqrt((2 * Math.log(n.count)) / (childCount)));
            currentV = (child.reward / childCount) + f;

            if (currentV > maxv) {
                maxv = currentV;
                j = new ArrayList<>();
                j.add(i);
            } else if (currentV == maxv) {
                j.add(i);
            }
        }

        int returnIndex;
        if (j.size() > 1) {
            // randomly pick an action
            int randomIndex = (int) (Math.random() * (j.size() - 1));
            returnIndex = j.get(randomIndex);
        } else {
            returnIndex = j.get(0);
        }
        return n.getChild(returnIndex).actionDetail;
    }

    private State transition(State state, ActionDetail actionDetail) {
        switch (actionDetail.action) {
        case CONTINUE_MOVING:
            int k = sampler.SampleMove(state);
            int fuelUsage = 0;
            if (Utils.pSpec.level.getLevelNumber() != 1) {
                fuelUsage = getFuelUsage(state);
            }
            if (k < 10) {
                int newCellIndex = state.cellIndex + k - 4;
                if (newCellIndex < 0)
                    newCellIndex = 0;
                if (newCellIndex > Utils.pSpec.N - 1)
                    newCellIndex = Utils.pSpec.N - 1;
                return new State(state.car, state.driver, state.tyreType, state.tyrePressure, state.fuel - fuelUsage,
                        newCellIndex, state.timeStep + 1);
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
            return new State(actionDetail.stringValue, state.driver, state.tyreType, "100", ProblemSpec.FUEL_MAX,
                    state.cellIndex, state.timeStep + 1);
        case CHANGE_DRIVER:
            return new State(state.car, actionDetail.stringValue, state.tyreType, state.tyrePressure, state.fuel,
                    state.cellIndex, state.timeStep + 1);
        case CHANGE_TYRES:
            return new State(state.car, state.driver, actionDetail.stringValue, "100", state.fuel, state.cellIndex,
                    state.timeStep + 1);
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
        while (!isTerminalState(state, true)) {
            state = transition(state, moveAction);
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

    private ActionDetail bestAction(Node rootNode) {
        float bestValue = Float.MIN_VALUE;
        ActionDetail bestAction = null;
        Collections.sort(rootNode.children, (a, b) -> {
            float res = (b.reward / b.count) - (a.reward / a.count);
            if (res > 0)
                return 1;
            else if (res < 0)
                return -1;
            else
                return 0;
        });

        for (int i = 0; i < 3; i++) {
            Node n = rootNode.children.get(i);
            System.out.println(n.actionDetail.action.toString() + ", " + n.actionDetail.stringValue + ", " + n.count
                    + ", " + n.reward / n.count);
        }
        bestAction = rootNode.children.get(0).actionDetail;
        if (bestAction == null) {
            System.err.println("no best action?");
        }
        return bestAction;
    }

    private boolean isTerminalState(State state, boolean isSimulation) {

        if (state.cellIndex == Utils.pSpec.N - 1) {
            return true;
        }
        if (state.timeStep >= Utils.pSpec.maxT) {
            return true;
        }

        if (isSimulation && state.fuel < getFuelUsage(state)) {
            return true;
        }

        return false;
    }

    private float getReward(State state) {
        if (state.cellIndex == Utils.pSpec.N - 1) {
            return 1;
        } else {
            return (state.cellIndex * 0.5f) / (Utils.pSpec.N - 1);
        }

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
