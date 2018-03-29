import java.util.*;

public class PlayerSkeleton {

    private static int SPEED = 1;

    public int pickMove(State s, int[][] legalMoves) {
        int bestMove = 0;
        int bestCost = evaluateField(MoveTester.testMove(s, 0));

        for (int move = 1; move < legalMoves.length; move++) {
            int cost = evaluateField(MoveTester.testMove(s, move));
            Random r = new Random();

            if(r.nextInt(1000) < 1){
                continue;
            }else {
                if (cost == bestCost) {
                    if (r.nextInt(10) < 5) {
                        bestMove = move;
                        bestCost = cost;
                    }
                } else if (cost < bestCost) {
                    bestMove = move;
                    bestCost = cost;
                }
            }
        }

        System.out.println("cost: " + bestCost);
        return bestMove;
    }

    public int evaluateField(MoveResult moveResult) {
        if (moveResult == null) {
            return Integer.MAX_VALUE;
        }

        return (
            10 * getTotalHeight(moveResult) +
            2 * getBumpiness(moveResult) +
            1/2 * getNumHoles(moveResult)
        );
    }

    public int getBumpiness(MoveResult moveResult) {
        int bumpiness = 0;

        for (int col = 1; col < State.COLS; col++) {
            bumpiness += Math.abs(moveResult.top[col - 1] - moveResult.top[col]);
        }

        return bumpiness;
    }

    public int getMaxHeight(MoveResult moveResult) {
        int maxHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            maxHeight = Integer.max(maxHeight, moveResult.top[col]);
        }

        return maxHeight;
    }

    public int getTotalHeight(MoveResult moveResult) {
        int totalHeight = 0;

        for (int col = 0; col < State.COLS; col++) {
            totalHeight += moveResult.top[col];
        }

        return totalHeight;
    }

    public int getNumGapsOnRow(MoveResult moveResult, int row) {
        int numGaps = 0;

        for (int col = 0; col < State.COLS; col++) {
            if (moveResult.field[row][col] == 0) {
                numGaps++;
            }
        }

        return numGaps;
    }

    public int getNumHolesOnRow(MoveResult moveResult, int row) {
        int numHoles = 0;

        for (int col = 0; col < State.COLS; col++) {
            boolean hasCeiling = false;
            int numConsecEmpty = 0;

            for(int r = row; r < getMaxHeight(moveResult); r++){
                if(moveResult.field[row][col] != 0 || numConsecEmpty == 3){
                    hasCeiling = true;
                    break;
                }
                if(moveResult.field[row][col] == 0) {
                    numConsecEmpty++;
                }
            }
            if (moveResult.field[row][col] == 0 && hasCeiling) {
                if(numConsecEmpty >= 5){
                    numHoles+=5;
                }
                numHoles++;
            }
        }

        return numHoles;
    }

    public int getNumHoles(MoveResult moveResult) {
        int height = getMaxHeight(moveResult);
        int numHoles = 0;

        for (int row = 0; row < height; row++) {
            numHoles += getNumHolesOnRow(moveResult, row);
        }

        return numHoles;
    }

    public int getNumGaps(MoveResult moveResult) {
        int height = getMaxHeight(moveResult);
        int numGaps = 0;

        for (int row = 0; row < height; row++) {
            numGaps += getNumGapsOnRow(moveResult, row);
        }

        return numGaps;
    }

    public int getNumRowsWithSparseGaps(MoveResult moveResult) {
        int height = getMaxHeight(moveResult);
        int numRowsWithSparseGaps = 0;

        for (int row = 0; row < height; row++) {
            if (getNumGapsOnRow(moveResult, row) <= 3) {
                numRowsWithSparseGaps++;
            }
        }

        return numRowsWithSparseGaps;
    }

    public int getGapsOnBottomRow(MoveResult moveResult) {
        return getNumGapsOnRow(moveResult, 0);
    }

    public int getNumGapsOnTopRow(MoveResult moveResult) {
        int topRow = getMaxHeight(moveResult);
        return getNumGapsOnRow(moveResult, topRow);
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
