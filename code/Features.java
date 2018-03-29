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

    /**
     *
     * @param testState
     * @return numbers of columns where all neighbouring columns are both at least 3 blocks taller
     */
    public static Double getNumOfSignificantTopDifference(TestState testState){
        int num = 0;
        for(int i =0;i<State.COLS;i++){
            if((i == 0 || testState.top[i-1] >= testState.top[i]+3) && (i < State.COLS-1 || testState.top[i + 1] >= testState.top[i] + 3))
                num++;
        }
        return (double) num;
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
    public static Double hasPossibleDeathNextPiece(TestState testState){
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


}
