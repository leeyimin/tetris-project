import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Increases each coefficient until it stops improving, then move on to next coefficient. Lower the increment after one iteration of the features.
 * After the increment reaches below epsilon, it repeats the steps but decreasing each coefficient instead.
 *
 * Doubles allowed moves if previous best clears 95% of total possible lines
 *
 * Perturbation strategy to be improved.
 */
public class LocalIncreasingDecreasingTrainer extends Trainer{

    static final int ITERATIONS = 50;
    static final double STARTING_INCREMENT = 32;
    static final double EPSILON = 0.5;
    static final double factor = 4.0; // multiply increment by 1/factor after one iteration of the features
    static final int MOVE_FACTOR = 2;
    static final int STARTING_MOVES =1000;
    static final boolean DECREASE_FLAG = true;

    int bestResult = Integer.MIN_VALUE;
    List<Double> bestCoefficient;
    int rounds;
    double increment;
    int[] resultsInRound;
    int direction;
    int order[];

    int moves;

    long startTime;
    long lastUpdate;
    long interval = 15*60*1000;


    int currentCoefficient;

    Random random;

    public LocalIncreasingDecreasingTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(Integer.MAX_VALUE, coefficients, features);
        moves = STARTING_MOVES;
        rounds = 0;
        resultsInRound = new int[ITERATIONS];
        bestCoefficient = new ArrayList<>(coefficients);
        increment = STARTING_INCREMENT;
        currentCoefficient = 0;
        direction = 1;
        random = new Random();
        randomOrder();
        startTime = System.currentTimeMillis();
        lastUpdate = startTime;
        try {
            String filename = "LIDtrain" + startTime + ".txt";
            FileWriter fw = new FileWriter(filename, true); //the true will append the new data
            fw.write("iterations: " + ITERATIONS + "\n");
            fw.write("max moves: " + moves + "\n");
            fw.write("increment: " + STARTING_INCREMENT + "\n");
            fw.write("epsilon: " + EPSILON + "\n");
            fw.write("\n");
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        //coefficients.set(currentCoefficient, coefficients.get(currentCoefficient)+increment);
    }

    @Override
    public void train() {
        for (int i = 0; i < this.numIterations; i++) {
            int rowsCleared = new Player(coefficients, features).simulate(moves);
            this.onSimulateDone(rowsCleared);
        }
    }

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

            for (int i=0;i<ITERATIONS;i++){
                sum += resultsInRound[i];
                //penalise rounds with deaths
               // sum+=r[1] ==  Double.MAX_VALUE? r[0]/2 : r[0];
            }


            System.out.println("sum: "+ sum);

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

                    boolean increaseLines = false;

                    if(bestResult >= 0.95*(moves*4/10)*ITERATIONS){
                        moves *= MOVE_FACTOR;
                        increaseLines = true;
                    }

                    // do file writing
                    if(System.currentTimeMillis() - lastUpdate >  interval || increaseLines){
                        lastUpdate = System.currentTimeMillis();
                        try {
                            String filename = "LIDtrain" + startTime+".txt";
                            FileWriter fw = new FileWriter(filename, true); //the true will append the new data

                            if (increaseLines) fw.write("max moves: " + moves + "\n");
                            fw.write("time: " + (lastUpdate - startTime)/(60*1000.0) + "\n");
                            fw.write("sum: " + bestResult + "\n");
                            fw.write("average over 100: " + BasicTrainer.getAverage(bestCoefficient, features, 100)+"\n");
                            fw.write(bestCoefficient.get(0) +"");
                            for (int i = 1; i < coefficients.size(); i++) {
                                fw.write(", "+ bestCoefficient.get(i));
                            }
                            fw.write("\n");
                            fw.close();
                        } catch (IOException ioe) {
                            System.err.println("IOException: " + ioe.getMessage());
                        }



                    }


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

        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getSumOfDepthOfHoles);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getSignificantHoleAndTopDifference);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::hasLevelSurface);

        features.add(Features::getNumColsWithHoles);
        features.add(Features::getNumRowsWithHoles);

        Features.addAllColHeightFeatures(features);
        Features.addAllHeightDiffFeatures(features);

        initialiseCoefficients(coefficients, features.size());

        features.add(Features::getBumpiness);
        coefficients.add(STARTING_INCREMENT/10.0);
        features.add(Features::getTotalHeight);
        coefficients.add(STARTING_INCREMENT);

        new LocalIncreasingDecreasingTrainer(coefficients, features).train();
    }

   public static void initialiseCoefficients(List<Double> coefficients, int size){
        for(int i=0;i<size;i++) coefficients.add(0.0);
    }
}
