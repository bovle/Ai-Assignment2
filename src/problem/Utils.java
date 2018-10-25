package problem;

import java.util.ArrayList;
import java.util.List;

class Utils {
    public static ProblemSpec pSpec;

    public static List<Action> getFreeActions(Action action) {
        if (Utils.pSpec.level.getLevelNumber() <= 3) {
            return null;
        }
        List<Action> result = new ArrayList<>();
        switch (action) {
        case CHANGE_DRIVER:
            result.add(Action.CHANGE_CAR);
            break;
        case CHANGE_CAR:
            result.add(Action.CHANGE_DRIVER);
            break;
        case CHANGE_TYRES:
            if (Utils.pSpec.level.getLevelNumber() == 4)
                return null;
            result.add(Action.CHANGE_PRESSURE);
            result.add(Action.ADD_FUEL);
            break;
        case CHANGE_PRESSURE:
            if (Utils.pSpec.level.getLevelNumber() == 4)
                return null;
            result.add(Action.CHANGE_TYRES);
            result.add(Action.ADD_FUEL);
            break;
        case ADD_FUEL:
            if (Utils.pSpec.level.getLevelNumber() == 4)
                return null;
            result.add(Action.CHANGE_PRESSURE);
            result.add(Action.CHANGE_TYRES);
            break;
        default:
            return null;
        }
        return result;
    }

}