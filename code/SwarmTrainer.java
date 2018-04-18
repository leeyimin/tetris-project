import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class SwarmTrainer {

    public static final int STARTING_MOVES = 1000;
    public static final int BATCH_SIZE = 20;
    public static final int SWARM_SIZE = 20;
    public static final double PHI_P = 1;
    public static final double PHI_G = 1;
    public static final double OMEGA = 1;

    private List<BiFunction<TestableState, TestableState, Integer>> features;
    private List<Particle> swarm;
    private int numMoves;

    private List<Double> globalBestPosition;
    private int globalBestFitness;

    public SwarmTrainer(List<BiFunction<TestableState, TestableState, Integer>> features) {
        this.features = features;
        this.swarm = Collections.synchronizedList(new ArrayList<>());
        this.numMoves = STARTING_MOVES;
        this.globalBestPosition = new ArrayList<>();
        this.globalBestFitness = Integer.MIN_VALUE;
    }

    public void train() {
        this.initializeSwarm();
        while (true) {
            this.evolveSwarm();
            this.updateParam();
            this.printSummary();
        }
    }

    private void initializeSwarm() {
        for (int i = 0; i < SWARM_SIZE; i++) {
            Particle particle = new Particle(this.features.size());
            this.swarm.add(particle);
        }
    }

    private void evolveSwarm() {
        this.swarm
            .stream()
            .unordered()
            .parallel()
            .forEach((Particle p) -> {
                this.evaluateParticle(p);
                System.out.print(".");
            });

        System.out.println();    
        System.out.println();    
        Collections.sort(this.swarm);
    }

    private void updateParam() {
        double firstRowsCleared = (double) this.swarm.get(0).fitness / BATCH_SIZE;
        double secondRowsCleared = (double) this.swarm.get(1).fitness / BATCH_SIZE;
        double thirdRowsCleared = (double) this.swarm.get(2).fitness / BATCH_SIZE;
        double theoreticalMaxRowsCleared = this.numMoves * 0.4;
        double threshold = 1.0 * theoreticalMaxRowsCleared;

        if (firstRowsCleared > threshold && secondRowsCleared > threshold && thirdRowsCleared > threshold) {
            this.numMoves *= 2;
        }

        if (this.swarm.get(0).fitness > this.globalBestFitness) {
            this.globalBestFitness = this.swarm.get(0).fitness;
            this.globalBestPosition = this.swarm.get(0).position;
        }

        for (Particle p : this.swarm) {
            p.move(this.globalBestPosition);
        }
    }

    private void evaluateParticle(Particle particle) {
        int totalRowsCleared = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            totalRowsCleared += new Player(particle.position, features).simulate(this.numMoves);
        }
        particle.updateFitness(totalRowsCleared);
    }

    private void printSummary() {
        System.out.println("========");
        System.out.println(" RESULT ");
        System.out.println("========");
        System.out.println();
        this.swarm.get(0).print();
        System.out.println();
        this.swarm.get(1).print();
        System.out.println();
        this.swarm.get(2).print();
        System.out.println();
    }


    public static void main(String args[]) {
        List<BiFunction<TestableState, TestableState, Integer>> features = new ArrayList<>();
        Features.addAllFeatures(features);

        new GeneticTrainer(features).train();
    }

}

class Particle implements Comparable<Particle> {

    public List<Double> position;
    public List<Double> velocity;
    public int fitness;

    public List<Double> bestPosition;
    public int bestFitness;

    public Particle(int numCoefficients) {
        this.position = new ArrayList<>();
        this.velocity = new ArrayList<>();
        this.fitness = 0;

        Random rng = new Random();
        for (int i = 0; i < numCoefficients; i++) {
            this.position.add(rng.nextDouble() * 2 - 1);
            this.velocity.add(rng.nextDouble() * 2 - 1);
        }

        this.bestPosition = this.position;
        this.bestFitness = this.fitness;
    }

    public void updateFitness(int fitness) {
        this.fitness = fitness;
        if (this.fitness > this.bestFitness) {
            this.bestFitness = this.fitness;
            this.bestPosition = this.position;
        }
    }

    public void move(List<Double> globalBestPosition) {
        List<Double> newPosition = new ArrayList<>();
        List<Double> newVelocity = new ArrayList<>();

        Random rng = new Random();
        for (int i = 0; i < newVelocity.size(); i++) {
            newVelocity.add(SwarmTrainer.OMEGA * this.velocity.get(i) +
                SwarmTrainer.PHI_P * rng.nextDouble() * (this.bestPosition.get(i) - this.position.get(i)) +
                SwarmTrainer.PHI_G * rng.nextDouble() * (globalBestPosition.get(i) - this.position.get(i)));
        }

        for (int i = 0; i < newPosition.size(); i++) {
            newPosition.add(this.position.get(i) + this.velocity.get(i));
        }

        this.position = newPosition;
        this.velocity = newVelocity;
    }

    public void print() {
        StringBuilder output = new StringBuilder();
        for (Double pos : this.position) {
            output.append(String.format("%.2f, ", pos));
        }
        System.out.println("Lines: " + String.format("%.2f", (double) this.fitness / SwarmTrainer.BATCH_SIZE));
        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    @Override
    public int compareTo(Particle particle) {
        return particle.fitness - this.fitness;
    }

}
