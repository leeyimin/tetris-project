import java.util.*;
import java.util.function.*;

public class Player {

    protected static int REFRESH_DELAY = 1;

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

    public void simulate() {
        State state = new State();
        this.frame = new TFrame(state);

        while (!state.hasLost()) {
            state.makeMove(this.pickMove(state, state.legalMoves()));
            state.draw();
            state.drawNext(0,0);
            try {
                Thread.sleep(REFRESH_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.frame.dispose();
        System.out.println("You have completed " + state.getRowsCleared() + " rows.");
    }

    public static void main(String[] args) {
        List<Double> coefficients = new ArrayList<>();
        List<Function<TestState, Double>> features = new ArrayList<>();

        coefficients.add(5.0);
        coefficients.add(1.0);
        coefficients.add(0.5);

        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);

        new Player(coefficients, features).simulate();
    }

}
