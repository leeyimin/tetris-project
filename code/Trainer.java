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
            new Player(coefficients, features).simulate();
            this.update();
        }
    }

    public abstract void update();
}
