import java.util.*;
import java.util.function.*;

public class HillClimbTrainer {

    private static final int BATCH_SIZE = 50;
    private static final int STARTING_MOVES = 1000;
    private static final double THRESHOLD_MOVES = 0.995;
    private static final double STARTING_STEPS = 3.0;
    private static final double DECAY_STEPS = 1.1;

    private int currDimension;
    private int numBatches;
    private int numMoves;
    private double stepSize;
    private List<Double> coefficients;
    private List<Function<TestState, Double>> features;

    public HillClimbTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this.currDimension = 0;
        this.numBatches = -1;
        this.numMoves = STARTING_MOVES;
        this.stepSize = STARTING_STEPS;
        this.coefficients = coefficients;
        this.features = features;
    }

    public HillClimbTrainer(List<Double> coefficients, List<Function<TestState, Double>> features, int numBatches) {
        this(coefficients, features);
        this.numBatches = numBatches;
    }

    public List<Double> train() {
        while (this.numBatches != 0) {
            this.climb();
            this.currDimension = (this.currDimension + 1) % this.features.size();
            if (this.currDimension == 0) {
                this.stepSize /= DECAY_STEPS;
            }
            if (this.numBatches > 0) this.numBatches--;
        }
        return this.coefficients;
    }

    private void climb() {
        int direction = this.getDirection();
        if (direction == 0) return;

        double currDimensionCoefficient = this.coefficients.get(this.currDimension);
        double prevScore = this.evaluate(this.coefficients);

        while (true) {
            currDimensionCoefficient += this.stepSize * direction;
            this.coefficients.set(this.currDimension, currDimensionCoefficient);
            double score = this.evaluate(this.coefficients);

            if (score < prevScore) {
                currDimensionCoefficient -= this.stepSize * direction;
                this.coefficients.set(this.currDimension, currDimensionCoefficient);
                break;
            }

            this.printSummary(score);
            prevScore = score;
        }
    }

    private int getDirection() {
        List<Double> testCoefficients = new ArrayList<>(this.coefficients);
        double currDimensionValue = this.coefficients.get(this.currDimension);

        double neutralScore = this.evaluate(testCoefficients);
        testCoefficients.set(this.currDimension, currDimensionValue + this.stepSize);
        double positiveScore = this.evaluate(testCoefficients);
        testCoefficients.set(this.currDimension, currDimensionValue - this.stepSize);
        double negativeScore = this.evaluate(testCoefficients);

        if (neutralScore > positiveScore && neutralScore > negativeScore) return 0; 
        return positiveScore > negativeScore ? 1 : -1;
    }

    private double evaluate(List<Double> coefficients) {
        int totalRowsCleared = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            totalRowsCleared += new Player(coefficients, this.features).simulate(this.numMoves);
        }

        double averageRowsCleared = (double) totalRowsCleared / BATCH_SIZE;
        double maxRowsCleared = this.numMoves * 0.4;

        if (averageRowsCleared > THRESHOLD_MOVES * maxRowsCleared) {
            this.numMoves *= 2;
        }

        return averageRowsCleared;
    }
        
    private void printSummary(double score) {
        System.out.println();
        if (this.numBatches > 0) System.out.println("Batch Num : " + this.numBatches);
        System.out.println("Dimension : " + this.currDimension);
        System.out.println("Step Size : " + String.format("%.4f", this.stepSize));
        System.out.println("Score     : " + score);
        this.printCoefficients();
    }

    public void printCoefficients() {
        StringBuilder output = new StringBuilder();

        for (Double coefficient : this.coefficients) {
            output.append(String.format("%.2f, ", coefficient));
        }

        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    public static void main(String args[]) {
        List<Function<TestState, Double>> features = new ArrayList<>();
        Features.addAllFeatures(features);

        List<Double> coefficients = Arrays.asList(new Double[] { 1.37, 3.24, -6.16, 0.00, 23.83, -2.93, 3.47, 37.57, -4.92, 0.00, 0.00, -6.96, 6.83, 0.75, 0.00, 8.26, 0.00, 1.43, -3.69, 5.13, -5.64, 11.19, 9.09, 8.26, 13.91, 13.47, 8.01, 17.26, 6.83, 9.02, 0.00, 73.26 });

        new HillClimbTrainer(coefficients, features).train();
    }

}
