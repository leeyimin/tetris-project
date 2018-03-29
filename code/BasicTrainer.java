import java.util.*;
import java.util.function.*;

public class BasicTrainer extends Trainer {

    public BasicTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(100, coefficients, features);
    }

    public void onSimulateDone(int rowsCleared) {
        this.printCoefficients();
        System.out.println("Rows cleared: " + rowsCleared);
        System.out.println();
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
