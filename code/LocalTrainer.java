import java.util.*;
import java.util.function.*;

public class LocalTrainer extends Trainer {

    private static final int BATCH_SIZE = 100;
    private static final int NUM_BATCHES = 100000;
    private static final double LEARNING_RATE = 0.25;

    private int currBatch;
    private int currIteration;
    private int currRowsCleared;
    private int bestCumulatedRows; 
    private List<Double> bestCoefficients;

    public LocalTrainer(List<Double> coefficients, List<Function<State, Double>> features) {
        this.currBatch = 0;
        this.currIteration = 0;
        this.cumulatedRows = 0;
        this.bestCumulatedRows = 0;
        this.bestCoefficients = coefficients;
    }

    public void onRunFinished(int rowsCleared) {
        this.currIteration++;
        this.currRowsCleared += rowsCleared;

        if (this.currIteration % BATCH_SIZE == 0) {
            this.printSummary();
            this.updateBest();
            this.pertubateCoefficients();
            this.currIteration = 0;
            this.currRowsCleared = 0;
        }
    }

    private void printSummary() {
        System.out.println();
        System.out.println("======================================");
        System.out.println(" RESULT OF BATCH #" + (++currBatch));
        System.out.println("======================================");
        System.out.println("Rows cleared: " + (double) this.cumulatedRows / BATCH_SIZE);
        this.printCoefficients();
    }

    private void updateBest() {
        if (this.bestRowsCleared < this.currRowsCleared) {
            this.bestRowsCleared = this.currRowsCleared;
            this.bestCoefficients = this.coefficients;
        }
    }

    private void pertubateCoefficients() {
        Random rng = new Random();
        List<Double> newCoefficients = new ArrayList<>();
        for (Double coefficient : this.bestCoefficients) {
            newCoefficients.add(Math.abs(coefficient + LocalTrainer.LEARNING_RATE * (rng.nextDouble() - LocalTrainer.LEARNING_RATE / 2)));
        }
        this.coefficients = newCoefficients;
    }

    public static void main(String args[]) {
        List<Double> coefficients = new ArrayList<>();
        List<Function<State, Double>> features = new ArrayList<>();

        coefficients.add(2.80764828782628);
        coefficients.add(9.52205211768384);
        coefficients.add(1.16540827653564);
        coefficients.add(0.11757694915822);

        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);

        new LocalTrainer(coefficients, features).train();
    }

}
