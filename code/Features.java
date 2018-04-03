import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Features {

    private Features() {}

    public static Double getBumpiness(State state) {
        int bumpiness = 0;

        for (int col = 1; col < State.COLS; col++) {
            bumpiness += Math.abs(state.top[col - 1] - state.top[col]);
        }

        return (double) bumpiness;
    }

    public static Double getTotalHeight(State state) {
        int totalHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            totalHeight += state.top[col];
        }

        return (double) totalHeight;
    }

    public static Double getMaxHeight(State state) {
        int maxHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            maxHeight = Integer.max(maxHeight, state.top[col]);
        }

        return (double) maxHeight;
    }

    /**
     *
     * @param state
     * @return number of empty blocks where there exists a full block above it
     */
    public static Double getNumHoles(State state) {
        int numHoles = 0;

        for(int col = 0; col < State.COLS; col++){
            for(int row =0;row< state.top[col]-1;row++){
                if(state.field[row][col] == 0) numHoles++;
            }
        }

        System.out.println(numHoles);
        return (double) numHoles;
    }

    /**
     *
     * @param state
     * @return number of blocks above holes
     */
    public static Double getBlocksAboveHoles(State state){
        int numBlocks = 0;

        for(int col = 0; col < State.COLS;col++){
            boolean foundHole = false;
            for(int row = 0; row< state.top[col];row++){
                if(state.field[row][col] == 0) foundHole = true;
                else if(foundHole) numBlocks++;
            }
        }
        return (double)numBlocks;
    }

    /**
     * motivation: penalize stacking over holes/ covering tall holes
     * @param state
     * @return sum of (top - 1 ) - row over all holes
     */
    public static Double getSumOfDepthOfHoles(State state){
        int sum = 0;

        for (int col = 0; col < State.COLS; col++) {
            for (int row = 0; row < state.top[col]; row++) {
                if (state.field[row][col] == 0) sum += state.top[col] - 1 - row;
            }
        }
        return (double) sum;
    }

    /**
     * NOTE: may cause holes since it may incentivize turning significant top diff into hole
     * @param state
     * @return numbers of columns where all neighbouring columns are both at least 3 blocks taller
     */
    public static Double getNumOfSignificantTopDifference(State state){
        int num = 0;
        for(int i =0;i<State.COLS;i++){
            if((i == 0 || state.top[i-1] >= state.top[i]+3) && (i == State.COLS-1 || state.top[i + 1] >= state.top[i] + 3))
                num++;
        }
        return (double) num;
    }

    /**
     * penalizes more for taller holes and significant top difference
     * For each hole, cost = height
     * For each dip, cost = smaller of the height difference
     *
     * ( may be similar to sum of bumpiness and num of holes...)
     * @param state
     * @return
     */
    public static Double getSignificantHoleAndTopDifference(State state){
        int sum = 0;
        for (int i = 0; i < State.COLS; i++) {
            int consec = 0;
            for(int j=0;j<state.top[i];j++){
                if(state.field[j][i] == 0){
                    consec++;
                }else{
                    sum += consec;
                }
            }
            if ((i == 0 || state.top[i - 1] >= state.top[i] + 3) && (i == State.COLS - 1 || state.top[i + 1] >= state.top[i] + 3)){
                if(i== State.COLS - 1) sum += (state.top[i - 1] - state.top[i]);
                else if(i == 0 ) sum += (state.top[i + 1] - state.top[i]);
                else sum += Math.min(state.top[i - 1] - state.top[i], state.top[i + 1] - state.top[i]);
            }

        }
        return (double) sum;
    }

    /**
     * Measure of height variation
     * @param state
     * @return mean absolute deviation of heights of columns
     */
    public static Double getMeanAbsoluteDeviationOfTop(State state){
        double average = IntStream.of(state.top).average().getAsDouble();

        double dev = 0;
        for(int i=0;i<State.COLS;i++){
            dev += Math.abs(state.top[i]-average);
        }

        return dev/State.COLS;
    }

    /**
     * Help to ensure cubes can be placed
     * @param state
     * @return 0 if there are at least two columns of same height, else returns 1.0
     */
    public static Double hasLevelSurface(State state){
        int prev = state.top[0];
        for(int i=1;i<State.COLS;i++){
            if(state.top[i]== prev) return 0.0;
        }
        return 1.0;
    }

    /**
     * Help to ensure z-blocks can be placed
     * @param state
     * @return 0 if there exist some column with its right column one unit higher, else returns 1.0
     */
    public static Double hasRightStep(State state){
        int prev = state.top[0];
        for (int i = 1; i < State.COLS; i++) {
            if (state.top[i] == prev + 1) return 0.0;
        }
        return 1.0;
    }

    /**
     * Help to ensure reverse z-blocks can be placed
     *
     * @param state
     * @return 0 if there exist some column with its left column one unit higher, else returns 1.0
     */
    public static Double hasLeftStep(State state) {
        int prev = state.top[0];
        for (int i = 1; i < State.COLS; i++) {
            if (state.top[i] == prev - 1) return 0.0;
        }
        return 1.0;
    }

    /**
     *
     * @param state
     * @return number of columns with holes in them
     */
    public static Double getNumColsWithHoles(State state){
        int numCol = 0;
        for(int col = 0; col<State.COLS; col++){
            for(int row = 0;row< state.top[col]; row++){
                if(state.field[row][col] == 0){
                    numCol++;
                    break;
                }
            }
        }
        return (double) numCol;
    }

    /**
     *
     * @param state
     * @return number of rows with holes in them
     */
    public static Double getNumRowsWithHoles(State state){
        boolean hasHole[] = new boolean[State.ROWS];
        int numRow = 0;
        for (int col = 0; col < State.COLS; col++) {
            for (int row = 0; row < state.top[col]; row++) {
                if (state.field[row][col] == 0 && !hasHole[row]) {
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
     * @param state
     * @return sum of (row height)^2 of empty cells with right wall
     */
    public static Double getSpaceWithRightWallMeasure(State state){
        int sum = 0;
        for(int col = 0; col< State.COLS - 1;col++){
            for(int row = 0; row< state.top[col+1]; row++){
                if(state.field[row][col] == 0 ) sum+=row*row;
            }
        }
        return (double) sum;
    }

    /**
     * Based on a KTH paper
     *
     * @param state
     * @return sum of (row height)^2 of empty cells with left wall
     */
    public static Double getSpaceWithLeftWallMeasure(State state) {
        int sum = 0;
        for (int col = 1; col < State.COLS; col++) {
            for (int row = 0; row < state.top[col - 1]; row++) {
                if (state.field[row][col] == 0) sum += row * row;
            }
        }
        return (double) sum;
    }



    /**
     * Based on a KTH paper
     *
     * @param state
     * @return sum of (row height)^3 of holes
     */
    public static Double getHoleMeasure(State state) {
        int sum = 0;
        for (int col = 0; col < State.COLS; col++) {
            for (int row = 0; row < state.top[col] - 1; row++) {
                if (state.field[row][col] == 0) sum += row * row * row;
            }
        }
        return (double) sum;
    }

    /**
     * Based on a KTH paper
     *
     * @param state
     * @return sum of hole and wall measures
     */
    public static Double getAggregateHoleAndWallMeasure(State state) {
        return (double) getSpaceWithLeftWallMeasure(state) + getSpaceWithRightWallMeasure(state) + getHoleMeasure(state);
    }




    /**
     * Return heights of particular columns, could be helpful if player tends to favour /miss out certain columns
     * @param state
     * @return
     */
    public static Double getFirstColHeight(State state){
        return (double) state.top[0];
    }

    public static Double getSecondColHeight(State state) {
        return (double) state.top[1];
    }

    public static Double getThirdColHeight(State state) {
        return (double) state.top[2];
    }

    public static Double getFourthColHeight(State state) {
        return (double) state.top[3];
    }

    public static Double getFifthColHeight(State state) {
        return (double) state.top[4];
    }

    public static Double getSixthColHeight(State state) {
        return (double) state.top[5];
    }

    public static Double getSeventhColHeight(State state) {
        return (double) state.top[6];
    }

    public static Double getEighthColHeight(State state) {
        return (double) state.top[7];
    }

    public static Double getNinthColHeight(State state) {
        return (double) state.top[8];
    }

    public static Double getTenthColHeight(State state) {
        return (double) state.top[9];
    }


    public static void addAllColHeightFeatures(List<Function<State, Double>> features){
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
     * @param state
     * @return top difference of pairwise columns
     */

    public static Double getFirstHeightDifference(State state){
        return (double) Math.abs(state.top[0]- state.top[1]);
    }

    public static Double getSecondHeightDifference(State state) {
        return (double) Math.abs(state.top[1] - state.top[2]);
    }

    public static Double getThirdHeightDifference(State state) {
        return (double) Math.abs(state.top[2] - state.top[3]);
    }

    public static Double getFourthHeightDifference(State state) {
        return (double) Math.abs(state.top[3] - state.top[4]);
    }

    public static Double getFifthHeightDifference(State state) {
        return (double) Math.abs(state.top[4] - state.top[5]);
    }

    public static Double getSixthHeightDifference(State state) {
        return (double) Math.abs(state.top[5] - state.top[6]);
    }

    public static Double getSeventhdHeightDifference(State state) {
        return (double) Math.abs(state.top[6] - state.top[7]);
    }

    public static Double getEighthHeightDifference(State state) {
        return (double) Math.abs(state.top[7] - state.top[8]);
    }

    public static Double getNinthHeightDifference(State state) {
        return (double) Math.abs(state.top[8] - state.top[9]);
    }

    public static void addAllHeightDiffFeatures(List<Function<State, Double>> features) {
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

}
