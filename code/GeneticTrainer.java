import java.util.*;
import java.util.function.*;

public class GeneticTrainer {

    private static final int BATCH_SIZE = 10;
    private static final int POPULATION_SIZE = 100;
    private static final int NEWBORN_SIZE = 20;
    private static final int MUTANT_SIZE = 20;
    private static final int COEFFICIENT_MEAN = 0;
    private static final int COEFFICIENT_STDDEV = 30;

    private List<Function<TestState, Double>> features;
    private List<Gene> population;
    private int generation;

    public GeneticTrainer(List<Function<TestState, Double>> features) {
        this.features = features;
        this.population = new ArrayList<>();
        this.generation = 0;
    }

    public void train() {
        this.initializePopulation();
        while (true) {
            this.generation++;
            this.matePopulation();
            this.mutatePopulation();
            this.evolvePopulation();
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
        for (Gene gene : this.population) {
            this.evaluateGene(gene);
        }
        Collections.sort(this.population);
        this.population = this.population.subList(0, POPULATION_SIZE);
    }

    private void evaluateGene(Gene gene) {
        int totalRowsCleared = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            totalRowsCleared += new Player(gene.coefficients, features).simulate();
        }
        gene.fitness = (double) totalRowsCleared / BATCH_SIZE;
    }

    private void printSummary() {
        System.out.println("===========================");
        System.out.println(" RESULT OF GENERATION #" + this.generation);
        System.out.println("===========================");
        this.population.get(0).print();
        this.population.get(1).print();
        this.population.get(2).print();
    }


    public static void main(String args[]) {
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
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);

        new GeneticTrainer(features).train();
    }

}

class Gene implements Comparable<Gene> {

    public List<Double> coefficients;
    public double fitness;

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
        System.out.println("Fitness: " + String.format("%.2f", this.fitness));
        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    @Override
    public int compareTo(Gene gene) {
        return (int) (gene.fitness - this.fitness);
    }

}
