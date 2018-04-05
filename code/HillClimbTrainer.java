import java.util.*;
import java.util.function.*;

public class HillClimbTrainer {

    private static final int BATCH_SIZE = 100;
    private static final double STARTING_STEPS = 0.5;

    private int currDimension;
    private double stepSize;
    private List<Double> coefficients;
    private List<Function<TestState, Double>> features;

    public HillClimbTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this.currDimension = 0;
        this.stepSize = STARTING_STEPS;
        this.coefficients = coefficients;
        this.features = features;
    }

    public void train() {
        while (true) {
            this.climb();
            this.currDimension = (this.currDimension + 1) % this.features.size();
            if (this.currDimension == 0) {
                this.stepSize /= 2;
            }
        }
    }

    private void climb() {
        int direction = this.getDirection();
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
        testCoefficients.set(this.currDimension, currDimensionValue + this.stepSize * 2);
        double positiveScore = this.evaluate(testCoefficients);
        testCoefficients.set(this.currDimension, currDimensionValue - this.stepSize * 2);
        double negativeScore = this.evaluate(testCoefficients);
        return positiveScore > negativeScore ? 1 : 0;
    }

    private double evaluate(List<Double> coefficients) {
        int totalRowsCleared = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            totalRowsCleared += new Player(coefficients, this.features).simulate();
        }
        return (double) totalRowsCleared / BATCH_SIZE;
    }
        
    private void printSummary(double score) {
        System.out.println();
        System.out.println("Dimension : " + this.currDimension);
        System.out.println("Step Size : " + this.stepSize);
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
        Features.addAllColHeightFeatures(features);
        Features.addAllHeightDiffFeatures(features);
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);

        List<Double> coefficients = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            coefficients.add(0.00);
        }

        /*
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
        */

        new HillClimbTrainer(coefficients, features).train();
    }

}
