package trainer;

import player.PlayerSkeleton;
import player.State;
import player.TFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TwoStrategyPlayer extends PlayerSkeleton {

    public static final int CRITICAL_TOTAL_HEIGHT = 150;
    public static final int RETURN_TOTAL_HEIGHT = 60;
    private boolean isCritical;
    State startingState;

    private PlayerSkeleton criticalPlayer;


    public TwoStrategyPlayer(List<Double> coefficients, List<Function<TestState, Double>> features,
                             List<Double> criticalCoefficients, List<Function<TestState, Double>> criticalFeatures) {
        super(coefficients, features);
        criticalPlayer = new PlayerSkeleton(criticalCoefficients, criticalFeatures);
        isCritical = false;
    }

    public TwoStrategyPlayer(List<Double> coefficients, List<Function<TestState, Double>> features,
                             List<Double> criticalCoefficients, List<Function<TestState, Double>> criticalFeatures, State startingState) {
        this(coefficients, features, criticalCoefficients, criticalFeatures);
        this.startingState = startingState;
    }

    public void updateCritical(State state){
        TestState t = new TestState(state.getField(), state.getTop(), 0);
        if(!isCritical){
            if(Features.getTotalHeight(t) >= CRITICAL_TOTAL_HEIGHT) isCritical = true;
        }
        else{
            if(Features.getTotalHeight(t) < RETURN_TOTAL_HEIGHT) isCritical = false;
        }
    }

    @Override
    public int pickMove(State currentState, int[][] legalMoves) {
        updateCritical(currentState);
        if(isCritical){
            return criticalPlayer.pickMove(currentState, legalMoves);
        }
        else return super.pickMove(currentState, legalMoves);
    }

    public State simulateToCritical(){
        State state = new State();

        while (!isCritical) {
            state.makeMove(pickMove(state, state.legalMoves()));
            updateCritical(state);
        }

        return state;
    }

    /**
     *
     * @param moves
     * @return 1 if not critical, 0 if still critical or lost
     */
    public int simulateToNotCritical(int moves){
        State state = new State();
        initState(state);


        if (RENDER_BOARD) {
            this.frame = new TFrame(state);
        }
        updateCritical(state);
        while (!state.hasLost() && numMoves < moves && isCritical) {
            numMoves++;
            state.makeMove(pickMove(state, state.legalMoves()));

            updateBoard(state);
            updateCritical(state);
        }

        if (RENDER_BOARD) {
            this.frame.dispose();
        }
        if(state.hasLost()) return 0;
        if(isCritical) return 0;
        return 1;
    }

    private void initState(State state) {
        if (startingState != null) {
            int sField[][] = state.getField();
            for (int i = 0; i < State.ROWS; i++) {
                System.arraycopy(startingState.getField()[i], 0, sField[i], 0, State.COLS);
            }

            System.arraycopy(startingState.getTop(), 0, state.getTop(), 0, State.COLS);
        }
    }


    @Override
    public int simulate(int maxMoves) {
        State state = new State();
        initState(state);

        if (RENDER_BOARD) {
            this.frame = new TFrame(state);
        }

        while (!state.hasLost() && numMoves < maxMoves) {
            numMoves++;
            state.makeMove(pickMove(state, state.legalMoves()));
            updateBoard(state);
            writeLog(state);
        }

        printDeathState(state);

        if (RENDER_BOARD) {
            this.frame.dispose();
        }

        return state.getRowsCleared();
    }

    public static double getAverage(List<Double> coefficients, List<Function<TestState, Double>> features,
                                    List<Double> criticalCoefficients, List<Function<TestState, Double>> criticalFeatures, int iterations){
        long result = 0;
        for(int i=0;i<iterations;i++){
            result += new TwoStrategyPlayer(coefficients, features, criticalCoefficients, criticalFeatures).simulate();
        }
        return result/(double) iterations;
    }

    public static void main(String[] args) {

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

        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        for(int i=0;i<100;i++)
            System.out.println((new TwoStrategyPlayer(
                    Arrays.asList(6.0, 0.0, -10.0, 0.0, 0.5, 16.0, 0.0, 0.0, 38.0, -4.0, 0.0, 0.0, -2.0, 0.5, -2.0, 2.0, 0.0, 0.0, 2.0, -0.5, 0.0, -2.0, 2.0, 0.0, 2.0, 0.0, 0.0, 0.0, 2.0, -0.5, 0.0, 3.2, 44.0),
                    features,
                    Arrays.asList(14.0, 0.0, -10.0, 0.0, 2.5, 16.0, 0.0, 0.0, 38.0, -4.0, 0.0, 0.0, -2.0, 0.5, -2.0, 2.0, 0.0, 0.0, 2.0, -0.5, 0.0, -2.0, 2.0, 0.0, 2.0, 0.0, 2.0, 0.0, 2.0, -0.5, 2.0, 3.2, 44.0),
                    features)).simulate());
    }
}
