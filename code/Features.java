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


    public static Double getAverageTop(TestState testState){
        return IntStream.of(testState.top).average().getAsDouble();
    }

    public static Double getMeanAbsoluteDeviationOfTop(TestState testState){
        double average = IntStream.of(testState.top).average().getAsDouble();

        double dev = 0;
        for(int i=0;i<State.COLS;i++){
            dev += Math.abs(testState.top[i]-average);
        }

        return dev/State.COLS;
    }

}
