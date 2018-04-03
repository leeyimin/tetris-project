import java.util.*;
import java.util.function.*;

public abstract class Trainer {

    protected Function<State, Integer> strategy;

    public Trainer(Function<State, Integer> strategy) {
        this.strategy = strategy;
    }

    public void train() {
        while (true) {
            rowsCleared += new PlayerSkeleton(this.strategy).run();
            this.onRunFinished(rowsCleared);
        }
    }

    public void printCoefficients() {
        StringBuilder output = new StringBuilder();
        for (Double coefficient : coefficients) {
            output.append(coefficient + ", ");
        }
        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    public abstract void onSimulateDone(double averageRowsCleared);
}
