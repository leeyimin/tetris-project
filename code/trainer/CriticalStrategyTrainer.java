package trainer;

import player.PlayerSkeleton;
import player.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CriticalStrategyTrainer extends LocalIncreasingDecreasingTrainer {

    public static final int TRIES_PER_STATE = 10;

    State startingState[];

    List<Double> normalCoefficients;
    List<Function<PlayerSkeleton.TestState, Double>> normalFeatures;

    int numStates;



    public CriticalStrategyTrainer(List<Double> normalCoefficients, List<Function<PlayerSkeleton.TestState, Double>> normalFeatures,
                                   List<Double> criticalCoefficients, List<Function<PlayerSkeleton.TestState, Double>> criticalFeatures) {
        super(criticalCoefficients, criticalFeatures);

        this.iterations = 5000;
        this.moves = 5000;

        this.normalCoefficients = normalCoefficients;
        this.normalFeatures = normalFeatures;

        numStates = iterations/TRIES_PER_STATE;
        startingState = new State[numStates];
        resultsInRound = new int[iterations];

        generateStartingState();
        backupBestAverage = 0;
        for (int i = 0; i < iterations; i++) {
            backupBestAverage += new TwoStrategyPlayer(normalCoefficients, normalFeatures, bestCoefficient, features, startingState[i % numStates]).simulateToNotCritical(moves);
        }
        backupBestAverage /= iterations;
    }

    private void generateStartingState() {
        for(int i=0;i<numStates;i++){
            System.out.print('.');
            startingState[i] = new TwoStrategyPlayer(normalCoefficients, normalFeatures, coefficients, features).simulateToCritical();
        }
        System.out.println();
    }

    @Override
    public void train() {
        while(true){
            for (int i = 0; i < this.iterations; i++) {
                int result = new TwoStrategyPlayer(normalCoefficients, normalFeatures, coefficients, features, startingState[i%numStates]).simulateToNotCritical(moves);
                this.onSimulateDone(result);
            }
        }
    }

    @Override
    public void onSimulateDone(int result) {
        rounds++;
        resultsInRound[(rounds - 1)% iterations] = result;
        rSum += result;
        if (rounds % iterations == 0 || rSum + (iterations - rounds) < bestResult) {

            printCurrentRound();

            rounds = 0;

            if (rSum > bestResult) {
                bestResult = rSum;
                bestCoefficient = new ArrayList<>(coefficients);
                coefficients.set(order[currentCoefficient], coefficients.get(order[currentCoefficient]) + increment);
            } else {
                currentCoefficient++;

                updateNextRoundIfNecessary();

                coefficients = new ArrayList<>(bestCoefficient);

                if (Math.abs(increment) < EPSILON) {

                    updateNextCycle();

                } else {

                    coefficients.set(order[currentCoefficient], coefficients.get(order[currentCoefficient]) + increment);
                }
            }

            rSum = 0;
            printCurrentBest();

        }
    }

    @Override
    String getFilePrefix() {
        return "CStrain";
    }

    void updateNextCycle() {
        printBest();

        boolean toPerturb = updateBackupBestAndParameters();

        bestResult = Integer.MIN_VALUE;

        //perturbation
        //TODO: perturbation strategy to be improved
        if (toPerturb) perturb();
    }

    void printCurrentRound() {
        //print results
        System.out.println("cc " + currentCoefficient + " increment " + increment + " order[cc]" + order[currentCoefficient]);

        System.out.println("critical coefficients");
        for (double r : coefficients) System.out.print(r + " ");
        System.out.println();

        System.out.println("avg: " + 1.0 * rSum / rounds);
        System.out.println("sum: " + rSum);
    }

    private boolean updateBackupBestAndParameters() {
        modifyParameters(moves);
        if (System.currentTimeMillis() - lastUpdate < interval) {
            return false;
        }

        boolean shouldPerturb = false;
        lastUpdate = System.currentTimeMillis();

        double currAverage = 0;
        for (int i = 0; i < iterations; i++) {
            currAverage += new TwoStrategyPlayer(normalCoefficients, normalFeatures, bestCoefficient, features, startingState[i % numStates]).simulateToNotCritical(moves);
        }
        currAverage /= iterations;

        if (bestCoefficient.equals(backupBest)) {
            backupBestAverage = currAverage = (currAverage+ backupBestAverage) / 2;
            shouldPerturb = true;
        } else if (currAverage > backupBestAverage) {
            backupBestAverage = currAverage;
            backupBest = new ArrayList<>(bestCoefficient);

        } else {
            double retest = 0;
            for (int i = 0; i < iterations; i++) {
                retest += new TwoStrategyPlayer(normalCoefficients, normalFeatures, backupBest, features, startingState[i % numStates]).simulateToNotCritical(moves);
            }
            retest /= iterations;
            if (currAverage > (retest + backupBestAverage) / 2) {
                backupBestAverage = currAverage;
                backupBest = new ArrayList<>(bestCoefficient);
            } else {
                bestCoefficient = new ArrayList<>(backupBest);
                backupBestAverage = currAverage = (retest + backupBestAverage) / 2;
                shouldPerturb = true;

            }
        }

        printLog(currAverage, shouldPerturb);
        return shouldPerturb;
    }

    boolean modifyParameters(int moves) {

        if (DECREASE_FLAG) {
            direction *= -1;
        }
        increment = direction * STARTING_INCREMENT;

        this.moves = moves;
        generateStartingState();

        resultsInRound = new int[iterations];

        return false;
    }

    public static void main(String[] args) {
        List<Double> coefficients = Arrays.asList(6.0, 0.0, -10.0, 0.0, 0.5, 16.0, 0.0, 0.0, 38.0, -4.0, 0.0, 0.0, -2.0, 0.5, -2.0, 2.0, 0.0, 0.0, 2.0, -0.5, 0.0, -2.0, 2.0, 0.0, 2.0, 0.0, 0.0, 0.0, 2.0, -0.5, 0.0, 3.2, 44.0);
        List<Double> normalCoefficients = Arrays.asList(6.0, 0.0, -10.0, 0.0, 0.5, 16.0, 0.0, 0.0, 38.0, -4.0, 0.0, 0.0, -2.0, 0.5, -2.0, 2.0, 0.0, 0.0, 2.0, -0.5, 0.0, -2.0, 2.0, 0.0, 2.0, 0.0, 0.0, 0.0, 2.0, -0.5, 0.0, 3.2, 44.0);

        List<Function<PlayerSkeleton.TestState, Double>> features = new ArrayList<>();

        features.add(PlayerSkeleton.Features::getNegativeOfRowsCleared);
        features.add(PlayerSkeleton.Features::getMaxHeight);
        features.add(PlayerSkeleton.Features::getNumHoles);
        features.add(PlayerSkeleton.Features::getSumOfDepthOfHoles);
        features.add(PlayerSkeleton.Features::getHeightAboveHoles);
        features.add(PlayerSkeleton.Features::getMeanAbsoluteDeviationOfTop);
        features.add(PlayerSkeleton.Features::getBlocksAboveHoles);
        features.add(PlayerSkeleton.Features::getSignificantHoleAndTopDifferenceFixed);
        features.add(PlayerSkeleton.Features::getNumOfSignificantTopDifference);
        features.add(PlayerSkeleton.Features::hasLevelSurface);

        features.add(PlayerSkeleton.Features::getNumColsWithHoles);
        features.add(PlayerSkeleton.Features::getNumRowsWithHoles);

        PlayerSkeleton.Features.addAllColHeightFeatures(features);
        PlayerSkeleton.Features.addAllHeightDiffFeatures(features);

        features.add(PlayerSkeleton.Features::getBumpiness);
        features.add(PlayerSkeleton.Features::getTotalHeight);

        new CriticalStrategyTrainer(normalCoefficients, features, coefficients, features).train();
    }
}
