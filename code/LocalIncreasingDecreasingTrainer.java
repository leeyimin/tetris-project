import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Increases each coefficient until it stops improving, then move on to next coefficient. Lower the increment after one iteration of the features.
 * After the increment reaches below epsilon, it repeats the steps but decreasing each coefficient instead.
 *
 * Perturbation strategy to be improved.
 */
public class LocalIncreasingDecreasingTrainer extends Trainer{

    static final int ITERATIONS = 200;
    static final double STARTING_INCREMENT = 16;
    static final double EPSILON = 1.0;
    static final double factor = 4.0; // multiply increment by 1/factor after one iteration of the features
    static final int MOVES = 300;

    double bestResult = Double.MIN_VALUE;
    List<Double> bestCoefficient;
    int rounds;
    double increment;
    double[][] resultsInRound;
    int direction;


    int currentCoefficient;

    Random random;

    public LocalIncreasingDecreasingTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(features.size()*50*100, coefficients, features);
        rounds = 0;
        resultsInRound = new double[ITERATIONS][2];
        bestCoefficient = new ArrayList<>(coefficients);
        increment = STARTING_INCREMENT;
        currentCoefficient = 0;
        direction = 1;
        random = new Random();
        //coefficients.set(currentCoefficient, coefficients.get(currentCoefficient)+increment);
    }

    public void train(){
        for (int i = 0; i < this.numIterations; i++) {
            double[] results = new Player(coefficients, features).simulate(MOVES);
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
                //penalise rounds with deaths
                sum+=r[1] ==  Double.MAX_VALUE? r[0]/2 : r[0];
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
                    increment /= factor;
                }
                if(Math.abs(increment) < EPSILON) {
                    direction *= -1;
                    increment = direction * STARTING_INCREMENT;
                    System.out.println();
                    System.out.println("BEST");
                    System.out.println(bestResult);
                    for (int i = 0; i < coefficients.size(); i++) {
                        System.out.print(bestCoefficient.get(i) + " ");
                    }

                    System.out.println();
                    System.out.println();



                    bestResult = Double.MIN_VALUE;

                    //perturbation
                    //TODO: perturbation strategy to be improved
                    if(direction == 1){
                        return true;
//                        System.out.println("PERTURBATION");
//                        for (int i = 0; i < coefficients.size(); i++) {
//                            bestCoefficient.set(i, bestCoefficient.get(i) + (1-2*(random.nextInt()%2))*STARTING_INCREMENT );
//                        }
//                        coefficients = new ArrayList<>(bestCoefficient);
                    }
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
        features.add(Features::getNumColsWithHoles);
        features.add(Features::getNumRowsWithHoles);
        features.add(Features::getFirstColHeight);
        features.add(Features::getSecondColHeight);
        features.add(Features::getThirdColHeight);
        features.add(Features::getFourthColHeight);
        features.add(Features::getFifthColHeight);
        features.add(Features::getSixthColHeight);
        features.add(Features::getSeventhColHeight);
        features.add(Features::getEighthColHeight);
        features.add(Features::getNinthColHeight);
        features.add(Features::getTenthColHeight);

        initialiseCoefficients(coefficients, features.size());

        new LocalIncreasingDecreasingTrainer(coefficients, features).train();
    }

   public static void initialiseCoefficients(List<Double> coefficients, int size){
        for(int i=0;i<size;i++) coefficients.add(0.0);
    }
}
