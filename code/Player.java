import java.util.*;
import java.util.function.*;

public class Player {

    protected static final int REFRESH_DELAY = 1;
    protected static final boolean RENDER_BOARD = false;

    private TFrame frame;

    private List<Double> coefficients;
    private List<Function<TestState, Double>> features;

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
        State state = new State();

        if (RENDER_BOARD) {
            this.frame = new TFrame(state);
        }

        while (!state.hasLost()) {
            state.makeMove(this.pickMove(state, state.legalMoves()));
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

        if (RENDER_BOARD) {
            this.frame.dispose();
        }

        return state.getRowsCleared();
    }

}
