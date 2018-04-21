package trainer;

import player.PlayerSkeleton;

import java.util.*;
import java.util.function.*;

public class LocalTrainer {

    private static final int BATCH_SIZE = 10;
    private static final int STARTING_MOVES = 1000;
    private static final double STARTING_STEPS = 2.0;

    private int currBatch;
    private int currRound;
    private int cumulatedRows;
    private int bestCumulatedRows; 
    private int maxMoves;
    private double stepSize;
    private List<Double> bestCoefficients;
    private List<Double> coefficients;
    private List<Function<PlayerSkeleton.TestState, Double>> features;

    public LocalTrainer(List<Double> coefficients, List<Function<PlayerSkeleton.TestState, Double>> features) {
        this.currBatch = 0;
        this.currRound = 0;
        this.cumulatedRows = 0;
        this.bestCumulatedRows = 0;
        this.maxMoves = STARTING_MOVES;
        this.stepSize = STARTING_STEPS;
        this.bestCoefficients = coefficients;
        this.coefficients = coefficients;
        this.features = features;
    }

    public void train() {
        while (true) {
            int rowsCleared = new PlayerSkeleton(coefficients, features).simulate(this.maxMoves);
            this.onSimulateDone(rowsCleared);
        }
    }

    public void onSimulateDone(int rowsCleared) {
        this.currRound++;
        this.cumulatedRows += rowsCleared;

        if (this.currRound % BATCH_SIZE == 0) {
            this.onBatchDone();
            this.cumulatedRows = 0;
        }
    }

    private void onBatchDone() {
        // compute row count
        double averageRows = (double) this.cumulatedRows / BATCH_SIZE;
        double theoreticalMax = this.maxMoves * 0.4;

        // print the rows cleared
        System.out.println();
        System.out.println("======================================");
        System.out.println(" RESULT OF BATCH #" + (++currBatch));
        System.out.println("======================================");
        System.out.println("Rows cleared: " + averageRows + "/" + theoreticalMax);
        this.printCoefficients();

        // if the new coefficients are better than the best so far
        // update the best coefficients
        if (this.bestCumulatedRows < this.cumulatedRows) {
            this.bestCumulatedRows = this.cumulatedRows;
            this.bestCoefficients = this.coefficients;
        }

        // if the rows cleared are more than theoretical limit,
        // increase the limit
        if (averageRows > 0.95 * theoreticalMax) {
            this.maxMoves *= 2;
            this.stepSize *= 0.9;
            return;
        }

        // pertubate the coefficients by a little every batch
        Random rng = new Random();
        List<Double> newCoefficients = new ArrayList<>();
        for (Double coefficient : this.bestCoefficients) {
            newCoefficients.add(coefficient + 2 * this.stepSize * (rng.nextDouble() - 0.5));
        }
        this.coefficients = newCoefficients;
    }

    public void printCoefficients() {
        StringBuilder output = new StringBuilder();

        for (Double coefficient : this.coefficients) {
            output.append(String.format("%.2f, ", coefficient));
        }

        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    public static void main(String args[]) {
        List<Function<PlayerSkeleton.TestState, Double>> features = new ArrayList<>();
        features.add(PlayerSkeleton.Features::getNegativeOfRowsCleared);
        features.add(PlayerSkeleton.Features::getMaxHeight);
        features.add(PlayerSkeleton.Features::getNumHoles);
        features.add(PlayerSkeleton.Features::getSumOfDepthOfHoles);
        features.add(PlayerSkeleton.Features::getMeanAbsoluteDeviationOfTop);
        features.add(PlayerSkeleton.Features::getBlocksAboveHoles);
        features.add(PlayerSkeleton.Features::getSignificantHoleAndTopDifference);
        features.add(PlayerSkeleton.Features::getNumOfSignificantTopDifference);
        features.add(PlayerSkeleton.Features::hasLevelSurface);
        features.add(PlayerSkeleton.Features::getNumColsWithHoles);
        features.add(PlayerSkeleton.Features::getNumRowsWithHoles);
        PlayerSkeleton.Features.addAllColHeightFeatures(features);
        PlayerSkeleton.Features.addAllHeightDiffFeatures(features);
        features.add(PlayerSkeleton.Features::getBumpiness);
        features.add(PlayerSkeleton.Features::getTotalHeight);

        List<Double> coefficients = new ArrayList<>();
        coefficients.add(  4.00);
        coefficients.add(- 1.50);
        coefficients.add(- 8.00);
        coefficients.add(  0.00);
        coefficients.add(  8.00);
        coefficients.add(  0.00);
        coefficients.add(  0.50);
        coefficients.add( 26.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.05);
        coefficients.add(- 1.00);
        coefficients.add(  2.00);
        coefficients.add(- 2.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  0.00);
        coefficients.add(  3.20);
        coefficients.add( 30.00);

        new LocalTrainer(coefficients, features).train();
    }

}
