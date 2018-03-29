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
}
