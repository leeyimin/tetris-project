import java.util.*;

public class PlayerSkeleton {

    private static int SPEED = 10;

    public int pickMove(State s, int[][] legalMoves) {
        int bestMove = 0;
        int bestCost = this.evaluateField(MoveTester.testMove(s, 0));

        for (int move = 1; move < legalMoves.length; move++) {
            int cost = evaluateField(MoveTester.testMove(s, move));
            Random r = new Random();

            if(r.nextInt(1000) < 1){
                continue;
            }

            if ((cost == bestCost && r.nextInt(10) < 5) || cost < bestCost) {
                bestMove = move;
                bestCost = cost;
            }
        }

        System.out.println("cost: " + bestCost);
        return bestMove;
    }

    public int evaluateField(TestState testState) {
        if (testState == null) {
            return Integer.MAX_VALUE;
        }

        return (
            2 * getBumpiness(testState) +
            10 * getTotalHeight(testState) +
            1/2 * getNumHoles(testState)
        );
    }

    public int getBumpiness(TestState testState) {
        int bumpiness = 0;

        for (int col = 1; col < State.COLS; col++) {
            bumpiness += Math.abs(testState.top[col - 1] - testState.top[col]);
        }

        return bumpiness;
    }

    public int getTotalHeight(TestState testState) {
        int totalHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            totalHeight += testState.top[col];
        }

        return totalHeight;
    }

    public int getMaxHeight(TestState testState) {
        int maxHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            maxHeight = Integer.max(maxHeight, testState.top[col]);
        }

        return maxHeight;
    }

    public int getNumHoles(TestState testState) {
        int height = getMaxHeight(testState);
        int numHoles = 0;

        for (int row = 0; row < height; row++) {
            numHoles += getNumHolesOnRow(testState, row);
        }

        return numHoles;
    }

    public int getNumHolesOnRow(TestState testState, int row) {
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

    public static void main(String[] args) {
        State state = new State();
        TFrame frame = new TFrame(state);
        PlayerSkeleton player = new PlayerSkeleton();

        while (!state.hasLost()) {
            state.makeMove(player.pickMove(state, state.legalMoves()));
            state.draw();
            state.drawNext(0,0);
            try {
                Thread.sleep(SPEED);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("You have completed " + state.getRowsCleared() + " rows.");
    }

}
