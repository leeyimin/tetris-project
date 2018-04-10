import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FairPlayer extends Player{



    public FairPlayer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(coefficients, features);
        System.out.println("FAIR PLAYER");
    }

    @Override
    public int pickMove(State currentState, int[][] legalMoves) {
        int bestMoves[] = new int[legalMoves.length];
        int count = 0;
        double bestCost = Double.MAX_VALUE;

        for (int move = 0; move < legalMoves.length; move++) {
            double cost = this.evaluateField(MoveTester.testMove(currentState, move));
            if (cost < bestCost) {
                count = 0;
                bestMoves[count++]= move;
                bestCost = cost;
            }
            else if(cost == bestCost){
                bestMoves[count++] = move;
            }
        }

        return bestMoves[(new Random()).nextInt(count)];
    }
}
