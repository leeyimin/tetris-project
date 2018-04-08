import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Player {

    protected static final int REFRESH_DELAY = 1;
    protected static final boolean RENDER_BOARD = false;
    protected static final boolean SHOW_DEATH_STATE = false;
    protected static final int MAX_NUM_MOVES = Integer.MAX_VALUE;
    protected static final boolean WRITE_LOG = false;

    protected static final int LOG_CYCLE = 100;

    private TFrame frame;

    private List<Double> coefficients;
    private List<Function<TestState, Double>> features;

    private int numMoves = 0;
    private long startTime;

    public static void main(String[] args) {
        List<Double> coefficients = Arrays.asList(new Double[] { 1.37, 3.24, -6.16, 0.00, 23.83, -2.93, 3.47, 37.57, -4.92, 0.00, 0.00, -6.96, 6.83, 0.75, 0.00, 8.26, 0.00, 1.43, -3.69, 5.13, -5.64, 11.19, 9.09, 8.26, 13.91, 13.47, 8.01, 17.26, 6.83, 9.02, 0.00, 73.26 });
        List<Function<TestState, Double>> features = new ArrayList<>();
        Features.addAllFeatures(features);
        System.out.println(new Player(coefficients, features).simulate());
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

    private void writeLog(State state){
        if (WRITE_LOG && numMoves % LOG_CYCLE == 0) {
            TestState tState = new TestState(state.getField(), state.getTop(), 0);
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
    }

    private void updateBoard(State state) {
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

    private void showDeathState(State state){
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
