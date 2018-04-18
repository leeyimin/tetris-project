import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class SATrainer {

    private static final int BATCH_SIZE = 20;
    private static final int STARTING_MOVES = Integer.MAX_VALUE;
    private static final double THRESHOLD_MOVES = 1.0;
    private static final double HALF_PROB = 25;
    private static final double NORM_FACTOR = Math.log(2) / HALF_PROB;
    private static final double DELTA_TIME = 0.01;

    private int numBatches;
    private int numMoves;
    private double score;
    private double time;
    private List<Double> coefficients;
    private List<BiFunction<TestableState, TestableState, Integer>> features;

    public SATrainer(List<Double> coefficients, List<BiFunction<TestableState, TestableState, Integer>> features) {
        this.numBatches = -1;
        this.numMoves = STARTING_MOVES;
        this.score = Double.NEGATIVE_INFINITY;
        this.time = 1;
        this.coefficients = coefficients;
        this.features = features;
    }

    public SATrainer(List<Double> coefficients, List<BiFunction<TestableState, TestableState, Integer>> features, int numBatches) {
        this(coefficients, features);
        this.numBatches = numBatches;
    }

    public List<Double> train() {
        while (this.numBatches != 0) {
            this.climb();
            if (this.numBatches > 0) this.numBatches--;
        }
        return this.coefficients;
    }

    private void climb() {
        List<Double> newCoefficients = this.getNeighbour();
        double newScore = this.evaluate(newCoefficients);

        this.time += DELTA_TIME;

        if (this.shouldAccept(newScore)) {
            this.coefficients = newCoefficients;
            this.score = newScore;
            this.printSummary(newScore);
        }
    }

    private boolean shouldAccept(double newScore) {
        if (newScore > this.score) return true;

        Random rng = new Random();
        return rng.nextDouble() <= Math.exp(-this.time * NORM_FACTOR * (this.score - newScore));
    }

    private List<Double> getNeighbour() {
        List<Double> newCoefficients = new ArrayList<>(this.coefficients);
        Random rng = new Random();

        for (int i = 0; i < newCoefficients.size(); i++) {
            double coefficient = newCoefficients.get(i);
            newCoefficients.set(i, coefficient + 0.5 * rng.nextGaussian());
        }

        return newCoefficients;
    }

    private double evaluate(List<Double> coefficients) {
        int totalRowsCleared = IntStream
            .range(0, BATCH_SIZE)
            .parallel()
            .unordered()
            .map(i -> new Player(coefficients, this.features).simulate(this.numMoves))
            .sum();

        /*int totalRowsCleared = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            totalRowsCleared += new Player(coefficients, this.features).simulate(this.numMoves);
        }*/

        double averageRowsCleared = (double) totalRowsCleared / BATCH_SIZE;
        double maxRowsCleared = this.numMoves * 0.4;

        if (averageRowsCleared > THRESHOLD_MOVES * maxRowsCleared) {
            this.numMoves *= 2;
            System.out.println();
            System.out.println("Doubling numMoves to " + this.numMoves);
            System.out.println("Rows Cleared: " + averageRowsCleared);
            this.printCoefficients(coefficients);
        }

        return averageRowsCleared;
    }
        
    private void printSummary(double score) {
        System.out.println();
        if (this.numBatches > 0) System.out.println("Batch Num : " + this.numBatches);
        System.out.println("Time        : " + String.format("%.3f", this.time));
        System.out.println("Score       : " + score);
        System.out.println("Temperature : " + Math.exp(-this.time * NORM_FACTOR * HALF_PROB));
        this.printCoefficients();
    }

    public void printCoefficients() {
        this.printCoefficients(this.coefficients);
    }

    public void printCoefficients(List<Double> coefficients) {
        StringBuilder output = new StringBuilder();

        for (Double coefficient : coefficients) {
            output.append(String.format("%.2f, ", coefficient));
        }

        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    public static void main(String args[]) {
        List<BiFunction<TestableState, TestableState, Integer>> features = new ArrayList<>();
        Features.addAllFeatures(features);

        List<Double> coefficients = new ArrayList<>();
        /*for (int i = 0; i < features.size(); i++) {
            coefficients.add(0.0);
        }*/
        coefficients.add( 0.00);
        coefficients.add( 4.50);
        coefficients.add(-3.42);
        coefficients.add( 3.22);
        coefficients.add( 9.35);
        coefficients.add( 7.89);
        coefficients.add( 3.39);

        new SATrainer(coefficients, features).train();
    }

}
