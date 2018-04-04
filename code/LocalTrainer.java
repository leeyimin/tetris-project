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
            newCoefficients.add(coefficient + this.stepSize * (rng.nextDouble() - 0.5));
        }
        this.coefficients = newCoefficients;
    }

    public static void main(String args[]) {
        /*
           Double[] coefficients = new Double[] { 2.8076482878262876, 9.52205211768384, 1.1654082765356404, 0.11757694915822758, 
           3.227936651873784, 4.127918804476838, 3.6275198005155755, 1.332574515842945, 1.948833014274156, 1.844853394469137,
           3.1667299474927315, 0.9287248528714973, 0.3974205294020895, 1.2099788795849369, 0.5851998600926502, 4.596407295055194,
           3.3174993445272625, 2.7003736997933534, 3.161659677428588, 3.0375575590367485, 4.410247099265778, 3.7599910254579814,
           2.9479913080723645, 1.6601874447011036 };
           */
        Random rng = new Random();
        List<Double> coefficients = new ArrayList<>();

        /*
        for (int i = 0; i < 5; i++) {
            coefficients.add(rng.nextDouble() - 0.5);
        }
        */

        coefficients.add(0.9906969863409814);
        coefficients.add(5.150433816053596);
        coefficients.add(0.04170423876619145);
        coefficients.add(-0.5280838169281842);
        coefficients.add(0.39639544240468605);

        List<Function<TestState, Double>> features = new ArrayList<>();
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getBlocksAboveHoles);
        /*
           features.add(Features::getNumOfSignificantTopDifference);
           features.add(Features::getMeanAbsoluteDeviationOfTop);
           features.add(Features::hasLevelSurface);
           features.add(Features::hasRightStep);
           features.add(Features::hasLeftStep);
           features.add(Features::getNegativeOfRowsCleared);
           features.add(Features::hasPossibleInevitableDeathNextPiece);
           features.add(Features::getNumColsWithHoles);
           features.add(Features::getNumRowsWithHoles);
           features.add(Features::getFirstColHeight);
           features.add(Features::getSecondColHeight);
           features.add(Features::getThirdColHeight);
           features.add(Features::getFourthColHeight);
           features.add(Features::getFifthColHeight);
           features.add(Features::getSixthColHeight);
           features.add(Features::getSeventhColHeight);
           features.add(Features::getEighthColHeight);
           features.add(Features::getNinthColHeight);
           features.add(Features::getTenthColHeight);
           */

        new LocalTrainer(coefficients, features).train();
    }

}
