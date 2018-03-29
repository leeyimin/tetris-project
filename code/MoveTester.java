public class MoveTester {

    // test a move based on the move index - its order in the legalMoves list
    public static TestState testMove(State s, int moveIndex) {
        int[][] legalMoves = s.legalMoves();
        return MoveTester.testMove(s, legalMoves[moveIndex]);
    }

    // test a move based on an array of orient and slot
    public static TestState testMove(State s, int[] move) {
        return MoveTester.testMove(s, move[State.ORIENT], move[State.SLOT]);
    }

    // returns null if you lose,
    // instance of TestState after move taken otherwise
    public static TestState testMove(State s, int orient, int slot) {
        // create alias for all static public fields
        int[] pOrients = State.getpOrients();
        int[][] pWidth = State.getpWidth();
        int[][] pHeight = State.getpHeight();
        int[][][] pBottom = State.getpBottom();
        int[][][] pTop = State.getpTop();

        // create alias for all instance fields
        int[][] stateField = s.getField();
        int[] stateTop = s.getTop();
        int nextPiece = s.getNextPiece();
        boolean lost = s.hasLost();
        int cleared = s.getRowsCleared();
        int turn = s.getTurnNumber();

        // deep copy field
        int[][] field = new int[State.ROWS][State.COLS];
        for (int r = 0; r < State.ROWS; r++) {
            for (int c = 0; c < State.COLS; c++) {
                field[r][c] = stateField[r][c];
            }
        }

        // deep copy top
        int[] top = new int[State.COLS];
        for (int c = 0; c < State.COLS; c++) {
            top[c] = stateTop[c];
        }

        // height if the first column makes contact
        int height = top[slot] - pBottom[nextPiece][orient][0];

        // for each column beyond the first in the piece
        for (int c = 1; c < pWidth[nextPiece][orient];c++) {
            height = Math.max(height, top[slot + c] - pBottom[nextPiece][orient][c]);
        }

        // check if game ended
        if (height + pHeight[nextPiece][orient] >= State.ROWS) {
            return null;
        }

        // for each column in the piece - fill in the appropriate blocks
        for (int i = 0; i < pWidth[nextPiece][orient]; i++) {
            // from bottom to top of brick
            for (int h = height + pBottom[nextPiece][orient][i]; h < height + pTop[nextPiece][orient][i]; h++) {
                field[h][i+slot] = turn;
            }
        }

        // adjust top
        for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
            top[slot + c] = height + pTop[nextPiece][orient][c];
        }

        // check for full rows - starting at the top
        for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
            // check all columns in the row
            boolean full = true;
            for (int c = 0; c < State.COLS; c++) {
                if (field[r][c] == 0) {
                    full = false;
                    break;
                }
            }

            // if the row was full - remove it and slide above stuff down
            if (full) {
                // for each column
                for (int c = 0; c < State.COLS; c++) {
                    // slide down all bricks
                    for (int i = r; i < top[c]; i++) {
                        field[i][c] = field[i + 1][c];
                    }
                }
            }
        }

        return new TestState(field, top);
    }

}
