package problem;

import java.util.ArrayList;

public class Node {
    public Node parent;
    public ArrayList<Node> children;
    public State state;
    public float reward;
    public int count;
    public ActionDetail actionDetail;

    public Node(Node parent, ActionDetail actionDetial, State s) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.state = s;
        this.reward = 0;
        this.count = 0;
        this.actionDetail = actionDetial;
    }

    public int numChildren() {
        return this.children.length();
    }

    public Node getChild(int i) {
        if (i <= numChildren)
            return children.get(i);
        else
            return null;
    }
}
