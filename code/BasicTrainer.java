import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BasicTrainer extends Trainer {

    final static int ITERATIONS_NUM =100;

    long sum;
    int iterations;
    int result[];

    public BasicTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this(coefficients, features, ITERATIONS_NUM);
    }

    public BasicTrainer(List<Double> coefficients, List<Function<TestState, Double>> features, int numIterations) {
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

    public static BasicTrainer getTrainer(List<Double> coefficients, List<Function<TestState, Double>> features, int numIterations){
        BasicTrainer trainer = new BasicTrainer(coefficients, features, numIterations);
        trainer.numIterations = numIterations;
        trainer.train();
        return trainer;
    }

    public double getAverage(){
        return (double)sum/iterations;
    }

    public int getFirstQuartile(){
        Arrays.sort(result);
        return result[numIterations/4];
    }

    public void printAllResults(){
        System.out.println();
        for(int r: result){
            System.out.println(r);
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
