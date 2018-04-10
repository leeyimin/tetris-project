import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class LookAheadPlayer extends Player {

    public LookAheadPlayer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(coefficients, features);
    }

    @Override
    public int pickMove(State currentState, int[][] legalMoves) {
        //minimax
        int bestMoves[] = new int[legalMoves.length];
        int count = 0;
        double bestCost = Double.MAX_VALUE;

        for (int move = 0; move < legalMoves.length; move++) {
            TestState state = MoveTester.testMove(currentState, move);
            double maxCost = Double.MIN_VALUE;

            if(state != null){
                for (int i = 0; i < State.N_PIECES; i++) {
                    TestState possibleState = MoveTester.testMove(state, i, 0);

                    double minCostForPiece = evaluateField(possibleState);
                    for (int j = 1; j < State.legalMoves[i].length && minCostForPiece > maxCost; j++) {
                        possibleState = MoveTester.testMove(state, i, j);
                        double costForMove = evaluateField(possibleState);
                        if (costForMove < minCostForPiece) minCostForPiece = costForMove;
                    }

                    if (minCostForPiece > maxCost) {
                        // take maximum of all minCost
                        maxCost = minCostForPiece;
                    }

                }
            }
            else maxCost = Double.MAX_VALUE;

            if (maxCost < bestCost) {
                count = 0;
                bestMoves[count++] = move;
                bestCost = maxCost;
            } else if (maxCost == bestCost) {
                bestMoves[count++] = move;
            }
        }

        return bestMoves[(new Random()).nextInt(count)];
    }
}


