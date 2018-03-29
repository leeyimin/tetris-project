import java.util.*;

public class Player {

    private static int REFRESH_DELAY = 1;

    public int pickMove(State s, int[][] legalMoves) {
        int bestMove = 0;
        int bestCost = this.evaluateField(MoveTester.testMove(s, 0));

        for (int move = 1; move < legalMoves.length; move++) {
            int cost = evaluateField(MoveTester.testMove(s, move));
            Random r = new Random();

            if(r.nextInt(1000) < 1){
                continue;
            }

            if ((cost == bestCost && r.nextInt(10) < 5) || cost < bestCost) {
                bestMove = move;
                bestCost = cost;
            }
        }

        return bestMove;
    }

    public int evaluateField(TestState testState) {
        if (testState == null) {
            return Integer.MAX_VALUE;
        }

        return (
            2 * Features.getBumpiness(testState) +
            10 * Features.getTotalHeight(testState) +
            1/2 * Features.getNumHoles(testState)
        );
    }

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            State state = new State();
            TFrame frame = new TFrame(state);
            Player player = new Player();

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
