import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BasicLookAheadTrainer extends LookAheadTrainer {
    final static int ITERATIONS_NUM = 100;

    long sum;
    int iterations;
    int result[];

    public BasicLookAheadTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this(coefficients, features, ITERATIONS_NUM);
    }

    public BasicLookAheadTrainer(List<Double> coefficients, List<Function<TestState, Double>> features, int numIterations) {
        super(numIterations, coefficients, features);
        sum = 0;
        iterations = 0;
        result = new int[numIterations];
    }

    public void onSimulateDone(int rowsCleared) {

        result[iterations] = rowsCleared;
        iterations++;
        sum += rowsCleared;
        System.out.println("Iteration " + iterations);
        this.printCoefficients();
        System.out.println("Rows cleared: " + rowsCleared);
        System.out.println("Average: " + ((double) sum / iterations));
        System.out.println();
    }

    public static BasicLookAheadTrainer getTrainerResults(List<Double> coefficients, List<Function<TestState, Double>> features, int numIterations) {
        BasicLookAheadTrainer trainer = new BasicLookAheadTrainer(coefficients, features, numIterations);
        trainer.numIterations = numIterations;
        trainer.train();
        return trainer;
    }

    public double getAverage() {
        return (double) sum / iterations;
    }

    public int getPercentile(int num) {
        Arrays.sort(result);
        return result[num * numIterations / 100];
    }

    public void printAllResults() {
        System.out.println();
        for (int r : result) {
            System.out.println(r);
        }
    }

    public static void main(String[] args) {
        List<Double> coefficients = Arrays.asList(32.0, 0.0, -8.0, 4.5, 22.0, 0.0, 0.0, 64.0, -2.0, 0.5, 4.0, -8.0, -2.0, 0.5, 2.0, 0.5, 0.0, 0.0, 2.0, 0.5, -8.0, 8.0, 0.0, 8.0, -0.5, 0.0, 0.0, 4.0, 0.0, 4.0, 9.2, 78.0);

        List<Function<TestState, Double>> features = new ArrayList<>();
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        //features.add(Features::getSumOfDepthOfHoles);
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
        new BasicLookAheadTrainer(coefficients, features).train();
    }
}
