import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BasicTrainer extends Trainer {

    long sum;
    int iterations;

    public BasicTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(100, coefficients, features);
        sum = 0;
        iterations = 0;
    }

    public void onSimulateDone(int rowsCleared) {
        iterations++;
        sum += rowsCleared;
        System.out.println("Iteration " + iterations);
        this.printCoefficients();
        System.out.println("Rows cleared: " + rowsCleared);
        System.out.println("Average: " + ((double)sum/iterations));
        System.out.println();
    }

    public static double getAverage(List<Double> coefficients, List<Function<TestState, Double>> features, int numIterations){
        BasicTrainer trainer = new BasicTrainer(coefficients, features);
        trainer.numIterations = numIterations;
        trainer.train();
        return (double) trainer.sum/trainer.iterations;
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
