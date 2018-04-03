import java.util.function.Function;

public class PlayerSkeleton {

    private int refreshDelay;
    private TFrame frame;

    protected Function<State, Integer> strategy;
    protected State state;

    public PlayerSkeleton(Function<State, Integer> strategy, int refreshDelay) {
        this.refreshDelay = refreshDelay;
        this.strategy = strategy;
        this.state = new State();
    }

    public PlayerSkeleton(Function<State, Integer> strategy) {
        this(strategy, 1);
    }

    public int run() {
        this.initializeFrame();
        while (!this.state.hasLost()) {
            int move = this.strategy.apply(this.state);
            this.state.makeMove(move);
            this.updateFrame();
        }
        this.destroyFrame();
        return this.state.getRowsCleared();
    }

    private void initializeFrame() {
        if (this.refreshDelay > 0) {
            this.frame = new TFrame(this.state);
        }
    }

    private void destroyFrame() {
        if (this.frame != null) {
            this.frame.dispose();
        }
    }

    private void updateFrame() {
        if (this.frame != null) {
            this.state.draw();
            this.state.drawNext(0,0);

            try {
                Thread.sleep(this.refreshDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
