package problem;

import java.util.ArrayList;

public class Node {
    public Node parent;
    public ArrayList<Node> children;
    public State state;
    public float reward;
    public int count;

    public Node(Node parent, State s) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.state = s;
        this.reward = 0;
        this.count = 0;

    }
}