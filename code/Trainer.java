import java.util.*;
import java.util.function.*;

public abstract class Trainer {

    protected List<Double> coefficients;
    protected List<Function<TestState, Double>> features;
    protected int numIterations;

    public Trainer(int numIterations, List<Double> coefficients, List<Function<TestState, Double>> features) {
        this.coefficients = coefficients;
        this.features = features;
        this.numIterations = numIterations;
    }

    public void train() {
        for (int i = 0; i < this.numIterations; i++) {
            int rowsCleared = new Player(coefficients, features).simulate();
            this.update(rowsCleared);
        }
    }

    public void printCoefficients() {
        StringBuilder output = new StringBuilder();

        for (Double coefficient : coefficients) {
            output.append(coefficient + ", ");
        }

        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    public abstract void update(int rowsCleared);
}
