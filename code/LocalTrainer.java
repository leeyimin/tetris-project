/*
import java.util.*;
import java.util.function.*;

public class LocalTrainer extends Trainer {

    private static final int BATCH_SIZE = 10;
    private static final int NUM_BATCHES = 100;

    private int currRound;
    private int cumulatedRows;
    private double stepSize;
    private int bestCumulatedRows; 
    private List<Double> bestCoefficients;

    public BasicTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(BATCH_SIZE * NUM_BATTLES, coefficients, features);

        this.currRound = 0;
        this.cumulatedRows = 0;
        this.stepSize = 10.0;
        this.bestCumulatedRows = 0;
        this.bestCoefficients = coefficients;
    }

    public void onSimulateDone(int rowsCleared) {
        this.currRound++;
        this.cumulatedRows += rowsCleared;

        if (this.currRound % BATCH_SIZE == 0) {
            this.updateCoefficient();
            this.cumulatedRows = 0;
        }
    }

    private void updateCoefficients() {
        Random rng = new Random();
        List<Double> newCoefficients = new ArrayList<>();

        for (Double coefficient : this.coefficients) {
            newCoefficients.add(coefficient + stepSize * rng.nextDouble());
        }
    }

    public static void main(String args[]) {
        List<Double> coefficients = new ArrayList<>();
        coefficients.add(2.0);
        coefficients.add(10.0);
        coefficients.add(0.5);

        List<Function<TestState, Double>> features = new ArrayList<>();
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);

        new BasicTrainer(coefficients, features).train();
    }

}
*/
