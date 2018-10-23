package problem;

public class ActionDetail {
    public Action action;
    public String stringValue;

    public ActionDetail(Action a, String str) {
        this.action = a;
        this.stringValue = str;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ActionDetail)) {
            return false;
        }
        ActionDetail other = (ActionDetail) obj;
        if (action == other.action && stringValue == null) {
            return true;
        } else if (action == other.action && stringValue.equals(other.stringValue)) {
            return true;
        }
        return false;
    }

}
