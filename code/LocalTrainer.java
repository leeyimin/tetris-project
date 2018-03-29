/*
import java.util.*;
import java.util.function.*;

public class LocalTrainer extends Trainer {

    private static final int BATCH_SIZE = 100;
    private static final int NUM_BATCHES = 100000;

    private int currBatch;
    private int currRound;
    private int cumulatedRows;
    private double stepSize;
    private int bestCumulatedRows; 
    private List<Double> bestCoefficients;

    public LocalTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(BATCH_SIZE * NUM_BATCHES, coefficients, features);
        this.currBatch = 0;
        this.currRound = 0;
        this.cumulatedRows = 0;
        this.stepSize = 0.2;
        this.bestCumulatedRows = 0;
        this.bestCoefficients = coefficients;
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
        // print the rows cleared
        System.out.println();
        System.out.println("RESULT OF BATCH #" + (++currBatch));
        System.out.println("======================================");
        System.out.println("Rows cleared: " + (double) this.cumulatedRows / BATCH_SIZE);
        this.printCoefficients();

        // if the new coefficients are better than the best so far
        // update the best coefficients
        if (this.bestCumulatedRows < this.cumulatedRows) {
            this.bestCumulatedRows = this.cumulatedRows;
            this.bestCoefficients = this.coefficients;
        }

        // pertubate the coefficients by a little every batch
        Random rng = new Random();
        List<Double> newCoefficients = new ArrayList<>();
        for (Double coefficient : this.bestCoefficients) {
            newCoefficients.add(Math.abs(coefficient + this.stepSize * (rng.nextDouble() - this.stepSize / 2)));
        }
        this.coefficients = newCoefficients;
    }

    public static void main(String args[]) {
        List<Double> coefficients = new ArrayList<>();

        coefficients.add(0.5104353223422937);
        coefficients.add(3.817903728201659);
        coefficients.add(0.31012531039568025);
        coefficients.add(0.14667791183942425);
        coefficients.add(2.2330640842735687);
        coefficients.add(2.263339721291005);
        coefficients.add(0.18623383673088817);
        coefficients.add(0.5414191629604356);
        coefficients.add(0.6156312845858801);
        coefficients.add(2.5117122546500923);
        coefficients.add(2.0367323121409804);

        /*
        for (int i = 0; i < 11; i++) {
            coefficients.add(2.0);
        }
        */

        List<Function<TestState, Double>> features = new ArrayList<>();
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::hasLevelSurface);
        features.add(Features::hasRightStep);
        features.add(Features::hasLeftStep);
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::hasPossibleDeathNextPiece);

        new LocalTrainer(coefficients, features).train();
    }

}
*/
