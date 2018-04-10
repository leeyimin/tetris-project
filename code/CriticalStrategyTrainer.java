import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CriticalStrategyTrainer extends LocalIncreasingDecreasingTrainer {

    State[] startingStates;

    List<Double> normalCoefficients;
    List<Function<TestState, Double>> normalFeatures;


    public CriticalStrategyTrainer(List<Double> normalCoefficients, List<Function<TestState, Double>> normalFeatures,
                                   List<Double> criticalCoefficients, List<Function<TestState, Double>> criticalFeatures) {
        super(criticalCoefficients, criticalFeatures);

        this.iterations = 200;
        this.moves = 5000;

        this.normalCoefficients = normalCoefficients;
        this.normalFeatures = normalFeatures;
        startingStates = new State[iterations];
        resultsInRound = new int[iterations];
        generateStartingStates();
    }

    private void generateStartingStates() {
        List<Double> zeroCoefficients = new ArrayList<>();
        initialiseCoefficients(zeroCoefficients, features.size());
        for(int i=0;i<iterations;i++){
            startingStates[i] = new TwoStrategyPlayer(zeroCoefficients, normalFeatures, coefficients, features).simulateToCritical();
        }
    }

    @Override
    public void train() {
        while(true){
            for (int i = 0; i < this.iterations; i++) {
                int result = new TwoStrategyPlayer(normalCoefficients, normalFeatures, coefficients, features, startingStates[i]).simulateToNotCritical(moves);
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

        Arrays.sort(resultsInRound, 0, rounds);

        System.out.println("Back to not critical");
        for (int i = 0; i < rounds; i++) {
            System.out.print(resultsInRound[i] + " ");
        }

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

        double currAverage = TwoStrategyPlayer.getAverage(normalCoefficients, normalFeatures, bestCoefficient, features, 30);

        if (bestCoefficient.equals(backupBest)) {
            backupBestAverage = currAverage = (currAverage+ backupBestAverage) / 2;
            shouldPerturb = true;
        } else if (currAverage > backupBestAverage) {
            backupBestAverage = currAverage;
            backupBest = new ArrayList<>(bestCoefficient);

        } else {
            double retest = TwoStrategyPlayer.getAverage(normalCoefficients, normalFeatures, backupBest, features, 30);
            if (currAverage > (retest + backupBestAverage) / 2) {
                backupBestAverage = currAverage;
                backupBest = new ArrayList<>(bestCoefficient);
            } else {
                bestCoefficient = new ArrayList<>(backupBest);
                backupBestAverage = currAverage = (retest + backupBestAverage) / 2;
                shouldPerturb = true;

            }
        }

        printLog(currAverage);
        return shouldPerturb;
    }

    boolean modifyParameters(int moves) {

        if (DECREASE_FLAG) {
            direction *= -1;
        }
        increment = direction * STARTING_INCREMENT;

        this.moves = moves;
        iterations += 25;
        startingStates = new State[iterations];
        generateStartingStates();

        resultsInRound = new int[iterations];

        return false;
    }

    public static void main(String[] args) {
        List<Double> coefficients = Arrays.asList(6.0, 0.0, -10.0, 0.0, 0.5, 16.0, 0.0, 0.0, 38.0, -4.0, 0.0, 0.0, -2.0, 0.5, -2.0, 2.0, 0.0, 0.0, 2.0, -0.5, 0.0, -2.0, 2.0, 0.0, 2.0, 0.0, 0.0, 0.0, 2.0, -0.5, 0.0, 3.2, 44.0);
        List<Double> normalCoefficients = Arrays.asList(6.0, 0.0, -10.0, 0.0, 0.5, 16.0, 0.0, 0.0, 38.0, -4.0, 0.0, 0.0, -2.0, 0.5, -2.0, 2.0, 0.0, 0.0, 2.0, -0.5, 0.0, -2.0, 2.0, 0.0, 2.0, 0.0, 0.0, 0.0, 2.0, -0.5, 0.0, 3.2, 44.0);

        List<Function<TestState, Double>> features = new ArrayList<>();

        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getSumOfDepthOfHoles);
        features.add(Features::getHeightAboveHoles);
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

        new CriticalStrategyTrainer(normalCoefficients, features, coefficients, features).train();
    }
}
