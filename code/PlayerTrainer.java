import java.util.*;

public class PlayerTrainer {

    private static int REFRESH_DELAY = 1;
    private static int NUM_ITERATIONS = 30;

    private double[] coefficients;

    public PlayerTrainer(double[] coefficients) {
        this.coefficients = coefficients;
    }

    public int pickMove(State s, int[][] legalMoves) {
        int bestMove = 0;
        double bestCost = this.evaluateField(MoveTester.testMove(s, 0));

        for (int move = 1; move < legalMoves.length; move++) {
            double cost = this.evaluateField(MoveTester.testMove(s, move));
            Random rng = new Random();

            if(rng.nextInt(1000) < 1){
                continue;
            }

            if ((cost == bestCost && rng.nextInt(10) < 5) || cost < bestCost) {
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

        return (
            this.coefficients[0] * Features.getBumpiness(testState) +
            this.coefficients[1] * Features.getTotalHeight(testState) +
            this.coefficients[2] * Features.getNumHoles(testState)
        );
    }

   public static void main(String[] args) {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            State state = new State();
            TFrame frame = new TFrame(state);
            PlayerTrainer player = new PlayerTrainer(new double[] {1, 2, 0.5});

            while (!state.hasLost()) {
                state.makeMove(player.pickMove(state, state.legalMoves()));
                state.draw();
                state.drawNext(0,0);
                try {
                    Thread.sleep(REFRESH_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("You have completed " + state.getRowsCleared() + " rows.");
        }

    }

}
