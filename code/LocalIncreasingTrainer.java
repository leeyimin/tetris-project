import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Increases each coefficient until it stops improving, then move on to next coefficient. Lower the increment after one iteration of the features.
 * Perturbation strategy to be improved.
 */
public class LocalIncreasingTrainer extends Trainer{

    static final int ITERATIONS = 50;

    double bestResult = Double.MIN_VALUE;
    List<Double> bestCoefficient;
    int rounds;
    double increment = 16;
    double[][] resultsInRound;

    int currentCoefficient;

    public LocalIncreasingTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(10000, coefficients, features);
        rounds = 0;
        resultsInRound = new double[ITERATIONS][2];
        bestCoefficient = new ArrayList<>(coefficients);
        currentCoefficient = 0;
        //coefficients.set(currentCoefficient, coefficients.get(currentCoefficient)+increment);
    }

    public void train(){
        for (int i = 0; i < this.numIterations; i++) {
            double[] results = new Player(coefficients, features).simulate(1000);
            if(this.onSimulateDone(results)) break;
        }
    }

    @Override
    public void onSimulateDone(int rowsCleared) {
        // not implemented
    }

    /**return true if training has ended
     *
     * @param result
     * @return
     */
    public boolean onSimulateDone(double[] result) {
        rounds++;
        resultsInRound[rounds%ITERATIONS] = result;
        if(rounds%ITERATIONS == 0){
            rounds = 0;
            //print results
            System.out.println("coefficients");
            for (double r : coefficients) System.out.print(r + " ");
            System.out.println();
            System.out.println("Rows cleared");
            for(double[] r: resultsInRound) System.out.print((int)r[0] + " ");
            System.out.println();
            System.out.println("Cost");
            for (double[] r : resultsInRound) System.out.print((int) r[1] + " ");
            System.out.println();

            double sum = 0;

            for (double[] r : resultsInRound){
                sum+=r[0];
            }

            if(sum > bestResult){
                bestResult = sum;
                bestCoefficient = new ArrayList<>(coefficients);
                coefficients.set(currentCoefficient, coefficients.get(currentCoefficient) + increment);
            }
            else{

                currentCoefficient++;
                if(currentCoefficient% features.size() == 0){
                    currentCoefficient = 0;
                    increment /=2;
                }
                if(increment < 1) {
                    increment = 16;
                    System.out.println();
                    System.out.println("BEST");
                    System.out.println(bestResult);

                    for(int i =0;i<coefficients.size(); i++) {
                        System.out.print(bestCoefficient.get(i) + " ");
                        bestCoefficient.set(i, bestCoefficient.get(i)/2);
                    }

                    System.out.println();
                    System.out.println();

                        return true;
                    // perturbation strategy to be improved
//                    bestResult = Double.MIN_VALUE;
//                    coefficients = new ArrayList<>(bestCoefficient);
                }
                else{

                    coefficients = new ArrayList<>(bestCoefficient);
                    coefficients.set(currentCoefficient, coefficients.get(currentCoefficient) + increment);
                }
            }


            System.out.println("Current best");
            System.out.println(bestResult);
            System.out.println("Best coefficients");
            for (double r : bestCoefficient) System.out.print(r + " ");
            System.out.println();
            System.out.println();

        }
        return false;
    }

    public static void main(String args[]) {
        List<Double> coefficients = new ArrayList<>();

        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);
        coefficients.add(0.0);

        List<Function<TestState, Double>> features = new ArrayList<>();
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::hasLevelSurface);
        features.add(Features::hasRightStep);
        features.add(Features::hasLeftStep);
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::hasPossibleDeathNextPiece);
        features.add(Features::getBlocksAboveHoles);

        new LocalIncreasingTrainer(coefficients, features).train();
    }
}
