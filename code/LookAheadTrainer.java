import java.util.List;
import java.util.function.Function;

public abstract class LookAheadTrainer extends Trainer{

    public LookAheadTrainer(int numIterations, List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(numIterations, coefficients, features);
    }

    public void train() {
        for (int i = 0; i < this.numIterations; i++) {
            int rowsCleared = new WorstCaseLookAheadPlayer(coefficients, features).simulate();
            this.onSimulateDone(rowsCleared);
        }
    }
}
