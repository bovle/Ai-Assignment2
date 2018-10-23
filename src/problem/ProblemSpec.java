package problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class represents the problem detailed in the assignment spec. It
 * contains functionality to parse the input file and load it into class
 * variable (see comments for each variable for more info).
 */
public class ProblemSpec {

    /** min and max values for car fuel level **/
    public static final int FUEL_MIN = 0;
    public static final int FUEL_MAX = 50;
    /** car move range [-4, 5] **/
    public static final int CAR_MIN_MOVE = -4;
    public static final int CAR_MAX_MOVE = 5;
    public static final int CAR_MOVE_RANGE = 12;
    /** number of different tyres **/
    public static final int NUM_TYRE_MODELS = 4;

    /** For rounding probabilities to 4 decimal places **/
    private DecimalFormat df = new DecimalFormat("#.####");

    /** The level of the game **/
    Level level;
    /** Discount factor **/
    float discountFactor;
    /** Time to recover from a slip **/
    int slipRecoveryTime;
    /** Breakdown repair time **/
    int repairTime;
    /** The number of cells in map **/
    int N;
    /** The maximum number of time-steps allowed for reaching goal **/
    int maxT;
    /** Number of terrain types **/
    int NT;
    /* Terrain to index mapping */
    Map<Terrain, Integer> terrainToIndex;
    /** The environment map as a 1D array of terrains in order **/
    Terrain[] environmentMap;
    /**
     * The terrain map which maps terrains to their cell indices on the environment
     * map
     */
    LinkedHashMap<Terrain, List<Integer>> terrainMap;
    /** Number of car types **/
    int CT;
    /** Car probability mapping **/
    Map<String, float[]> carMoveProbability;
    /* Car to index mapping */
    Map<String, Integer> carToIndex;
    /** Number of drivers **/
    int DT;
    /** Driver to probability mapping **/
    Map<String, float[]> driverMoveProbability;
    /** Tyre model to probability mapping **/
    Map<String, float[]> tyreModelMoveProbability;
    /**
     * Fuel usage matrix Size is NT rows * CT columns Each row, i, represents the
     * ith terrain type Each column, j, represents the jth Car type
     */
    int[][] fuelUsage;
    /**
     * Slip probability matrix Size s NT rows * CT columns Each row, i, represents
     * the ith terrain type Each column, j, represents the jth Car type
     */
    float[][] slipProbability;

    List<ActionDetail> actionDetails;

