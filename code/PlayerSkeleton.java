import java.util.List;
import java.util.function.Function;

public abstract class PlayerSkeleton {

    private static final int REFRESH_DELAY = 1;
    private static final boolean RENDER_BOARD = false;
    private static final boolean SHOW_DEATH_STATE = false;
    private static final int MAX_NUM_MOVES = Integer.MAX_VALUE;

    private TFrame frame;

    private List<Double> coefficients;
    private List<Function<TestState, Double>> features;
    private State currentState;

    private int numMoves = 0;

    public PlayerSkeleton(List<Double> coefficients, List<Function<TestState, Double>> features) {
        this.coefficients = coefficients;
        this.features = features;
        this.state = new State();
    }

    public int pickMove(int[][] legalMoves);

    public int run() {
        if (RENDER_BOARD) {
            this.frame = new TFrame(state);
        }

        while (!state.hasLost() && numMoves < maxMoves) {
            numMoves++;
            state.makeMove(this.pickMove(state, state.legalMoves()));
            updateBoard(state);
        }

        showDeathState(state);

        if (RENDER_BOARD) {
            this.frame.dispose();
        }

        return state.getRowsCleared();
    }

    private void updateBoard(State state) {
        if (RENDER_BOARD) {
            state.draw();
            state.drawNext(0,0);
            try {
                Thread.sleep(REFRESH_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDeathState(State state){
        int field[][] = state.getField();

        if( SHOW_DEATH_STATE && state.hasLost()){
            System.out.println();
            for(int i=State.ROWS-1;i>=0;i--){
                for(int j=0;j< State.COLS;j++){
                    System.out.print(field[i][j] == 0?" ": "X");
                }
                System.out.println();
            }
        }
    }
}
