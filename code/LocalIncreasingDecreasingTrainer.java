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
 * <p>
 * Doubles allowed moves if previous best clears 95% of total possible lines
 * <p>
 * Perturbation strategy to be improved.
 */
public class LocalIncreasingDecreasingTrainer extends Trainer {

    static final int STARTING_ITERATIONS = 50;
    static final double STARTING_INCREMENT = 32;
    static final double EPSILON = 0.5;
    static final double factor = 4.0; // multiply increment by 1/factor after one iteration of the features
    static final int IT_INCREMENT = 5;
    static final int STARTING_MOVES = 1000;
    static final boolean DECREASE_FLAG = true;
    static final double PASS_MARK = 0.95;
    static final int TARGET_PERCENTILE = 25;

    //static final String folder = "data/local-increasing-trainer-v1/";
    static final String folder = "";

    int bestResult = Integer.MIN_VALUE;
    List<Double> bestCoefficient;
    int rounds;
    double increment;
    int[] resultsInRound;
    int rSum;
    int direction;
    int order[];

    List<Double> backupBest;
    double backupBestAverage;


    int moveIncrement = 1000;

    int moves;
    int iterations;

    long startTime;
    long lastUpdate;
    long interval = 15 * 60 * 1000;

    int currentCoefficient;

    Random random;

