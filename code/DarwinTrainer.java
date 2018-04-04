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

    /**
     * Testing all coefficients to play the game. Top 2 fittest coefficients are tracked.
     *
     * @param playerCoefficients
     * @param features
     * @return integer array containing the index of the 2 fittest coefficients in playerCoefficients
     */
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

    /**
     *
     *
     * @param resultPair
     * @param playerCoefficients
     * @return  ArrayList of the mutated cross product of the previous 2 fittest coefficients
     */
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

        /**
         *  Perfect cross breed
         */
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

        /**
         * Minor mutation cross breed
         */
        for (int i = 0; i < stupidCount; i++) {

            childCoefficient = new ArrayList<Double>();
            for (int j = 0; j < bestCoefficient.size(); j++) {
                parentCoin = ThreadLocalRandom.current().nextInt(0, 2);
                mutationDice = ThreadLocalRandom.current().nextInt(0, 4);

                if (parentCoin == 0) {

                    if (mutationDice == 0) {
                        mutatedCoefficient = bestCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-0.2,0.2));
                    } else {
                        mutatedCoefficient = bestCoefficient.get(j);
                    }

                    childCoefficient.add(Math.max(mutatedCoefficient, 0));

                } else {
                    if (mutationDice == 0) {
                        mutatedCoefficient = secondCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-0.2,0.2));
                    } else {
                        mutatedCoefficient = secondCoefficient.get(j);
                    }

                    childCoefficient.add(mutatedCoefficient);
                }
            }
            newGeneration.add(childCoefficient);
        }


        /**
         * Large mutation cross breed
         */
        for (int i = 0; i < retardedCount; i++) {

            childCoefficient = new ArrayList<Double>();
            for (int j = 0; j < bestCoefficient.size(); j++) {
                parentCoin = ThreadLocalRandom.current().nextInt(0, 2);
                mutationDice = ThreadLocalRandom.current().nextInt(0, 2);

                if (parentCoin == 0) {

                    if (mutationDice == 0) {
                        mutatedCoefficient = bestCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-2,2));
                    } else {
                        mutatedCoefficient = bestCoefficient.get(j);
                    }

                    childCoefficient.add(Math.max(mutatedCoefficient, 0));

                } else {
                    if (mutationDice == 0) {
                        mutatedCoefficient = secondCoefficient.get(j) +
                                (ThreadLocalRandom.current().nextDouble(-2,2));
                    } else {
                        mutatedCoefficient = secondCoefficient.get(j);
                    }

                    childCoefficient.add(mutatedCoefficient);
                }
            }
            newGeneration.add(childCoefficient);
        }

        return newGeneration;
    }

    public static void main(String args[]) {
        ArrayList<List<Double>> playerCoefficients = new ArrayList<>();
        List<Function<TestState, Double>> features = new ArrayList<>();
        List<Function<TestState, Double>> trimmedFeatures = new ArrayList<>();
        List<Double> oneSetCoefficients;
        int[] resultPair = new int[2];

        final int numPlayers = 1000;

        //Adding features
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getSumOfDepthOfHoles);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifference);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::hasLevelSurface);
        features.add(Features::getNumColsWithHoles);
        features.add(Features::getNumRowsWithHoles);
        Features.addAllColHeightFeatures(features);
        Features.addAllHeightDiffFeatures(features);
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);

        /**
         *  Adding increasing decreasing trainer's champion with trimmedFeatures
         */

        double[] championArray = {6.0, 0.5, -8.0, 0.0, 8.0, 0.0, 0.5, 26.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                2.0, 0.0, 0.0, 0.5, -1.0, 2.0, -2.0, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0, 3.2, 32.0};

        System.out.println("Full feature list size: " + features.size());
        System.out.println("championArray size: " + championArray.length);

        oneSetCoefficients = new ArrayList<>();

        for (int i = 0; i < championArray.length; i++) {

            double currentValue = championArray[i];

            if (currentValue != 0.0) {
                trimmedFeatures.add(features.get(i));
                oneSetCoefficients.add(currentValue);
            }
        }

        playerCoefficients.add(oneSetCoefficients);

        System.out.println("Trimmed feature list size: " + trimmedFeatures.size());
        System.out.println("coefficient size: " + oneSetCoefficients.size());

        /**
         * Populating random coefficients
         */
        for (int i = 0; i < numPlayers; i++) {
            oneSetCoefficients = new ArrayList<>();
            for (int j = 0; j < trimmedFeatures.size(); j++) {
                oneSetCoefficients.add(ThreadLocalRandom.current().nextDouble(-10,10));
            }
            playerCoefficients.add(oneSetCoefficients);
        }

        for (int generation = 1; generation < 20; generation++) {
            resultPair = oneGenerationRun(playerCoefficients, trimmedFeatures);
            if (generation < 19) {
                playerCoefficients = crossBreed(resultPair, playerCoefficients);
            }
        }

        System.out.println("Winner winner chicken dinner: " + playerCoefficients.get(resultPair[0]));
        System.out.println("Cai peng dinner: " + playerCoefficients.get(resultPair[1]));

    }

}
