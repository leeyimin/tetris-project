import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class GeneticTrainer {

    public static final int STARTING_MOVES = Integer.MAX_VALUE;
    public static final int BATCH_SIZE = 20;
    public static final int POPULATION_SIZE = 200;
    public static final int NEWBORN_SIZE = 60;
    public static final int MUTANT_SIZE = 60;
    public static final int COEFFICIENT_MEAN = 0;
    public static final int COEFFICIENT_STDDEV = 10;

    private List<BiFunction<TestableState, TestableState, Integer>> features;
    private List<Gene> population;
    private int numMoves;
    private int generation;

    public GeneticTrainer(List<BiFunction<TestableState, TestableState, Integer>> features) {
        this.features = features;
        this.population = Collections.synchronizedList(new ArrayList<>());
        this.numMoves = STARTING_MOVES;
        this.generation = 0;
    }

    public void train() {
        this.initializePopulation();
        while (true) {
            this.generation++;
            this.matePopulation();
            this.mutatePopulation();
            this.evolvePopulation();
            this.updateParam();
            this.printSummary();
        }
    }

    private void initializePopulation() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Gene gene = Gene.random(features.size(), COEFFICIENT_MEAN, COEFFICIENT_STDDEV);
            this.population.add(gene);
        }
    }

    private void matePopulation() {
        Random rng = new Random();
        for (int i = 0; i < NEWBORN_SIZE; i++) {
            Gene father = this.population.get(rng.nextInt(POPULATION_SIZE));
            Gene mother = this.population.get(rng.nextInt(POPULATION_SIZE));
            this.population.add(Gene.mate(father, mother));
        }
    }

    private void mutatePopulation() {
        Random rng = new Random();
        for (int i = 0; i < MUTANT_SIZE; i++) {
            Gene gene = this.population.get(rng.nextInt(POPULATION_SIZE));
            this.population.add(Gene.mutate(gene, COEFFICIENT_MEAN, COEFFICIENT_STDDEV));
        }
    }

    private void evolvePopulation() {
        this.population
            .stream()
            .unordered()
            .parallel()
            .forEach((Gene g) -> {
                this.evaluateGene(g);
                System.out.print(".");
            });

        System.out.println();    
        System.out.println();    
        Collections.sort(this.population);
        this.population = this.population.subList(0, POPULATION_SIZE);
    }

    private void updateParam() {
        double firstRowsCleared = (double) this.population.get(0).fitness / BATCH_SIZE;
        double secondRowsCleared = (double) this.population.get(1).fitness / BATCH_SIZE;
        double thirdRowsCleared = (double) this.population.get(2).fitness / BATCH_SIZE;
        double theoreticalMaxRowsCleared = this.numMoves * 0.4;
        double threshold = 1.0 * theoreticalMaxRowsCleared;

        if (firstRowsCleared > threshold && secondRowsCleared > threshold && thirdRowsCleared > threshold) {
            this.numMoves *= 2;
        }
    }

    private void evaluateGene(Gene gene) {
        int totalRowsCleared = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            totalRowsCleared += new Player(gene.coefficients, features).simulate(this.numMoves);
        }
        gene.fitness = totalRowsCleared;
    }

    private void printSummary() {
        System.out.println("===========================");
        System.out.println(" RESULT OF GENERATION #" + this.generation);
        System.out.println("===========================");
        System.out.println();
        this.population.get(0).print();
        System.out.println();
        this.population.get(1).print();
        System.out.println();
        this.population.get(2).print();
        System.out.println();
    }


    public static void main(String args[]) {
        List<BiFunction<TestableState, TestableState, Integer>> features = new ArrayList<>();
        Features.addAllFeatures(features);
        new GeneticTrainer(features).train();
    }

}

class Gene implements Comparable<Gene> {

    public List<Double> coefficients;
    public int fitness;

    private Gene() {
        this.coefficients = new ArrayList<>();
        this.fitness = 0;
    }

    public static Gene random(int numCoefficients, double mean, double stdDev) {
        Gene gene = new Gene();
        Random rng = new Random();
        for (int i = 0; i < numCoefficients; i++) {
            double randomVal = rng.nextGaussian() * stdDev + mean;
            gene.coefficients.add(randomVal);
        }
        return gene;
    }

    public static Gene mate(Gene father, Gene mother) {
        Gene newborn = new Gene();
        Random rng = new Random();
        for (int i = 0; i < father.coefficients.size(); i++) {
            if (rng.nextBoolean()) {
                newborn.coefficients.add(father.coefficients.get(i));
            } else {
                newborn.coefficients.add(mother.coefficients.get(i));
            }
        }
        return newborn;
    }

    public static Gene mutate(Gene gene, double mean, double stdDev) {
        Gene mutant = new Gene();
        Random rng = new Random();
        for (int i = 0; i < gene.coefficients.size(); i++) {
            if (rng.nextDouble() < 0.2) {
                mutant.coefficients.add(rng.nextGaussian() * stdDev + mean);
            } else {
                mutant.coefficients.add(gene.coefficients.get(i));
            }
        }
        return mutant;
    }

    public void print() {
        StringBuilder output = new StringBuilder();
        for (Double coefficient : this.coefficients) {
            output.append(String.format("%.2f, ", coefficient));
        }
        System.out.println("Lines: " + String.format("%.2f", (double) this.fitness / GeneticTrainer.BATCH_SIZE));
        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    @Override
    public int compareTo(Gene gene) {
        return gene.fitness - this.fitness;
    }

}