    public LocalIncreasingDecreasingTrainer(List<Double> coefficients, List<Function<TestState, Double>> features) {
        super(Integer.MAX_VALUE, coefficients, features);
        moves = STARTING_MOVES;
        rounds = 0;

        iterations = STARTING_ITERATIONS;
        resultsInRound = new int[iterations];
        bestCoefficient = new ArrayList<>(coefficients);
        increment = STARTING_INCREMENT;
        currentCoefficient = 0;
        direction = 1;
        random = new Random();
        randomOrder();
        startTime = System.currentTimeMillis();
        lastUpdate = startTime;
        rSum = 0;

        backupBest = new ArrayList<>(coefficients);
        backupBestAverage = Double.MIN_VALUE;

        try {
            String filename = folder + "LIDtrain" + startTime + ".txt";
            FileWriter fw = new FileWriter(filename, true); //the true will append the new data
            fw.write("iterations: " + iterations + "\n");
            fw.write("max moves: " + moves + "\n");
            fw.write("increment: " + STARTING_INCREMENT + "\n");
            fw.write("epsilon: " + EPSILON + "\n");
            fw.write("\n");
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
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
        resultsInRound[(rounds-1) % iterations] = result;
        rSum+=result;
        if (rounds % iterations == 0 || rSum + (iterations - rounds)*moves*4/10.0 < bestResult) {
            printCurrentRound();

            rounds = 0;

            if (rSum > bestResult) {
                bestResult = rSum;
                bestCoefficient = new ArrayList<>(coefficients);
                coefficients.set(order[currentCoefficient], coefficients.get(order[currentCoefficient]) + increment);
            }
            else {
                currentCoefficient++;

                updateNextRoundIfNecessary();

                coefficients = new ArrayList<>(bestCoefficient);

                if ( (currentCoefficient == 0 && shouldModifyTargetLines()) ||  Math.abs(increment) < EPSILON) {

                    updateNextCycle();

                } else {

                    coefficients.set(order[currentCoefficient], coefficients.get(order[currentCoefficient]) + increment);
                }
            }

            rSum = 0;
            printCurrentBest();

        }
    }

    private void printCurrentBest() {
        System.out.println("Current best");
        System.out.println(bestResult);
        System.out.println("Best coefficients");
        for (double r : bestCoefficient) System.out.print(r + " ");
        System.out.println();
        System.out.println();
    }

    /**
     * prepare for next cycle through all increments and features.
     */
    private void updateNextCycle() {
        printBest();

        boolean toPerturb = updateBackupBestAndParameters();

        bestResult = Integer.MIN_VALUE;

        //perturbation
        //TODO: perturbation strategy to be improved
        if(toPerturb) perturb();
    }

    private void perturb() {
        int countNonZero = 0;
        for(double r: bestCoefficient){
            if(r!=0) countNonZero++;
        }
        int toZero = random.nextInt()%countNonZero;
        for(int i=0;i<bestCoefficient.size();i++){
            if(bestCoefficient.get(i)== 0) continue;
            if(toZero == 0){
                bestCoefficient.set(i,0.0);
                break;
            }
            else toZero--;
        }
    }

    /**
     * return if perturbation should be performed.
     * @return
     */
    private boolean updateBackupBestAndParameters() {

        if(System.currentTimeMillis() - lastUpdate < interval && !shouldModifyTargetLines()){
            modifyParameters(moves);
            return false;
        }

        boolean shouldPerturb = false;
        lastUpdate = System.currentTimeMillis();

        BasicTrainer trainer = BasicTrainer.getTrainerResults(bestCoefficient, features, 100);
        double currAverage = trainer.getAverage();

        if(bestCoefficient.equals(backupBest)){
            backupBestAverage = currAverage = (trainer.getAverage() + backupBestAverage) / 2;
            modifyParameters(((trainer.getPercentile(TARGET_PERCENTILE) * 10 / 4) + moves) / 2);
            shouldPerturb = true;
        }
        else if(currAverage> backupBestAverage){
            backupBestAverage = currAverage;
            backupBest = new ArrayList<>(bestCoefficient);
            modifyParameters(trainer.getPercentile(TARGET_PERCENTILE) * 10 / 4);

        }
        else{
            BasicTrainer retest = BasicTrainer.getTrainerResults(backupBest, features, 100);
            if(currAverage > (retest.getAverage()+backupBestAverage)/2){
                backupBestAverage = currAverage;
                backupBest = new ArrayList<>(bestCoefficient);
                modifyParameters(trainer.getPercentile(TARGET_PERCENTILE) * 10 / 4);
            }
            else{

                bestCoefficient = new ArrayList<>(backupBest);
                backupBestAverage = currAverage = (retest.getAverage() + backupBestAverage) / 2;
                modifyParameters(((retest.getPercentile(TARGET_PERCENTILE) * 10 / 4) + moves)/2 );
                shouldPerturb = true;

            }
        }

        printLog(currAverage);
        return shouldPerturb;
    }

    /**
     * prepares for next round of features
     */
    private void updateNextRoundIfNecessary() {
        if (currentCoefficient % features.size() == 0) {
            randomOrder();
            currentCoefficient = 0;
            increment /= factor;
        }
    }

    /**
     * updates increments, moves and iterations
     * @return if moves and iterations are updated
     */
    private boolean modifyParameters(int moves) {

        if (DECREASE_FLAG) {
            direction *= -1;
        }
        increment = direction * STARTING_INCREMENT;

        this.moves = moves;
        iterations = Math.max(50, moves/500);

        resultsInRound = new int[iterations];

        return false;
    }

    private boolean shouldModifyTargetLines(){
        return bestResult >= PASS_MARK * (moves * 4 / 10) * iterations;
    }

    private void printLog(double average) {
        // do file writing

        try {
            String filename = folder + "LIDtrain" + startTime + ".txt";
            FileWriter fw = new FileWriter(filename, true); //the true will append the new data
            fw.write("time: " + (lastUpdate - startTime) / (60 * 1000.0) + "\n");
            fw.write("sum: " + bestResult + "\n");

            fw.write("average over 100: " + average + "\n");
            fw.write(bestCoefficient.get(0) + "");
            for (int i = 1; i < coefficients.size(); i++) {
                fw.write(", " + bestCoefficient.get(i));
            }
            fw.write("\n\n");


            fw.write("max moves: " + moves + "\n");
            fw.write("iterations: " + iterations + "\n");
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    private void printBest() {
        System.out.println();
        System.out.println("BEST");
        System.out.println(bestResult);
        for (int i = 0; i < coefficients.size(); i++) {
            System.out.print(bestCoefficient.get(i) + " ");
        }

        System.out.println();
        System.out.println();
    }

    /**
     * prints statistics for current round
     */
    private void printCurrentRound() {
        //print results
        System.out.println("cc " + currentCoefficient + " increment " + increment + " order[cc]" + order[currentCoefficient]);
        System.out.println("coefficients");
        for (double r : coefficients) System.out.print(r + " ");
        System.out.println();
        Arrays.sort(resultsInRound, 0, rounds);

        System.out.println("Rows cleared");
        for (int i = 0; i < rounds; i++) {
            System.out.print(resultsInRound[i] + " ");
        }

        System.out.println();


        System.out.println("avg: " + 1.0*rSum/rounds);
        System.out.println("sum: " + rSum );
    }

    private void randomOrder() {
        order = new int[features.size()];
        for (int i = 0; i < features.size(); i++) {
            order[i] = i;
        }
        for (int i = features.size() - 1; i >= 1; i--) {
            int swap = random.nextInt() % (i + 1);
            if (swap < 0) swap *= -1;
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
        features.add(Features::getHeightAboveHoles);
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
        coefficients.add(STARTING_INCREMENT / 10.0);
        features.add(Features::getTotalHeight);
        coefficients.add(STARTING_INCREMENT);

        new LocalIncreasingDecreasingTrainer(coefficients, features).train();
    }

    public static void initialiseCoefficients(List<Double> coefficients, int size) {
        for (int i = 0; i < size; i++) coefficients.add(0.0);
    }
}
