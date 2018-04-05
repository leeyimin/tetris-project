import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

//Trigonometric additive cooling
// http://what-when-how.com/artificial-intelligence/a-comparison-of-cooling-schedules-for-simulated-annealing-artificial-intelligence/
public class SimulatedAnnealingTrainer extends Trainer {

    private static final int BATCH_SIZE = 100;
    private static final int NUM_BATCHES = 100000;

    private int currBatch;
    private int currRound;
    private int cumulatedRows;
    private double initialStepSize;
    private double stepSizeFloor;
    private double currentStepSize;
    private int bestCumulatedRows;
    private List<Double> bestCoefficients;

    public SimulatedAnnealingTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(BATCH_SIZE * NUM_BATCHES, coefficients, features);
        this.currBatch = 0;
        this.currRound = 0;
        this.cumulatedRows = 0;
        this.initialStepSize = 2.00;
        this.stepSizeFloor = 0.00005;
        this.currentStepSize = initialStepSize;
        this.bestCumulatedRows = 0;
        this.bestCoefficients = coefficients;
    }

    public void onSimulateDone(int rowsCleared) {
        this.currRound++;
        this.cumulatedRows += rowsCleared;

        if (this.currRound % BATCH_SIZE == 0) {
            this.onBatchDone();
            this.cumulatedRows = 0;
            this.currentStepSize = stepSizeFloor + 0.5 * (initialStepSize - stepSizeFloor) * (1 + Math.cos(currBatch * Math.PI / NUM_BATCHES));
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
        List<Double> coefficients = new ArrayList<>();

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
        features.add(Features::getNumColsWithHoles);
        features.add(Features::getNumRowsWithHoles);
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        Features.addAllColHeightFeatures(features);
        Features.addAllHeightDiffFeatures(features);

        initialiseCoefficients(coefficients, features.size());

        new SimulatedAnnealingTrainer(coefficients, features).train();
    }

    public static void initialiseCoefficients(List<Double> coefficients, int size){
        for(int i = 0; i < size; i++) coefficients.add(0.0);
    }

}