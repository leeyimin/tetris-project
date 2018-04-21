package player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PlayerSkeleton {

    protected static final int REFRESH_DELAY = 1;
    protected static final boolean RENDER_BOARD = false;
    protected static final boolean SHOW_DEATH_STATE = false;
    protected static final int MAX_NUM_MOVES = Integer.MAX_VALUE;
    protected static final boolean WRITE_LOG = false;
    protected static final int LAST_MOVES = 500;

    protected static final int LOG_CYCLE = 100;

    protected TFrame frame;

    private List<Double> coefficients;
    private List<Function<TestState, Double>> features;

    protected int numMoves = 0;
    private long startTime;

    private LinkedList<Integer> totalHeightOfState = new LinkedList<>();
    private int countCritical = 0;
    private boolean wasCritical = false;

    public static void main(String[] args) {
        List<Double> coefficients = Arrays.asList(new Double[]{11.51, 3.24, -15.31, 0.00, 23.83, -2.02, 3.47, 26.01, -3.10, 0.00, 0.00, -12.97, 12.84, -4.71, 12.02, 1.65, 0.00, 5.42, 2.92, 5.13, -5.64, 19.19, 9.09, 20.28, 13.91, 13.47, 10.26, 17.26, 6.83, 9.93, 0.00, 97.06});
        List<Function<TestState, Double>> features = new ArrayList<>();
        Features.addAllFeatures(features);
        int total = 0;
        for (int i = 0; i < 20; i++) {
            int rows = new PlayerSkeleton(coefficients, features).simulate();
            System.out.println(rows);
            total += rows;
        }
        System.out.println("Total: " + (double) total / 20);
    }

    public PlayerSkeleton(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this.coefficients = coefficients;
        this.features = features;
        startTime = System.currentTimeMillis();
    }

    public int pickMove(State currentState, int[][] legalMoves) {
        int bestMoves[] = new int[legalMoves.length];
        int count = 0;
        double bestCost = Double.MAX_VALUE;

        for (int move = 0; move < legalMoves.length; move++) {
            double cost = this.evaluateField(MoveTester.testMove(currentState, move));
            if (cost < bestCost) {
                count = 0;
                bestMoves[count++] = move;
                bestCost = cost;
            } else if (cost == bestCost) {
                bestMoves[count++] = move;
            }
        }

        return bestMoves[(new Random()).nextInt(count)];
    }

    public double evaluateField(TestState testState) {
        if (testState == null) {
            return Double.MAX_VALUE;
        }

        int score = 0;

        for (int i = 0; i < this.coefficients.size(); i++) {
            score += this.coefficients.get(i) * this.features.get(i).apply(testState);
        }

        return score;
    }

    public int simulate() {
        return simulate(MAX_NUM_MOVES);
    }

    public int simulate(int maxMoves){
        State state = new State();

        if (RENDER_BOARD) {
            this.frame = new TFrame(state);
        }

        while (!state.hasLost() && numMoves < maxMoves) {
            numMoves++;
            state.makeMove(this.pickMove(state, state.legalMoves()));
            updateBoard(state);
            writeLog(state);
        }

        showDeathState(state);

        if (RENDER_BOARD) {
            this.frame.dispose();
        }

        return state.getRowsCleared();
    }

    protected void writeLog(State state){
        if(!WRITE_LOG) return;
        TestState tState = new TestState(state.getField(), state.getTop(), 0);
        totalHeightOfState.addLast(Features.getTotalHeight(tState).intValue());
        if(totalHeightOfState.size() > LAST_MOVES){
            totalHeightOfState.removeFirst();
        }

        if(wasCritical && totalHeightOfState.getLast() < 60){
            wasCritical = false;
        }
        else if(!wasCritical && totalHeightOfState.getLast() >= 150){
            wasCritical = true;
            countCritical++;
        }

        if (numMoves % LOG_CYCLE == 0 || state.hasLost()) {
            try {
                String filename = "data/player" + startTime + ".csv";
                FileWriter fw = new FileWriter(filename, true); //the true will append the new data
                fw.write(numMoves + ", " + Features.getTotalHeight(tState) + ", " + Features.getMaxHeight(tState) + ", " + Features.getNumHoles(tState));
                fw.write("\n");
                fw.close();
            } catch (IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
        }
        if(state.hasLost()){
            try {
                String filename = "data/player" + startTime + ".csv";
                FileWriter fw = new FileWriter(filename, true); //the true will append the new data
                for(int i:totalHeightOfState){
                    fw.write(i +",");
                }
                fw.write("\n");
                fw.write(countCritical + "\n");
                fw.close();
            } catch (IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
        }
    }

    protected void updateBoard(State state) {
        if (RENDER_BOARD) {
            state.draw();
            state.drawNext(0,0);
            try {
                Thread.sleep(REFRESH_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void showDeathState(State state){
        int field[][] = state.getField();

        if( SHOW_DEATH_STATE && state.hasLost()){
            System.out.println();
            for(int i=State.ROWS-1;i>=0;i--){
                for(int j=0;j< State.COLS;j++){
                    System.out.print(field[i][j] == 0?" ": "X");
                }
                System.out.println();
            }
        }
    }

    public static class Features {

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
        public static Double getSignificantHoleAndTopDifference(TestState testState) {
            int sum = 0;
            for (int i = 0; i < State.COLS; i++) {
                int consec = 0;
                for (int j = 0; j < testState.top[i]; j++) {
                    if (testState.field[j][i] == 0) {
                        consec++;
                    } else {
                        sum += consec;
                    }
                }

            }
            return (double) sum + getSignificantTopDifference(testState);
        }

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
            }
            return (double) sum + getSignificantTopDifference(testState);
        }

        public static Double getSignificantTopDifference(TestState testState){
            int sum = 0;
            for (int i = 0; i < State.COLS; i++) {
                if ((i == 0 || testState.top[i - 1] >= testState.top[i] + 3) && (i == State.COLS - 1 || testState.top[i + 1] >= testState.top[i] + 3)) {
                    if (i == State.COLS - 1) sum += (testState.top[i - 1] - testState.top[i]);
                    else if (i == 0) sum += (testState.top[i + 1] - testState.top[i]);
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

    public static class MoveTester {

        // test a move based on the move index - its order in the legalMoves list
        public static TestState testMove(State s, int moveIndex) {
            int[][] legalMoves = s.legalMoves();
            return MoveTester.testMove(s, legalMoves[moveIndex]);
        }

        // test a move based on an array of orient and slot
        public static TestState testMove(State s, int[] move) {
            return MoveTester.testMove(s, move[State.ORIENT], move[State.SLOT]);
        }

        // test a move based on an array of orient and slot
        public static TestState testMove(TestState s, int piece, int moveIndex) {
            return MoveTester.testMove(s, piece, State.legalMoves[piece][moveIndex][State.ORIENT], State.legalMoves[piece][moveIndex][State.ORIENT]);
        }

        // returns null if you lose,
        // instance of player.PlayerSkeleton.TestState after move taken otherwise
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
            int lastCleared = 0;

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
                    lastCleared++;
                    // for each column
                    for (int c = 0; c < State.COLS; c++) {
                        // slide down all bricks
                        for (int i = r; i < top[c]; i++) {
                            field[i][c] = field[i + 1][c];
                        }
                    }
                }
            }

            return new TestState(field, top, lastCleared);
        }

        // returns null if you lose,
        // instance of player.PlayerSkeleton.TestState after move taken otherwise
        public static TestState testMove(TestState s, int piece, int orient, int slot) {
            // create alias for all static public fields
            int[] pOrients = State.getpOrients();
            int[][] pWidth = State.getpWidth();
            int[][] pHeight = State.getpHeight();
            int[][][] pBottom = State.getpBottom();
            int[][][] pTop = State.getpTop();

            // create alias for all instance fields
            int[][] stateField = s.field;
            int[] stateTop = s.top;
            int nextPiece = piece;
            int lastCleared = 0;
            int turn = 1;

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
            for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
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
                    field[h][i + slot] = turn;
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
                    lastCleared++;
                    // for each column
                    for (int c = 0; c < State.COLS; c++) {
                        // slide down all bricks
                        for (int i = r; i < top[c]; i++) {
                            field[i][c] = field[i + 1][c];
                        }
                    }
                }
            }

            return new TestState(field, top, lastCleared);
        }

    }

    public static class TestState {

        public int[][] field;
        public int[] top;

        public int lastCleared;

        public TestState(int[][] field, int[] top, int lastCleared) {
            this.field = field;
            this.top = top;
            this.lastCleared = lastCleared;
        }

    }
}
