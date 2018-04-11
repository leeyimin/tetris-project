import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Player {

    protected static final int REFRESH_DELAY = 1;
    protected static final boolean RENDER_BOARD = false;
    protected static final boolean SHOW_DEATH_STATE = true;
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
        List<Double> coefficients = Arrays.asList();
        List<Function<TestState, Double>> features = new ArrayList<>();
        Features.addAllFeatures(features);
        int total = 0;
        for (int i = 0; i < 20; i++) {
            int rows = new Player(coefficients, features).simulate();
            System.out.println(rows);
            total += rows;
        }
        System.out.println("Total: " + (double) total / 20);
    }

    public Player(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this.coefficients = coefficients;
        this.features = features;
        startTime = System.currentTimeMillis();
    }

    public int pickMove(State currentState, int[][] legalMoves) {
        int bestMove = 0;
        double bestCost = Double.MAX_VALUE;

        for (int move = 0; move < legalMoves.length; move++) {
            double cost = this.evaluateField(MoveTester.testMove(currentState, move));
            if (cost < bestCost) {
                bestMove = move;
                bestCost = cost;
            }
        }

        return bestMove;
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

        if(wasCritical && totalHeightOfState.getLast() < TwoStrategyPlayer.RETURN_TOTAL_HEIGHT){
            wasCritical = false;
        }
        else if(!wasCritical && totalHeightOfState.getLast() >= TwoStrategyPlayer.CRITICAL_TOTAL_HEIGHT){
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
}
