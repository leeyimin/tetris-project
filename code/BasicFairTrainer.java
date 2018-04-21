import java.util.List;
import java.util.function.Function;

public class BasicFairTrainer extends BasicTrainer {

    public BasicFairTrainer(List<Double> coefficients, List<Function<TestState, Double>> features, int numIterations) {
        super(coefficients, features, numIterations);
    }

    public static BasicFairTrainer getTrainerResults(List<Double> coefficients, List<Function<TestState, Double>> features, int numIterations){
        BasicFairTrainer trainer = new BasicFairTrainer(coefficients, features, numIterations);
        trainer.numIterations = numIterations;
        trainer.train();
        return trainer;
    }

    @Override
    public void train() {
        for (int i = 0; i < this.numIterations; i++) {
            int rowsCleared = new PlayerSkeleton(coefficients, features).simulate();
            this.onSimulateDone(rowsCleared);
        }
    }

}
