import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Features {

    private Features() {}

    public static Double getBumpiness(TestState testState) {
        int bumpiness = 0;

        for (int col = 1; col < State.COLS; col++) {
            bumpiness += Math.abs(testState.top[col - 1] - testState.top[col]);
        }

        return (double) bumpiness;
    }

    public static Double getTotalHeight(TestState testState) {
        int totalHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            totalHeight += testState.top[col];
        }

        return (double) totalHeight;
    }

    public static Double getMaxHeight(TestState testState) {
        int maxHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            maxHeight = Integer.max(maxHeight, testState.top[col]);
        }

        return (double) maxHeight;
    }

    /**
     *
     * @param testState
     * @return number of empty blocks where there exists a full block above it
     */
    public static Double getNumHoles(TestState testState) {
        int numHoles = 0;

        for(int col = 0; col < State.COLS; col++){
            for(int row =0;row< testState.top[col]-1;row++){
                if(testState.field[row][col] == 0) numHoles++;
            }
        }

        return (double) numHoles;
    }

    /**
     *
     * @param testState
     * @return number of blocks above holes
     */
    public static Double getBlocksAboveHoles(TestState testState){
        int numBlocks = 0;

        for(int col = 0; col < State.COLS;col++){
            boolean foundHole = false;
            for(int row = 0; row< testState.top[col];row++){
                if(testState.field[row][col] == 0) foundHole = true;
                else if(foundHole) numBlocks++;
            }
        }
        return (double)numBlocks;
    }

    public static Double getHeightAboveHoles(TestState testState){
        int numBlocks = 0;

        for (int col = 0; col < State.COLS; col++) {
            for (int row = 0; row < testState.top[col]; row++) {
                if (testState.field[row][col] == 0){
                    numBlocks += testState.top[col] - 1 - row;
                    break;
                }
            }
        }
        return (double) numBlocks;
    }

    /**
     * motivation: penalize stacking over holes/ covering tall holes
     * @param testState
     * @return sum of (top - 1 ) - row over all holes
     */
    public static Double getSumOfDepthOfHoles(TestState testState){
        int sum = 0;

        for (int col = 0; col < State.COLS; col++) {
            for (int row = 0; row < testState.top[col]; row++) {
                if (testState.field[row][col] == 0) sum += testState.top[col] - 1 - row;
            }
        }
        return (double) sum;
    }

    /**
     * NOTE: may cause holes since it may incentivize turning significant top diff into hole
     * @param testState
     * @return numbers of columns where all neighbouring columns are both at least 3 blocks taller
     */
    public static Double getNumOfSignificantTopDifference(TestState testState){
        int num = 0;
        for(int i =0;i<State.COLS;i++){
            if((i == 0 || testState.top[i-1] >= testState.top[i]+3) && (i == State.COLS-1 || testState.top[i + 1] >= testState.top[i] + 3))
                num++;
        }
        return (double) num;
    }

    /**
     * penalizes more for taller holes and significant top difference
     * For each hole, cost = height of hole
     * For each dip, cost = smaller of the height difference
     *
     * ( may be similar to sum of bumpiness and num of holes...)
     * @param testState
     * @return
     */
    public static Double getSignificantHoleAndTopDifferenceFixed(TestState testState){
        int sum = 0;
        for (int i = 0; i < State.COLS; i++) {
            int consec = 0;
            for(int j=0;j<testState.top[i];j++){
                if(testState.field[j][i] == 0){
                    consec++;
                }else{
                    sum += consec;
                    consec = 0;
                }
            }
            if ((i == 0 || testState.top[i - 1] >= testState.top[i] + 3) && (i == State.COLS - 1 || testState.top[i + 1] >= testState.top[i] + 3)){
                if(i== State.COLS - 1) sum += (testState.top[i - 1] - testState.top[i]);
                else if(i == 0 ) sum += (testState.top[i + 1] - testState.top[i]);
                else sum += Math.min(testState.top[i - 1] - testState.top[i], testState.top[i + 1] - testState.top[i]);
            }

        }
        return (double) sum;
    }

    /**
     * Measure of height variation
     * @param testState
     * @return mean absolute deviation of heights of columns
     */
    public static Double getMeanAbsoluteDeviationOfTop(TestState testState){
        double average = IntStream.of(testState.top).average().getAsDouble();

        double dev = 0;
        for(int i=0;i<State.COLS;i++){
            dev += Math.abs(testState.top[i]-average);
        }

        return dev/State.COLS;
    }

    /**
     * Help to ensure cubes can be placed
     * @param testState
     * @return 0 if there are at least two columns of same height, else returns 1.0
     */
    public static Double hasLevelSurface(TestState testState){
        int prev = testState.top[0];
        for(int i=1;i<State.COLS;i++){
            if(testState.top[i]== prev) return 0.0;
        }
        return 1.0;
    }

    /**
     * Help to ensure z-blocks can be placed
     * @param testState
     * @return 0 if there exist some column with its right column one unit higher, else returns 1.0
     */
    public static Double hasRightStep(TestState testState){
        int prev = testState.top[0];
        for (int i = 1; i < State.COLS; i++) {
            if (testState.top[i] == prev + 1) return 0.0;
        }
        return 1.0;
    }

    /**
     * Help to ensure reverse z-blocks can be placed
     *
     * @param testState
     * @return 0 if there exist some column with its left column one unit higher, else returns 1.0
     */
    public static Double hasLeftStep(TestState testState) {
        int prev = testState.top[0];
        for (int i = 1; i < State.COLS; i++) {
            if (testState.top[i] == prev - 1) return 0.0;
        }
        return 1.0;
    }

    /**
     *
     * @param testState
     * @return -1 * rows cleared last move
     */
    public static Double getNegativeOfRowsCleared(TestState testState){
        return (double) -1*testState.lastCleared;
    }

    /**
     *
     * @param testState
     * @return 1.0 if there exist some next piece that has no possible move, else 0
     */
    public static Double hasPossibleInevitableDeathNextPiece(TestState testState){
        for(int i=0;i<State.N_PIECES;i++){
            TestState possibleState = MoveTester.testMove(testState, i, 0);
            for(int move=1;move<State.legalMoves[i].length;move++){
                if(possibleState != null) break;
                possibleState = MoveTester.testMove(testState, i, move);
            }
            if(possibleState == null) return 1.0;
        }
        return 0.0;
    }

    /**
     * WARNING: SUPER SLOW
     * @param testState
     * @return 1.0 if there exist some next size two sequence that has no possible move, else 0
     */
    public static Double hasPossibleInevitableDeathNextTwoSequence(TestState testState) {
        for (int i = 0; i < State.N_PIECES; i++) {
            TestState possibleState = null;
            for (int move = 0; move < State.legalMoves[i].length; move++) {
                possibleState = MoveTester.testMove(testState, i, move);
                if(possibleState == null) continue;
                if(hasPossibleInevitableDeathNextPiece(possibleState) >= 0.99) possibleState = null;
            }
            if (possibleState == null) return 1.0;
        }
        return 0.0;
    }

    /**
     *
     * @param testState
     * @return number of columns with holes in them
     */
    public static Double getNumColsWithHoles(TestState testState){
        int numCol = 0;
        for(int col = 0; col<State.COLS; col++){
            for(int row = 0;row< testState.top[col]; row++){
                if(testState.field[row][col] == 0){
                    numCol++;
                    break;
                }
            }
        }
        return (double) numCol;
    }

    /**
     *
     * @param testState
     * @return number of rows with holes in them
     */
    public static Double getNumRowsWithHoles(TestState testState){
        boolean hasHole[] = new boolean[State.ROWS];
        int numRow = 0;
        for (int col = 0; col < State.COLS; col++) {
            for (int row = 0; row < testState.top[col]; row++) {
                if (testState.field[row][col] == 0 && !hasHole[row]) {
                    hasHole[row] = true;
                    numRow++;
                    break;
                }
            }
        }
        return (double) numRow;

    }

    /**
     * Based on a KTH paper - Tetris: A Heuristic Study
     * Using height-based weighing functions and breadth-first search
     * heuristics for playing Tetris
     * @param testState
     * @return sum of (row height)^2 of empty cells with right wall
     */
    public static Double getSpaceWithRightWallMeasure(TestState testState){
        int sum = 0;
        for(int col = 0; col< State.COLS - 1;col++){
            for(int row = 0; row< testState.top[col+1]; row++){
                if(testState.field[row][col] == 0 ) sum+=row*row;
            }
        }
        return (double) sum;
    }

    /**
     * Based on a KTH paper
     *
     * @param testState
     * @return sum of (row height)^2 of empty cells with left wall
     */
    public static Double getSpaceWithLeftWallMeasure(TestState testState) {
        int sum = 0;
        for (int col = 1; col < State.COLS; col++) {
            for (int row = 0; row < testState.top[col - 1]; row++) {
                if (testState.field[row][col] == 0) sum += row * row;
            }
        }
        return (double) sum;
    }



    /**
     * Based on a KTH paper
     *
     * @param testState
     * @return sum of (row height)^3 of holes
     */
    public static Double getHoleMeasure(TestState testState) {
        int sum = 0;
        for (int col = 0; col < State.COLS; col++) {
            for (int row = 0; row < testState.top[col] - 1; row++) {
                if (testState.field[row][col] == 0) sum += row * row * row;
            }
        }
        return (double) sum;
    }

    /**
     * Based on a KTH paper
     *
     * @param testState
     * @return sum of hole and wall measures
     */
    public static Double getAggregateHoleAndWallMeasure(TestState testState) {
        return (double) getSpaceWithLeftWallMeasure(testState) + getSpaceWithRightWallMeasure(testState) + getHoleMeasure(testState);
    }




    /**
     * Return heights of particular columns, could be helpful if player tends to favour /miss out certain columns
     * @param testState
     * @return
     */
    public static Double getFirstColHeight(TestState testState){
        return (double) testState.top[0];
    }

    public static Double getSecondColHeight(TestState testState) {
        return (double) testState.top[1];
    }

    public static Double getThirdColHeight(TestState testState) {
        return (double) testState.top[2];
    }

    public static Double getFourthColHeight(TestState testState) {
        return (double) testState.top[3];
    }

    public static Double getFifthColHeight(TestState testState) {
        return (double) testState.top[4];
    }

    public static Double getSixthColHeight(TestState testState) {
        return (double) testState.top[5];
    }

    public static Double getSeventhColHeight(TestState testState) {
        return (double) testState.top[6];
    }

    public static Double getEighthColHeight(TestState testState) {
        return (double) testState.top[7];
    }

    public static Double getNinthColHeight(TestState testState) {
        return (double) testState.top[8];
    }

    public static Double getTenthColHeight(TestState testState) {
        return (double) testState.top[9];
    }


    public static void addAllColHeightFeatures(List<Function<TestState, Double>> features){
        features.add(Features::getFirstColHeight);
        features.add(Features::getSecondColHeight);
        features.add(Features::getThirdColHeight);
        features.add(Features::getFourthColHeight);
        features.add(Features::getFifthColHeight);
        features.add(Features::getSixthColHeight);
        features.add(Features::getSeventhColHeight);
        features.add(Features::getEighthColHeight);
        features.add(Features::getNinthColHeight);
        features.add(Features::getTenthColHeight);
    }

    /**
     *
     * @param testState
     * @return top difference of pairwise columns
     */

    public static Double getFirstHeightDifference(TestState testState){
        return (double) Math.abs(testState.top[0]- testState.top[1]);
    }

    public static Double getSecondHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[1] - testState.top[2]);
    }

    public static Double getThirdHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[2] - testState.top[3]);
    }

    public static Double getFourthHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[3] - testState.top[4]);
    }

    public static Double getFifthHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[4] - testState.top[5]);
    }

    public static Double getSixthHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[5] - testState.top[6]);
    }

    public static Double getSeventhdHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[6] - testState.top[7]);
    }

    public static Double getEighthHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[7] - testState.top[8]);
    }

    public static Double getNinthHeightDifference(TestState testState) {
        return (double) Math.abs(testState.top[8] - testState.top[9]);
    }

    public static void addAllHeightDiffFeatures(List<Function<TestState, Double>> features) {
        features.add(Features::getFirstHeightDifference);
        features.add(Features::getSecondHeightDifference);
        features.add(Features::getThirdHeightDifference);
        features.add(Features::getFourthHeightDifference);
        features.add(Features::getFifthHeightDifference);
        features.add(Features::getSixthHeightDifference);
        features.add(Features::getSeventhdHeightDifference);
        features.add(Features::getEighthHeightDifference);
        features.add(Features::getNinthHeightDifference);
    }

    public static Function<TestState, Double> squareFunc(Function<TestState, Double> func) {
        return (state) -> {
            Double result = func.apply(state);
            return result * result;
        };
    }

    public static void addAllFeatures(List<Function<TestState, Double>> features) {
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getSumOfDepthOfHoles);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifference);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::hasLevelSurface);
        features.add(Features::getNumColsWithHoles);
        features.add(Features::getNumRowsWithHoles);
        features.add(Features::getFirstColHeight);
        features.add(Features::getSecondColHeight);
        features.add(Features::getThirdColHeight);
        features.add(Features::getFourthColHeight);
        features.add(Features::getFifthColHeight);
        features.add(Features::getSixthColHeight);
        features.add(Features::getSeventhColHeight);
        features.add(Features::getEighthColHeight);
        features.add(Features::getNinthColHeight);
        features.add(Features::getTenthColHeight);
        features.add(Features::getFirstHeightDifference);
        features.add(Features::getSecondHeightDifference);
        features.add(Features::getThirdHeightDifference);
        features.add(Features::getFourthHeightDifference);
        features.add(Features::getFifthHeightDifference);
        features.add(Features::getSixthHeightDifference);
        features.add(Features::getSeventhdHeightDifference);
        features.add(Features::getEighthHeightDifference);
        features.add(Features::getNinthHeightDifference);
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
    }

}
