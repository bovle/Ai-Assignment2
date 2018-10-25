package problem;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public Node parent;
    public ArrayList<Node> children;
    public State state;
    public float reward;
    public int count;
    public ActionDetail actionDetail;
    public List<Action> freeActions;

    public Node(Node parent, ActionDetail actionDetial, State s, List<Action> freeActions) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.state = s;
        this.reward = 0;
        this.count = 0;
        this.actionDetail = actionDetial;
        this.freeActions = freeActions;
    }

    public int numChildren() {
        return this.children.size();
    }

    public Node getChild(int i) {
        if (i <= this.numChildren())
            return children.get(i);
        else
            return null;
    }
}
