import java.util.*;
import java.util.function.*;

public class GeneticTrainer {

    private static final int POPULATION = 100;
    private static final int BATCH_SIZE = 10;

    private List<Function<TestState, Double>> features;
    private int generation;

    public GeneticTrainer(List<Function<TestState, Double>> features) {
        this.features = features;
        this.generation = 0;
    }

    public void train() {
        List<Gene> population = this.createRandomGenes(POPULATION);
        while (true) {
            this.generation++;
            this.matePopulation(population, 20);
            this.mutatePopulation(population, 20);
            population = this.evolvePopulation(population);

            this.printReport(population);
        }
    }

    public void printReport(List<Gene> population) {
        // print the rows cleared
        System.out.println();
        System.out.println("======================================");
        System.out.println(" RESULT OF GENERATION #" + generation);
        System.out.println("======================================");
        population.get(0).print();
        population.get(1).print();
        population.get(2).print();
    }

    private void mutatePopulation(List<Gene> population, int size) {
        Random rng = new Random();
        for (int i = 0; i < size; i++) {
            Gene gene = population.get(rng.nextInt(POPULATION));
            population.add(this.mutate(gene));
        }
    }

    private Gene mutate(Gene gene) {
        Gene mutant = new Gene();
        Random rng = new Random();
        for (int i = 0; i < gene.coefficients.size(); i++) {
            if (rng.nextDouble() < 0.2) {
                mutant.coefficients.add(2 * rng.nextDouble() - 1);
            } else {
                mutant.coefficients.add(gene.coefficients.get(i));
            }
        }
        return mutant;
    }

    private void matePopulation(List<Gene> population, int size) {
        Random rng = new Random();
        for (int i = 0; i < size; i++) {
            Gene father = population.get(rng.nextInt(POPULATION));
            Gene mother = population.get(rng.nextInt(POPULATION));
            population.add(this.mate(father, mother));
        }
    }

    private Gene mate(Gene father, Gene mother) {
        Gene offspring = new Gene();
        Random rng = new Random();
        for (int i = 0; i < father.coefficients.size(); i++) {
            if (rng.nextBoolean()) {
                offspring.coefficients.add(father.coefficients.get(i));
            } else {
                offspring.coefficients.add(mother.coefficients.get(i));
            }
        }
        return offspring;
    }


    private List<Gene> evolvePopulation(List<Gene> population) {
        for (Gene gene : population) {
            this.evaluateGene(gene);
        }
        Collections.sort(population);
        return population.subList(0, POPULATION);
    }

    private void evaluateGene(Gene gene) {
        int totalRowsCleared = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            totalRowsCleared += new Player(gene.coefficients, features).simulate();
        }
        gene.fitness = (double) totalRowsCleared / BATCH_SIZE;
    }

    private List<Gene> createRandomGenes(int size) {
        List<Gene> genes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            genes.add(this.createRandomGene());
        }
        return genes;
    }

    private Gene createRandomGene() {
        Gene gene = new Gene(); 
        Random rng = new Random();
        for (int i = 0; i < this.features.size(); i++) {
            gene.coefficients.add(2 * rng.nextDouble() - 1);
        }
        return gene;
    }

    public static void main(String args[]) {
        List<Function<TestState, Double>> features = new ArrayList<>();
        features.add(Features::getBumpiness);
        features.add(Features::getTotalHeight);
        features.add(Features::getMaxHeight);
        features.add(Features::getNumHoles);
        features.add(Features::getBlocksAboveHoles);
        features.add(Features::getNumOfSignificantTopDifference);
        features.add(Features::getMeanAbsoluteDeviationOfTop);
        features.add(Features::hasLevelSurface);
        features.add(Features::hasRightStep);
        features.add(Features::hasLeftStep);
        features.add(Features::getNegativeOfRowsCleared);
        features.add(Features::hasPossibleInevitableDeathNextPiece);
        features.add(Features::getNumColsWithHoles);
        features.add(Features::getNumRowsWithHoles);
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

        new GeneticTrainer(features).train();
    }

}

class Gene implements Comparable<Gene> {

    List<Double> coefficients;
    double fitness;

    public Gene() {
        this.coefficients = new ArrayList<>();
    }

    public void print() {
        System.out.println("Fitness: " + this.fitness);
        this.printCoefficients();
        System.out.println();
    }

    private void printCoefficients() {
        StringBuilder output = new StringBuilder();

        for (Double coefficient : this.coefficients) {
            output.append(coefficient + ", ");
        }

        System.out.println(output.delete(output.length() - 2, output.length()).toString());
    }

    public int compareTo(Gene gene) {
        if (this.fitness < gene.fitness) {
            return 1;
        } else {
            return -1;
        }
    }

}


