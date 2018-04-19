import java.util.ArrayList;
import java.util.Arrays;
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
            int rowsCleared = new FairPlayer(coefficients, features).simulate();
            this.onSimulateDone(rowsCleared);
        }
    }

    public static void main(String[] args) {
        List<Function<TestState, Double>> features = new ArrayList<>();

        Features.addAllHeightDiffFeatures(features);
        Features.addAllColHeightFeatures(features);
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getSumOfDepthOfHoles);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifferenceFixed);
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        BasicFairTrainer trainer = BasicFairTrainer.getTrainerResults(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 32.0, 0.0, 0.0, 0.0, 0.0, 0.5, 2.0, 32.0, 0.5, 0.0, 32.0, 0.0, 0.0, 88.0, -12.0, 0.0, 144.0, 4.0, 195.0, 96.0, 484.0),features,100);
        System.out.println(trainer.getAverage());
        System.out.println(trainer.getPercentile(50));
    }

}