    public ProblemSpec(String fileName) throws IOException {
        loadProblem(fileName);
        actionDetails = new ArrayList<>();
        for (Action action : level.getAvailableActions()) {
            switch (action) {
            case CONTINUE_MOVING: // A1
                actionDetails.add(new ActionDetail(action, null));
                break;
            case CHANGE_CAR: // A2
                for (String car : this.carMoveProbability.keySet()) {
                    actionDetails.add(new ActionDetail(action, car));
                }
                break;
            case CHANGE_DRIVER: // A3
                for (String driver : this.driverMoveProbability.keySet()) {
                    actionDetails.add(new ActionDetail(action, driver));
                }
                break;
            case CHANGE_TYRES: // A4
                for (String tyreType : this.tyreModelMoveProbability.keySet()) {
                    actionDetails.add(new ActionDetail(action, tyreType));
                }
                break;
            case ADD_FUEL: // A5
                actionDetails.add(new ActionDetail(action, null));
                break;
            case CHANGE_PRESSURE: // A6
                actionDetails.add(new ActionDetail(action, "50"));
                actionDetails.add(new ActionDetail(action, "75"));
                actionDetails.add(new ActionDetail(action, "100"));
                break;
            default:
                break;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 1.
        sb.append("level: " + level.getLevelNumber() + "\n");
        // 2.
        sb.append("discount: " + discountFactor + "\n");
        sb.append("recoverTime: " + slipRecoveryTime + "\n");
        sb.append("repairTime: " + repairTime + "\n");
        // 3.
        sb.append("N: " + N + "\n");
        sb.append("maxT: " + maxT + "\n");
        // 4.
        sb.append(Arrays.toString(environmentMap)).append("\n");

        return sb.toString();
    }

    /**
     * Loads a problem from a problem text file.
     *
     * @param fileName the path of the text file to load.
     * @throws IOException if the text file doesn't exist or doesn't meet the
     *                     assignment specifications.
     */
    private void loadProblem(String fileName) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(fileName));
        String line;
        String[] splitLine;
        int lineNo = 0;
        Scanner s;
        try {
            // 1. line 1
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            int levelNumber = s.nextInt();
            level = new Level(levelNumber);
            s.close();

            // 2. line 2
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            discountFactor = s.nextFloat();
            slipRecoveryTime = s.nextInt();
            repairTime = s.nextInt();
            s.close();

            // 3. line 3
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            N = s.nextInt();
            maxT = s.nextInt();
            s.close();

            // 4. line 4 to (3+NT)
            int NT = level.get_NT();
            terrainToIndex = new HashMap<>();
            environmentMap = new Terrain[N];
            terrainMap = new LinkedHashMap<>();
            for (int i = 0; i < NT; i++) {
                line = input.readLine();
                lineNo++;
                splitLine = line.split(":");
                // first part is name of terrain
                Terrain terrain = parseTerrain(splitLine[0], lineNo);
                terrainToIndex.put(terrain, i);
                List<Integer> terrainIndices = parseTerrainCellIndices(splitLine[1], lineNo);
                terrainMap.put(terrain, terrainIndices);
                for (Integer j : terrainIndices) {
                    environmentMap[j - 1] = terrain;
                }
            }

            // 5. line (3+NT+1)
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            CT = s.nextInt();
            s.close();

            // 6. line (3+NT+2) to (3+NT+2+CT)
            carToIndex = new HashMap<>();
            carMoveProbability = new LinkedHashMap<>();
            for (int i = 0; i < CT; i++) {
                line = input.readLine();
                lineNo++;
                carToIndex.put(line.split(":")[0], i);
                parseProbLine(line, carMoveProbability);
            }

            // 7. Number of drivers line
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            DT = s.nextInt();
            s.close();

            // 8. Driver move probabilities
            driverMoveProbability = new LinkedHashMap<>();
            for (int i = 0; i < DT; i++) {
                line = input.readLine();
                lineNo++;
                parseProbLine(line, driverMoveProbability);
            }

            // 9. Tyre model move probabilities
            tyreModelMoveProbability = new HashMap<>();
            for (int i = 0; i < NUM_TYRE_MODELS; i++) {
                line = input.readLine();
                lineNo++;
                parseProbLine(line, tyreModelMoveProbability);
            }

            // 10. Fuel usage by terrain and car matrix
            fuelUsage = new int[NT][CT];
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            for (int i = 0; i < NT; i++) {
                for (int j = 0; j < CT; j++) {
                    fuelUsage[i][j] = s.nextInt();
                }
            }
            s.close();

            // 11. Slip probability by terrain and car matrix
            slipProbability = new float[NT][CT];
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            float rowSum;
            for (int i = 0; i < NT; i++) {
                rowSum = 0;
                for (int j = 0; j < CT; j++) {
                    slipProbability[i][j] = s.nextFloat();
                    rowSum += slipProbability[i][j];
                }
                if (Math.abs(rowSum - 1.0) > 0.001) {
                    throw new InputMismatchException("Slip probability for does not sum to one for row " + i);
                }
            }
            s.close();

        } catch (InputMismatchException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (NoSuchElementException e) {
            System.out.println("Not enough tokens on input file - line " + lineNo);
            System.exit(2);
        } catch (NullPointerException e) {
            System.out.format("Input file - line %d expected, but file ended.", lineNo);
            System.exit(3);
        } finally {
            input.close();
        }
    }

    /**
     * Parse a line of the below form and add entry to map:
     *
     * thingName : p0 p1 p2 ... p11
     *
     * where pi represents probability of ith possible car move distance, starting
     * at -4 upto 5, then slip and breakdown.
     *
     * @param line    the line text
     * @param probMap map the add entry to
     */
    private void parseProbLine(String line, Map<String, float[]> probMap) {
        String[] splitLine = line.split(":");
        String thingName = splitLine[0];
        Scanner s = new Scanner(splitLine[1]);
        float[] probabilities = new float[CAR_MOVE_RANGE];
        float pSum = 0;
        for (int j = 0; j < CAR_MOVE_RANGE; j++) {
            probabilities[j] = s.nextFloat();
            pSum += probabilities[j];
        }
        probMap.put(thingName, probabilities);
        s.close();

        if (Math.abs(pSum - 1.0) > 0.001) {
            throw new InputMismatchException("Car move probability does not sum to 1.0");
        }
    }

    private List<Integer> parseTerrainCellIndices(String indexText, int lineNo) {

        List<Integer> indices = new ArrayList<>();

        String[] splitText = indexText.split(",");
        String[] splitIndices;
        int start, end;

        for (String s : splitText) {
            splitIndices = s.split("-");

            if (splitIndices.length == 1) {
                indices.add(Integer.parseInt(splitIndices[0]));
            } else if (splitIndices.length == 2) {
                start = Integer.parseInt(splitIndices[0]);
                end = Integer.parseInt(splitIndices[1]);
                for (int i = start; i <= end; i++) {
                    indices.add(i);
                }
            }
            // else empty so no terrain of this type
        }
        return indices;
    }

    private Terrain parseTerrain(String terrainText, int lineNo) {
        switch (terrainText) {
        case "dirt":
            return Terrain.DIRT;
        case "asphalt":
            return Terrain.ASPHALT;
        case "dirt-straight":
            return Terrain.DIRT_STRAIGHT;
        case "dirt-slalom":
            return Terrain.DIRT_SLALOM;
        case "asphalt-straight":
            return Terrain.ASPHALT_STRAIGHT;
        case "asphalt-slalom":
            return Terrain.ASPHALT_SLALOM;
        case "dirt-straight-hilly":
            return Terrain.DIRT_STRAIGHT_HILLY;
        case "dirt-straight-flat":
            return Terrain.DIRT_STRAIGHT_FLAT;
        case "dirt-slalom-hilly":
            return Terrain.DIRT_SLALOM_HILLY;
        case "dirt-slalom-flat":
            return Terrain.DIRT_SLALOM_FLAT;
        case "asphalt-straight-hilly":
            return Terrain.ASPHALT_STRAIGHT_HILLY;
        case "asphalt-straight-flat":
            return Terrain.ASPHALT_STRAIGHT_FLAT;
        case "asphalt-slalom-hilly":
            return Terrain.ASPHALT_SLALOM_HILLY;
        case "asphalt-slalom-flat":
            return Terrain.ASPHALT_SLALOM_FLAT;
        default:
            String errMsg = "Invalid terrain type " + terrainText + "on line " + lineNo;
            throw new InputMismatchException(errMsg);
        }
    }
}
