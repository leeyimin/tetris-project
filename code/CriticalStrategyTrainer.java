import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CriticalStrategyTrainer extends LocalIncreasingDecreasingTrainer {

    public static final int TRIES_PER_STATE = 10;

    State startingState[];

    List<Double> normalCoefficients;
    List<Function<TestState, Double>> normalFeatures;

    int numStates;



    public CriticalStrategyTrainer(List<Double> normalCoefficients, List<Function<TestState, Double>> normalFeatures,
                                   List<Double> criticalCoefficients, List<Function<TestState, Double>> criticalFeatures) {
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
            backupBestAverage += new TwoStrategyPlayer(normalCoefficients, normalFeatures, bestCoefficient[0], features, startingState[i % numStates]).simulateToNotCritical(moves);
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
        if (rounds % iterations == 0 || rSum + (iterations - rounds) < bestResult[0]) {

            printCurrentRound();

            rounds = 0;

            if (rSum > bestResult[0]) {
                bestResult[0] = rSum;
                bestCoefficient[0] = new ArrayList<>(coefficients);
                coefficients.set(order[currentCoefficient], coefficients.get(order[currentCoefficient]) + increment);
            } else {
                currentCoefficient++;

                updateNextRoundIfNecessary();

                coefficients = new ArrayList<>(bestCoefficient[0]);

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

        bestResult[0] = Integer.MIN_VALUE;

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
            currAverage += new TwoStrategyPlayer(normalCoefficients, normalFeatures, bestCoefficient[0], features, startingState[i % numStates]).simulateToNotCritical(moves);
        }
        currAverage /= iterations;

        if (bestCoefficient.equals(backupBest)) {
            backupBestAverage = currAverage = (currAverage+ backupBestAverage) / 2;
            shouldPerturb = true;
        } else if (currAverage > backupBestAverage) {
            backupBestAverage = currAverage;
            backupBest = new ArrayList<>(bestCoefficient[0]);

        } else {
            double retest = 0;
            for (int i = 0; i < iterations; i++) {
                retest += new TwoStrategyPlayer(normalCoefficients, normalFeatures, backupBest, features, startingState[i % numStates]).simulateToNotCritical(moves);
            }
            retest /= iterations;
            if (currAverage > (retest + backupBestAverage) / 2) {
                backupBestAverage = currAverage;
                backupBest = new ArrayList<>(bestCoefficient[0]);
            } else {
                bestCoefficient[0] = new ArrayList<>(backupBest);
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

        List<Function<TestState, Double>> features = new ArrayList<>();

        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getSumOfDepthOfHoles);
        features.add(Features::getHeightAboveHoles);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifferenceFixed);
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
