import java.util.List;
import java.util.function.Function;

public class Player {

    protected static final int REFRESH_DELAY = 1;
    protected static final boolean RENDER_BOARD = false;
    protected static final boolean SHOW_DEATH_STATE = false;
    protected static final int MAX_NUM_MOVES = Integer.MAX_VALUE;

    private TFrame frame;

    private List<Double> coefficients;
    private List<Function<TestState, Double>> features;

    private int numMoves = 0;

    public Player(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this.coefficients = coefficients;
        this.features = features;
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
        }

        showDeathState(state);

        if (RENDER_BOARD) {
            this.frame.dispose();
        }

        return state.getRowsCleared();
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
