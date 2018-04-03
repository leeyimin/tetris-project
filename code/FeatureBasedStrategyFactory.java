import java.util.function.Function;
import java.util.List;

public class FeaturesBasedStrategyFactory {

    private List<Double> coefficients;
    private List<Function<State, Double>> features;

    public FeaturesBasedStrategyFactory(List<Double> coefficients, List<Function<State, Double>> features) {
        this.coefficients = coefficients;
        this.features = features;
    }

    public void updateCoefficients(List<Double> coefficients) {
        this.coefficients = coefficients;
    }

    public Function<State, Integer> createGreedyStrategy() {
        return (state) -> {
            int bestMove = 0;
            double bestCost = Double.MAX_VALUE;

            for (int move = 0; move < state.legalMoves.length; move++) {
                State testState = state.testStateAfterMove(move);
                double cost = this.evaluateCost(testState);
                if (cost < bestCost) {
                    bestMove = move;
                    bestCost = cost;
                }
            }
            
            return bestMove;
        };
    }

    public Function<State, Integer> createLookAheadStrategy() {
        return (state) -> {
            int bestMove = 0;
            double bestCost = Double.MAX_VALUE;

            for (int move = 0; move < state.legalMoves.length; move++) {
                State testState = state.testStateAfterMove(move);
                double cost = this.evaluateLookAheadCost(testState);
                if (cost < bestCost) {
                    bestMove = move;
                    bestCost = cost;
                }
            }
            
            return bestMove;
        };
    }

    private double evaluateCost(State state) {
        double cost = 0;
        for (int i = 0; i < this.coefficients.size(); i++) {
            cost += this.coefficients.get(i) * this.features.get(i).apply(state);
        }
        return cost;
    }

    private double evaluateLookAheadCost(State state) {
        double cost = 0;
        for (int move = 0; move < state.legalMoves.length; move++) {
            State testState = state.testStateAfterMove(move);
            cost += this.evaluateCost(testState);
        }
        return cost / state.legalMoves.length;
    }

}
