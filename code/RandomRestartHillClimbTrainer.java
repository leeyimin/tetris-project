import java.util.*;
import java.util.function.*;

public class RandomRestartHillClimbTrainer {

    private static final int NUM_BATCHES = 1000;
    private static final int TEST_BATCH_SIZE = 30;
    private static final double RAND_MEAN = 0;
    private static final double RAND_STDDEV = 1;

    private List<Function<TestState, Double>> features;
    private List<Double> bestCoefficients;
    private double bestRowsCleared;

    public RandomRestartHillClimbTrainer(List<Function<TestState, Double>> features) {
        this.features = features;
        this.bestRowsCleared = Double.NEGATIVE_INFINITY;
    }

    public void train() {
        while (true) {
            Random rng = new Random();
            List<Double> startingCoefficients = new ArrayList<>();
            for (int i = 0; i < features.size(); i++) {
                startingCoefficients.add(rng.nextGaussian() * RAND_STDDEV + RAND_MEAN);
            }

            List<Double> endCoefficients = new HillClimbTrainer(startingCoefficients, features, NUM_BATCHES).train();
            double rowsCleared = this.evaluate(endCoefficients);
            if (rowsCleared > this.bestRowsCleared) {
                this.bestRowsCleared = rowsCleared;
                this.bestCoefficients = endCoefficients;
            }

            this.printSummary(endCoefficients, rowsCleared);
        }
    }

    private void printSummary(List<Double> coefficients, double rowsCleared) {
        System.out.println();
        System.out.println("##############################");
        System.out.println("##############################");
        System.out.println("##     RESULT FOR BATCH     ##");
        System.out.println("##############################");
        System.out.println("##############################");
        System.out.println();
        System.out.println("Rows Cleared: " + rowsCleared);
        System.out.println("Coefficients: ");
        this.printCoefficients(coefficients);
        System.out.println();
        System.out.println("Best Rows Cleared: " + this.bestRowsCleared);
        System.out.println("Best Coefficients: ");
        this.printCoefficients(this.bestCoefficients);
    }

    private void printCoefficients(List<Double> coefficients) {
        StringBuilder output = new StringBuilder();

        for (Double coefficient : coefficients) {
            output.append(String.format("%.2f, ", coefficient));
        }

        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    private double evaluate(List<Double> coefficients) {
        int totalRowsCleared = 0;
        for (int i = 0; i < TEST_BATCH_SIZE; i++) {
            totalRowsCleared += new Player(coefficients, this.features).simulate();
        }

        double averageRowsCleared = (double) totalRowsCleared / TEST_BATCH_SIZE;
        return averageRowsCleared;
    }

    public static void main(String args[]) {
        List<Function<TestState, Double>> features = new ArrayList<>();
        Features.addAllFeatures(features);

        new RandomRestartHillClimbTrainer(features).train();
    }

}
