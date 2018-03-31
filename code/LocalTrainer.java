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
        this.stepSize = 0.5;
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
        System.out.println("======================================");
        System.out.println(" RESULT OF BATCH #" + (++currBatch));
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
        // General Coefficient
        Double[] coefficients = new Double[] { 1.4282372400117123, 9.516277725900398, 0.13525561944365183,  0.07368618867991161,
            4.5116824995818865, 4.150977779792673, 1.184726054395163, 0.9914349168793231, 1.1084028932492331, 3.437920660029056, 4.9684152255437155 };

        //Double[] coefficients = new Double[] { 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0 };

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
        features.add(Features::hasPossibleInevitableDeathNextPiece);

        new LocalTrainer(Arrays.asList(coefficients), features).train();
    }

}
