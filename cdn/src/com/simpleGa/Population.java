package com.simpleGa;

public class Population {
    Individual[] individuals;

    Individual bestIndividual;

    /*
     * 构造方法
     */
    // 创建一个种群
    public Population(int populationSize, boolean initialise, int genesLength) {
        individuals = new Individual[populationSize];
        // 初始化种群
        if (initialise) {
            for (int i = 1; i < size(); i++) {
                Individual newIndividual = new Individual(genesLength);
                newIndividual.generateIndividual();
                saveIndividual(i, newIndividual);
            }
        }
    }

    /* Getters */
    public Individual getIndividual(int index) {
        return individuals[index];
    }

    public Individual getFittest() {
        Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.getFitness() <= getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
            }
        }
        return fittest;
    }

    /* Public methods */
    // Get population size
    public int size() {
        return individuals.length;
    }

    // Save individual
    public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }
}