import java.util.List;
import java.util.function.BiFunction;

public class Features {

    /**
     * @param state
     * @return bumpiness of the current state
     */
    public static Integer getBumpiness(TestableState state, TestableState prevState) {
        int bumpiness = 0;
        for (int col = 1; col < TestableState.COLS; col++) {
            bumpiness += Math.abs(state.getTop()[col - 1] - state.getTop()[col]);
        }
        return bumpiness;
    }

    /**
     * @param state
     * @return sum of the height of all columns
     */
    public static Integer getTotalHeight(TestableState state, TestableState prevState) {
        int totalHeight = 0;
        for (int col = 0; col < TestableState.COLS; col++) {
            totalHeight += state.getTop()[col];
        }
        return totalHeight;
    }

    /**
     * @param state
     * @return height of the landing piece
     */
    public static Integer getLandingPieceHeight(TestableState state, TestableState prevState) {
        int landingPieceHeight = 0;
        for (int col = 0; col < TestableState.COLS; col++) {
            if (state.getTop()[col] > 0 && state.getTop()[col] <= TestableState.ROWS) {
                if (state.getField()[state.getTop()[col] - 1][col] == state.getTurnNumber() - 1) {
                    landingPieceHeight = Integer.max(landingPieceHeight, state.getTop()[col]);
                }
            }
        }
        return landingPieceHeight;
    }

    /**
     * @param state
     * @return number of empty blocks where there exists a full block above it
     */
    public static Integer getNumHoles(TestableState state, TestableState prevState) {
        int numHoles = 0;
        for (int col = 0; col < TestableState.COLS; col++){
            for (int row = 0; row < state.getTop()[col] - 1; row++){
                if (state.getField()[row][col] == 0) numHoles++;
            }
        }
        return numHoles;
    }

    /**
     * @param state
     * @return number of blocks above holes
     */
    public static Integer getBlocksAboveHoles(TestableState state, TestableState prevState) {
        int numBlocks = 0;
        for (int col = 0; col < TestableState.COLS; col++){
            boolean foundHole = false;
            for (int row = 0; row < state.getTop()[col]; row++){
                if (state.getField()[row][col] == 0) foundHole = true;
                else if (foundHole) numBlocks++;
            }
        }
        return numBlocks;
    }

    /**
     * Penalizes more for taller holes and significant top difference
     * For each hole, cost = height of hole
     * For each dip, cost = smaller of the height difference
     *
     * @param state
     * @return
     */
    public static Integer getSignificantHoleAndTopDifference(TestableState state, TestableState prevState) {
        int sum = 0;
        for (int i = 0; i < TestableState.COLS; i++) {
            int consec = 0;
            for (int j = 0; j < state.getTop()[i]; j++) {
                if (state.getField()[j][i] == 0) consec++;
                else sum += consec;
            }
            if ((i == 0 || state.getTop()[i - 1] >= state.getTop()[i] + 3) && (i == TestableState.COLS - 1 || state.getTop()[i + 1] >= state.getTop()[i] + 3)) {
                if (i == TestableState.COLS - 1) sum += (state.getTop()[i - 1] - state.getTop()[i]);
                else if (i == 0) sum += (state.getTop()[i + 1] - state.getTop()[i]);
                else sum += Math.min(state.getTop()[i - 1] - state.getTop()[i], state.getTop()[i + 1] - state.getTop()[i]);
            }

        }
        return sum;
    }

    /**
     * @param state
     * @return rows cleared from last move
     */
    public static Integer getRowsCleared(TestableState state, TestableState prevState) {
        return state.getRowsCleared() - prevState.getRowsCleared();
    }

    /**
     * Based on a KTH paper - Tetris: A Heuristic Study
     * Using height-based weighing functions and breadth-first search
     * heuristics for playing Tetris
     *
     * @param state
     * @return sum of (row height)^2 of empty cells with right wall
     */
    public static Integer getSpaceWithRightWallMeasure(TestableState state, TestableState prevState) {
        int sum = 0;
        for (int col = 0; col < TestableState.COLS - 1; col++){
            for(int row = 0; row < state.getTop()[col + 1]; row++){
                if (state.getField()[row][col] == 0) sum += row * row;
            }
        }
        return sum;
    }

    /**
     * Based on a KTH paper
     *
     * @param state
     * @return sum of (row height)^2 of empty cells with left wall
     */
    public static Integer getSpaceWithLeftWallMeasure(TestableState state, TestableState prevState) {
        int sum = 0;
        for (int col = 1; col < TestableState.COLS; col++) {
            for (int row = 0; row < state.getTop()[col - 1]; row++) {
                if (state.getField()[row][col] == 0) sum += row * row;
            }
        }
        return sum;
    }

    /**
     * Based on a KTH paper
     *
     * @param state
     * @return sum of (row height)^3 of holes
     */
    public static Integer getHoleMeasure(TestableState state, TestableState prevState) {
        int sum = 0;
        for (int col = 0; col < TestableState.COLS; col++) {
            for (int row = 0; row < state.getTop()[col] - 1; row++) {
                if (state.getField()[row][col] == 0) sum += row * row * row;
            }
        }
        return sum;
    }

    /**
     * Based on a KTH paper
     *
     * @param state
     * @return sum of hole and wall measures
     */
    public static Integer getAggregateHoleAndWallMeasure(TestableState state, TestableState prevState) {
        return getSpaceWithLeftWallMeasure(state, prevState) + 
            getSpaceWithRightWallMeasure(state, prevState) +
            getHoleMeasure(state, prevState);
    }

    public static void addAllFeatures(List<BiFunction<TestableState, TestableState, Integer>> features) {
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getLandingPieceHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifference);
        features.add(Features::getRowsCleared);
        features.add(Features::getAggregateHoleAndWallMeasure);
    }

}
