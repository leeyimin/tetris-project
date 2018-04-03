import java.util.*;
import java.util.function.*;

public class LocalTrainer {

    private static final int BATCH_SIZE = 10;
    private static final double LEARNING_RATE = 0.25;

    private StrategyFactory strategyFactory;
    private Function<State, Integer> strategy;
    private List<Double> currCoefficients;
    private List<Double> bestCoefficients;

    private int currBatch;
    private int currIteration;
    private int currRowsCleared;
    private int bestRowsCleared; 

    public LocalTrainer(List<Double> coefficients, List<Function<State, Double>> features) {
        this.strategyFactory = new StrategyFactory(features);
        this.strategy = this.strategyFactory.createGreedyStrategy(coefficients);
        this.currCoefficients = coefficients;
        this.bestCoefficients = coefficients;
        this.currBatch = 0;
        this.currIteration = 0;
        this.currRowsCleared = 0;
        this.bestRowsCleared = 0;
    }

    public void train() {
        while (true) {
            int rowsCleared = new PlayerSkeleton(this.strategy).run();
            this.onRunFinished(rowsCleared);
        }
    }

    public void onRunFinished(int rowsCleared) {
        this.currIteration++;
        this.currRowsCleared += rowsCleared;

        if (this.currIteration % BATCH_SIZE == 0) {
            this.printSummary();
            this.updateBest();
            this.pertubateCoefficients();
            this.currIteration = 0;
            this.currRowsCleared = 0;
        }
    }

    private void printSummary() {
        System.out.println();
        System.out.println("======================================");
        System.out.println(" RESULT OF BATCH #" + (++currBatch));
        System.out.println("======================================");
        System.out.println("Rows cleared: " + (double) this.currRowsCleared / BATCH_SIZE);
        this.printCoefficients();
    }

    public void printCoefficients() {
        StringBuilder output = new StringBuilder();
        for (Double coefficient : this.currCoefficients) {
            output.append(coefficient + ", ");
        }
        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    private void updateBest() {
        if (this.bestRowsCleared < this.currRowsCleared) {
            this.bestRowsCleared = this.currRowsCleared;
            this.bestCoefficients = this.currCoefficients;
        }
    }

    private void pertubateCoefficients() {
        Random rng = new Random();
        List<Double> newCoefficients = new ArrayList<>();
        for (Double coefficient : this.bestCoefficients) {
            newCoefficients.add(Math.abs(coefficient + LocalTrainer.LEARNING_RATE * (rng.nextDouble() - LocalTrainer.LEARNING_RATE / 2)));
        }
        this.currCoefficients = newCoefficients;
        this.strategy = this.strategyFactory.createGreedyStrategy(newCoefficients);
    }

    public static void main(String args[]) {
        List<Double> coefficients = new ArrayList<>();
        List<Function<State, Double>> features = new ArrayList<>();

        coefficients.add(2.80764828782628);
        coefficients.add(9.52205211768384);
        coefficients.add(1.16540827653564);
        coefficients.add(0.11757694915822);

        /*
        coefficients.add(2.0);
        coefficients.add(2.0);
        coefficients.add(2.0);
        coefficients.add(2.0);
        */

        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);

        new LocalTrainer(coefficients, features).train();
    }

}
