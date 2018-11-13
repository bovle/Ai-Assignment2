package problem;

import java.util.ArrayList;
import java.util.List;

class Utils {
    public static ProblemSpec pSpec;

    public static List<ActionType> getFreeActions(ActionType action) {
        if (Utils.pSpec.getLevel().getLevelNumber() <= 3) {
            return null;
        }
        List<ActionType> result = new ArrayList<>();
        switch (action) {
        case CHANGE_DRIVER:
            result.add(ActionType.CHANGE_CAR);
            break;
        case CHANGE_CAR:
            result.add(ActionType.CHANGE_DRIVER);
            break;
        case CHANGE_TIRES:
            if (Utils.pSpec.getLevel().getLevelNumber() == 4)
                return null;
            result.add(ActionType.CHANGE_PRESSURE);
            result.add(ActionType.ADD_FUEL);
            break;
        case CHANGE_PRESSURE:
            if (Utils.pSpec.getLevel().getLevelNumber() == 4)
                return null;
            result.add(ActionType.CHANGE_TIRES);
            result.add(ActionType.ADD_FUEL);
            break;
        case ADD_FUEL:
            if (Utils.pSpec.getLevel().getLevelNumber() == 4)
                return null;
            result.add(ActionType.CHANGE_PRESSURE);
            result.add(ActionType.CHANGE_TIRES);
            break;
        default:
            return null;
        }
        return result;
    }

}