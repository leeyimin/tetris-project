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
            this.onSimulateDone(rowsCleared);
        }
    }

    public int getAverageResult() {
        int rowsCleared = 0;

        for (int i = 0; i < this.numIterations; i++) {
            rowsCleared += new Player(coefficients, features).simulate();
        }
        return (rowsCleared/numIterations);
    }

    public void printCoefficients() {
        StringBuilder output = new StringBuilder();

        for (Double coefficient : coefficients) {
            output.append(coefficient + ", ");
        }

        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    public abstract void onSimulateDone(int rowsCleared);
}
