import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Player {

    protected static final int REFRESH_DELAY = 1;
    protected static final boolean RENDER_BOARD = false;

    protected TFrame frame;

    private List<Double> coefficients;
    private List<BiFunction<TestableState, TestableState, Integer>> features;

    protected int numMoves = 0;

    public Player(List<Double> coefficients, List<BiFunction<TestableState, TestableState, Integer>> features) {
        this.coefficients = coefficients;
        this.features = features;
    }

    public int pickMove(State currentState, int[][] legalMoves) {
        int bestMove = 0;
        double bestCost = Double.MAX_VALUE;

        TestableState testState = new TestableState(currentState);
        for (int move = 0; move < legalMoves.length; move++) {
            double cost = this.evaluateState(testState.testMove(move), testState);
            if (cost < bestCost) {
                bestMove = move;
                bestCost = cost;
            }
        }

        // eval(testState.testMove(bestMove), testState);
        return bestMove;
    }


    public void eval(TestableState state, TestableState prevState) {
        System.out.println(this.features.get(6).apply(state, prevState));
    }

    public double evaluateState(TestableState state, TestableState prevState) {
        if (state == null) {
            return Double.MAX_VALUE;
        }

        int score = 0;
        for (int i = 0; i < this.coefficients.size(); i++) {
            score += this.coefficients.get(i) * this.features.get(i).apply(state, prevState);
        }
        return score;
    }

    public int simulate() {
        return this.simulate(Integer.MAX_VALUE);
    }

    public int simulate(int maxMoves){
        State state = new State();

        if (RENDER_BOARD) {
            this.frame = new TFrame(state);
        }

        while (!state.hasLost() && this.numMoves < maxMoves) {
            this.numMoves++;
            state.makeMove(this.pickMove(state, state.legalMoves()));
            this.updateBoard(state);
        }

        if (RENDER_BOARD) {
            this.frame.dispose();
        }

        return state.getRowsCleared();
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
}
