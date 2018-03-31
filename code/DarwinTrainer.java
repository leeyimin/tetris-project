import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;

public class DarwinTrainer extends Trainer {

    public DarwinTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(100, coefficients, features);
    }

    public void onSimulateDone(int rowsCleared, int playerIndex) {
        System.out.println("Player at index: " + playerIndex);
        this.printCoefficients();
        System.out.println("Rows cleared on average: " + rowsCleared);
        System.out.println();
    }

    public void onSimulateDone(int rowsCleared) {
        System.out.println("Player at index: -1");
        this.printCoefficients();
        System.out.println("Rows cleared: " + rowsCleared);
        System.out.println();
    }

    private static int[] oneGenerationRun(ArrayList<List<Double>> playerCoefficients,
                                          List<Function<TestState, Double>> features) {

        int[] resultPair = new int[2];
        List<Double> currentPlayer;
        int bestPlayer, secondBestPlayer, highScore, secondHighScore;
        int currResult;

        highScore = secondHighScore = -1;
        bestPlayer = secondBestPlayer = -1;

        for (int playerIndex = 0; playerIndex < playerCoefficients.size(); playerIndex++) {

            currentPlayer = playerCoefficients.get(playerIndex);

            System.out.println(currentPlayer);

            DarwinTrainer player = new DarwinTrainer(currentPlayer, features);

            currResult = player.getAverageResult();
            player.onSimulateDone(currResult, playerIndex);

            if (currResult > highScore) {
                highScore = currResult;
                bestPlayer = playerIndex;
            } else if (currResult > secondHighScore) {
                secondHighScore = currResult;
                secondBestPlayer = playerIndex;
            }

        }

        System.out.println("@@@@@@@@@@@@ BATTLE ROYALE RESULTS @@@@@@@@@@@@@");
        System.out.println("Best player index: " + bestPlayer + " with score of " + highScore);
        System.out.println("Second best player index: " + secondBestPlayer + " with score of " + secondHighScore);
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println();

        resultPair[0] = bestPlayer;
        resultPair[1] = secondBestPlayer;

        return resultPair;
    }

    private static  ArrayList<List<Double>> crossBreed(int[] resultPair, ArrayList<List<Double>> playerCoefficients) {

        List<Double> bestCoefficient = playerCoefficients.get(resultPair[0]);
        List<Double> secondCoefficient = playerCoefficients.get(resultPair[1]);

        ArrayList<Double> childCoefficient;
        ArrayList<List<Double>> newGeneration = new ArrayList<List<Double>>();

        int parentCoin, mutationDice;

        int totalCrossNum = (int)(Math.pow(bestCoefficient.size(), 2));
        int perfectCount = (int)(totalCrossNum * 0.4);
        int stupidCount =(int)(totalCrossNum * 0.4);
        int retardedCount = (int)(totalCrossNum * 0.6);

        double mutatedCoefficient;



        //Perfect cross breed
        for (int i = 0; i < perfectCount; i++) {

            childCoefficient = new ArrayList<Double>();
            for (int j = 0; j < bestCoefficient.size(); j++) {
                parentCoin = ThreadLocalRandom.current().nextInt(0, 2);



                if (parentCoin == 0) {
                    childCoefficient.add(bestCoefficient.get(j));
                } else {
                    childCoefficient.add(secondCoefficient.get(j));
                }
            }
            newGeneration.add(childCoefficient);
        }

        //stupid cross breed
        for (int i = 0; i < stupidCount; i++) {

            childCoefficient = new ArrayList<Double>();
            for (int j = 0; j < bestCoefficient.size(); j++) {
                parentCoin = ThreadLocalRandom.current().nextInt(0, 2);
                mutationDice = ThreadLocalRandom.current().nextInt(0, 3);

                if (parentCoin == 0) {

                    if (mutationDice == 0) {
                        mutatedCoefficient = bestCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-0.3,0.3));
                    } else {
                        mutatedCoefficient = bestCoefficient.get(j);
                    }

                    childCoefficient.add(Math.max(mutatedCoefficient, 0));

                } else {
                    if (mutationDice == 0) {
                        mutatedCoefficient = secondCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-0.3,0.3));
                    } else {
                        mutatedCoefficient = secondCoefficient.get(j);
                    }

                    childCoefficient.add(Math.max(mutatedCoefficient, 0));
                }
            }
            newGeneration.add(childCoefficient);
        }


        //retarded crossbreed
        for (int i = 0; i < retardedCount; i++) {

            childCoefficient = new ArrayList<Double>();
            for (int j = 0; j < bestCoefficient.size(); j++) {
                parentCoin = ThreadLocalRandom.current().nextInt(0, 2);
                mutationDice = ThreadLocalRandom.current().nextInt(0, 2);

                if (parentCoin == 0) {

                    if (mutationDice == 0) {
                        mutatedCoefficient = bestCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-1,1));
                    } else {
                        mutatedCoefficient = bestCoefficient.get(j);
                    }

                    childCoefficient.add(Math.max(mutatedCoefficient, 0));

                } else {
                    if (mutationDice == 0) {
                        mutatedCoefficient = secondCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-0.4,0.4));
                    } else {
                        mutatedCoefficient = secondCoefficient.get(j);
                    }

                    childCoefficient.add(Math.max(mutatedCoefficient, 0));
                }
            }
            newGeneration.add(childCoefficient);
        }

        return newGeneration;
    }

    public static void main(String args[]) {
        ArrayList<List<Double>> playerCoefficients = new ArrayList<>();
        List<Function<TestState, Double>> features = new ArrayList<>();
        List<Double> oneSetCoefficients;
        int[] resultPair = new int[2];

        final int numPlayers = 20;

        //Adding features
        features.add(Features::hasPossibleInevitableDeathNextPiece);
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::hasRightStep);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getTotalHeight);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::getNumHoles);

        //Populating random weights
        for (int i = 0; i < numPlayers; i++) {
            oneSetCoefficients = new ArrayList<Double>();
            for (int j = 0; j < features.size(); j++) {
                oneSetCoefficients.add(ThreadLocalRandom.current().nextDouble(0,10));
            }
            playerCoefficients.add(oneSetCoefficients);
        }

        for (int generation = 1; generation < 20; generation++) {
            resultPair = oneGenerationRun(playerCoefficients, features);
            if (generation < 19) {
                playerCoefficients = crossBreed(resultPair, playerCoefficients);
            }

        }

        System.out.println("Winner winner chicken dinner: " + playerCoefficients.get(resultPair[0]));
        System.out.println("Cai peng dinner: " + playerCoefficients.get(resultPair[1]));

    }

}
