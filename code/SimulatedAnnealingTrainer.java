import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class SimulatedAnnealingTrainer extends Trainer {

    private static final int BATCH_SIZE = 100;
    private static final int NUM_BATCHES = 100000;

    private int currBatch;
    private int currRound;
    private int cumulatedRows;
    private double currentStepSize;
    private double stepSizeChange;
    private double stepSizeFloor;
    private int bestCumulatedRows;
    private List<Double> bestCoefficients;

    public SimulatedAnnealingTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(BATCH_SIZE * NUM_BATCHES, coefficients, features);
        this.currBatch = 0;
        this.currRound = 0;
        this.cumulatedRows = 0;
        this.currentStepSize = 5;
        this.stepSizeChange = 0.005;
        this.stepSizeFloor = 1;
        this.bestCumulatedRows = 0;
        this.bestCoefficients = coefficients;
    }

    public void onSimulateDone(int rowsCleared) {
        this.currRound++;
        this.cumulatedRows += rowsCleared;

        if (this.currRound % BATCH_SIZE == 0) {
            this.onBatchDone();
            this.cumulatedRows = 0;
            if (this.currentStepSize > stepSizeFloor) {
                //Temperature decrease
                this.currentStepSize -= stepSizeChange * ((double)(NUM_BATCHES - currBatch) / (double)NUM_BATCHES);
                if (this.currentStepSize < stepSizeFloor) {
                    this.currentStepSize = stepSizeFloor;
                }
            }
        }
    }

    private void onBatchDone() {
        // print the rows cleared
        System.out.println();
        System.out.println("======================================");
        System.out.println(" RESULT OF BATCH #" + (++currBatch));
        System.out.println("======================================");
        System.out.println("Rows cleared: " + (double) this.cumulatedRows / BATCH_SIZE);
        System.out.println("Current step size : " +  currentStepSize);
        this.printCoefficients();

        // if the new coefficients are better than the best so far
        // update the best coefficients
        if (this.bestCumulatedRows < this.cumulatedRows) {
            this.bestCumulatedRows = this.cumulatedRows;
            this.bestCoefficients = this.coefficients;
        }

        // pertubate the coefficients by a little every batch
        Random rng = new Random();
        double randomSign = (double)((rng.nextInt(2)) * 2 - 1);
        List<Double> newCoefficients = new ArrayList<>();
        for (Double coefficient : this.bestCoefficients) {
            newCoefficients.add(coefficient + randomSign * (this.currentStepSize * rng.nextDouble()));
        }
        this.coefficients = newCoefficients;
    }

    public static void main(String args[]) {
        Double[] coefficients = new Double[] {29.962215613633774, 19.319464772666812, 15.240956841509002, -1.2371753858723658, 15.596674922465308, -2.0498214208046766, 5.359734117478345, 4.4559643115714405, 9.642217009550793};

        List<Function<TestState, Double>> features = new ArrayList<>();
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getSumOfDepthOfHoles);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifference);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::hasLevelSurface);

        new SimulatedAnnealingTrainer(Arrays.asList(coefficients), features).train();
    }

}