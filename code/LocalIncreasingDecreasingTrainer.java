import java.util.ArrayList;
import java.util.Arrays;
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

    static final int ITERATIONS = 30;
    static final double STARTING_INCREMENT = 32;
    static final double EPSILON = 0.5;
    static final double factor = 4.0; // multiply increment by 1/factor after one iteration of the features
    static final boolean DECREASE_FLAG = true;

    int bestResult = Integer.MIN_VALUE;
    List<Double> bestCoefficient;
    int rounds;
    double increment;
    int[] resultsInRound;
    int direction;
    int order[];


    int currentCoefficient;

    Random random;

    public LocalIncreasingDecreasingTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(features.size()*50*100, coefficients, features);
        rounds = 0;
        resultsInRound = new int[ITERATIONS];
        bestCoefficient = new ArrayList<>(coefficients);
        increment = STARTING_INCREMENT;
        currentCoefficient = 0;
        direction = 1;
        random = new Random();
        randomOrder();
        //coefficients.set(currentCoefficient, coefficients.get(currentCoefficient)+increment);
    }

    /**return true if training has ended
     *
     * @param result
     * @return
     */
    public void onSimulateDone(int result) {
        rounds++;
        resultsInRound[rounds%ITERATIONS] = result;
        if(rounds%ITERATIONS == 0){
            rounds = 0;
            //print results
            System.out.println("cc " + currentCoefficient + " increment " + increment + " order[cc]" + order[currentCoefficient] );
            System.out.println("coefficients");
            for (double r : coefficients) System.out.print(r + " ");
            System.out.println();
            Arrays.sort(resultsInRound);
            System.out.println("Rows cleared");
            for(int r: resultsInRound) System.out.print(r + " ");
            System.out.println();

            int sum = 0;

            for (int i=ITERATIONS/4;i<ITERATIONS/4*3;i++){
                sum += resultsInRound[i];
                //penalise rounds with deaths
               // sum+=r[1] ==  Double.MAX_VALUE? r[0]/2 : r[0];
            }


            System.out.println("interquartile sum: "+ sum);

            if(sum > bestResult){
                bestResult = sum;
                bestCoefficient = new ArrayList<>(coefficients);
                coefficients.set(order[currentCoefficient], coefficients.get(order[currentCoefficient]) + increment);
            }
            else{

                currentCoefficient++;
                if(currentCoefficient% features.size() == 0){
                    randomOrder();
                    currentCoefficient = 0;
                    increment /= factor;
                }
                coefficients = new ArrayList<>(bestCoefficient);
                if(Math.abs(increment) < EPSILON) {
                    if(DECREASE_FLAG){
                        direction *= -1;
                    }
                    increment = direction * STARTING_INCREMENT;
                    System.out.println();
                    System.out.println("BEST");
                    System.out.println(bestResult);
                    for (int i = 0; i < coefficients.size(); i++) {
                        System.out.print(bestCoefficient.get(i) + " ");
                    }

                    System.out.println();
                    System.out.println();



                    bestResult = Integer.MIN_VALUE;

                    //perturbation
                    //TODO: perturbation strategy to be improved
                    if(direction == 1){
//                        System.out.println("PERTURBATION");
//                        for (int i = 0; i < coefficients.size(); i++) {
//                            bestCoefficient.set(i, bestCoefficient.get(i) + (1-2*(random.nextInt()%2))*STARTING_INCREMENT );
//                        }
//                        coefficients = new ArrayList<>(bestCoefficient);
                    }
                }
                else{

                    coefficients.set(order[currentCoefficient], coefficients.get(order[currentCoefficient]) + increment);
                }
            }


            System.out.println("Current best");
            System.out.println(bestResult);
            System.out.println("Best coefficients");
            for (double r : bestCoefficient) System.out.print(r + " ");
            System.out.println();
            System.out.println();

        }
    }

    private void randomOrder(){
        order = new int[features.size()];
        for(int i=0;i<features.size();i++){
            order[i] = i;
        }
        for(int i=features.size()-1;i>=1;i--){
            int swap = random.nextInt() % (i + 1);
            if(swap<0) swap*=-1;
            int temp = order[i];
            order[i] = order[swap];
            order[swap] = temp;

        }
    }

    public static void main(String args[]) {
        List<Double> coefficients = new ArrayList<>();

        List<Function<TestState, Double>> features = new ArrayList<>();

        features.add(Features::getBumpiness);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifference);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getNumColsWithHoles);
        features.add(Features::getNumRowsWithHoles);
        features.add(Features::getAggregateHoleAndWallMeasure);
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


        features.add(Features::getTotalHeight);
        coefficients.add(STARTING_INCREMENT);

        new LocalIncreasingDecreasingTrainer(coefficients, features).train();
    }

   public static void initialiseCoefficients(List<Double> coefficients, int size){
        for(int i=0;i<size;i++) coefficients.add(0.0);
    }
}
