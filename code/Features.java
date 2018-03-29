public class Features {

    private Features() {}

    public static int getBumpiness(TestState testState) {
        int bumpiness = 0;

        for (int col = 1; col < State.COLS; col++) {
            bumpiness += Math.abs(testState.top[col - 1] - testState.top[col]);
        }

        return bumpiness;
    }

    public static int getTotalHeight(TestState testState) {
        int totalHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            totalHeight += testState.top[col];
        }

        return totalHeight;
    }

    public static int getMaxHeight(TestState testState) {
        int maxHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            maxHeight = Integer.max(maxHeight, testState.top[col]);
        }

        return maxHeight;
    }

    public static int getNumHoles(TestState testState) {
        int height = getMaxHeight(testState);
        int numHoles = 0;

        for (int row = 0; row < height; row++) {
            numHoles += Features.getNumHolesOnRow(testState, row);
        }

        return numHoles;
    }

    public static int getNumHolesOnRow(TestState testState, int row) {
        int numHoles = 0;

        for (int col = 0; col < State.COLS; col++) {
            boolean hasCeiling = false;
            int numConsecEmpty = 0;

            for(int r = row; r < getMaxHeight(testState); r++){
                if(testState.field[row][col] != 0 || numConsecEmpty == 3){
                    hasCeiling = true;
                    break;
                }
                if(testState.field[row][col] == 0) {
                    numConsecEmpty++;
                }
            }
            if (testState.field[row][col] == 0 && hasCeiling) {
                if(numConsecEmpty >= 5){
                    numHoles+=5;
                }
                numHoles++;
            }
        }

        return numHoles;
    }

}
