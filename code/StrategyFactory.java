import java.util.function.Function;
import java.util.List;

public class StrategyFactory {

    private List<Function<State, Double>> features;

    public StrategyFactory(List<Function<State, Double>> features) {
        this.features = features;
    }

    public Function<State, Integer> createGreedyStrategy(List<Double> coefficients) {
        return (state) -> {
            int bestMove = 0;
            double bestCost = Double.MAX_VALUE;
            for (int move = 0; move < state.legalMoves().length; move++) {
                State testState = state.testStateAfterMove(move);
                double cost = this.evaluateCost(testState, coefficients);
                if (cost < bestCost) {
                    bestMove = move;
                    bestCost = cost;
                }
            }
            return bestMove;
        };
    }

    public Function<State, Integer> createLookAheadStrategy(List<Double> coefficients) {
        return (state) -> {
            int bestMove = 0;
            double bestCost = Double.MAX_VALUE;
            for (int move = 0; move < state.legalMoves().length; move++) {
                State testState = state.testStateAfterMove(move);
                double cost = this.evaluateLookAheadCost(testState, coefficients);
                if (cost < bestCost) {
                    bestMove = move;
                    bestCost = cost;
                }
            }
            return bestMove;
        };
    }

    private double evaluateCost(State state, List<Double> coefficients) {
        double cost = 0;
        for (int i = 0; i < coefficients.size(); i++) {
            cost += coefficients.get(i) * this.features.get(i).apply(state);
        }
        return cost;
    }

    private double evaluateLookAheadCost(State state, List<Double> coefficients) {
        double cost = 0;
        for (int move = 0; move < state.legalMoves().length; move++) {
            State testState = state.testStateAfterMove(move);
            cost += this.evaluateCost(testState, coefficients);
        }
        return cost / state.legalMoves().length;
    }

}
