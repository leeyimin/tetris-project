package trainer;

import player.PlayerSkeleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BasicTrainer extends Trainer {

    final static int ITERATIONS_NUM =100;

    long sum;
    int iterations;
    int result[];

    public BasicTrainer(List<Double> coefficients, List<Function<PlayerSkeleton.TestState, Double>> features) {
        this(coefficients, features, ITERATIONS_NUM);
    }

    public BasicTrainer(List<Double> coefficients, List<Function<PlayerSkeleton.TestState, Double>> features, int numIterations) {
        super(numIterations, coefficients, features);
        sum = 0;
        iterations = 0;
        result = new int[numIterations];
    }

    public void onSimulateDone(int rowsCleared) {

        result[iterations] = rowsCleared;
        iterations++;
        sum += rowsCleared;
        System.out.println("Iteration " + iterations);
        this.printCoefficients();
        System.out.println("Rows cleared: " + rowsCleared);
        System.out.println("Average: " + ((double)sum/iterations));
        System.out.println();
    }

    public static BasicTrainer getTrainerResults(List<Double> coefficients, List<Function<PlayerSkeleton.TestState, Double>> features, int numIterations){
        BasicTrainer trainer = new BasicTrainer(coefficients, features, numIterations);
        trainer.numIterations = numIterations;
        trainer.train();
        return trainer;
    }

    public double getAverage(){
        return (double)sum/iterations;
    }

    public int getPercentile(int num){
        Arrays.sort(result);
        return result[num*numIterations/100];
    }

    public void printAllResults(){
        System.out.println();
        for(int r: result){
            System.out.println(r);
        }
    }

    public static void main(String args[]) {
        List<Double> coefficients = Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 32.0, 0.0, 0.0, 0.0, 0.0, 0.5, 2.0, 32.0, 0.5, 0.0, 32.0, 0.0, 0.0, 88.0, -12.0, 0.0, 144.0, 4.0, 195.0, 96.0, 484.0);

        List<Function<PlayerSkeleton.TestState, Double>> features = new ArrayList<>();
        PlayerSkeleton.Features.addAllHeightDiffFeatures(features);
        PlayerSkeleton.Features.addAllColHeightFeatures(features);
        features.add(PlayerSkeleton.Features::getNegativeOfRowsCleared);
        features.add(PlayerSkeleton.Features::getMaxHeight);
        features.add(PlayerSkeleton.Features::getSumOfDepthOfHoles);
        features.add(PlayerSkeleton.Features::getMeanAbsoluteDeviationOfTop);
        features.add(PlayerSkeleton.Features::getBlocksAboveHoles);
        features.add(PlayerSkeleton.Features::getSignificantHoleAndTopDifferenceFixed);
        features.add(PlayerSkeleton.Features::getBumpiness);
        features.add(PlayerSkeleton.Features::getTotalHeight);

        BasicTrainer trainer =  new BasicTrainer(coefficients, features);
        trainer.train();
        trainer.printAllResults();
    }

}
