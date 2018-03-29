import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DarwinPlayer {

    private static int REFRESH_DELAY = 1;

    public int pickMove(State s, int[][] legalMoves, int[] currWeights) {
        int bestMove = 0;
        int bestCost = this.evaluateField(MoveTester.testMove(s, 0), currWeights);

        for (int move = 1; move < legalMoves.length; move++) {
            int cost = evaluateField(MoveTester.testMove(s, move), currWeights);
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

    public int evaluateField(TestState testState, int[] currWeights) {
        if (testState == null) {
            return Integer.MAX_VALUE;
        }

        return (
                currWeights[0] * Features.getBumpiness(testState) +
                currWeights[1] * Features.getTotalHeight(testState) +
                currWeights[2] * Features.getNumHoles(testState) +
                currWeights[3] * Features.getMaxHeight(testState)
        );
    }

    public static void main(String[] args) {

        DarwinPlayer[] battleRoyale = new DarwinPlayer[100];
        int[][] playerWeights = new int[battleRoyale.length][4];
        int highScore, secondHighScore, bestPlayer, secondBestPlayer;

        int averageRows = 0;
        highScore = 0;
        secondHighScore = 0;
        bestPlayer = -1;
        secondBestPlayer = -1;

        for (int i = 0; i < battleRoyale.length; i++) {
            battleRoyale[i] = new DarwinPlayer();

            for (int j = 0; j < 4; j++){
                playerWeights[i][j] = ThreadLocalRandom.current().nextInt(-10, 11);
            }
        }

        for (int i = 0; i < battleRoyale.length; i++) {

            DarwinPlayer currPlayer = battleRoyale[i];
            int[] currWeights = playerWeights[i];

            for (int j = 0; j < 10; j++){

                State state = new State();
                TFrame frame = new TFrame(state);
                state.draw();

                while (!state.hasLost()) {
                    state.makeMove(currPlayer.pickMove(state, state.legalMoves(), currWeights));
                    state.draw();
                    state.drawNext(0,0);
                    try {
                        Thread.sleep(REFRESH_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                averageRows += state.getRowsCleared();
            }

            averageRows = averageRows/battleRoyale.length;
            System.out.println("Player: " + i + " have completed on average " + averageRows + " rows.");

            if (averageRows > highScore) {
                highScore = averageRows;
                bestPlayer = i;
            } else if (averageRows > secondHighScore) {
                secondHighScore = averageRows;
                secondBestPlayer = i;
            }
        }

    }

}
