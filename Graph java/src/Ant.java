package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ant {

    private final int numCities;
    private final Random random;

    protected List<Integer> tour;
    protected List<Integer> unvisited;
    protected double tourLength;

    public Ant(int numCities, Random random) {
        this.numCities = numCities;
        this.random = random;
        this.tour = new ArrayList<>();
        this.unvisited = new ArrayList<>();
        this.tourLength = Double.MAX_VALUE;
    }

    // ---------------------- Inicialização ----------------------

    public void startTourRandomly() {
        tour.clear();
        unvisited.clear();

        for (int i = 1; i <= numCities; i++) {
            unvisited.add(i);
        }

        int start = unvisited.remove(random.nextInt(unvisited.size()));
        tour.add(start);
    }

    // Construção de caminho completa
    public void constructTour(double[][] pheromone, double[][] distance, double alpha, double beta) {
        while (!unvisited.isEmpty()) {
            int current = tour.get(tour.size() - 1);
            int next = selectNextCity(current, pheromone, distance, alpha, beta);
            tour.add(next);
            unvisited.remove((Integer) next);
        }

        tour.add(tour.get(0));
    }

    // ---------------------- Seleção da próxima cidade ----------------------

    public int selectNextCity(int currentCity,
                              double[][] pheromone,
                              double[][] distance,
                              double alpha,
                              double beta) {

        double[] probs = new double[numCities + 1];
        double sum = 0;

        for (int j : unvisited) {
            double tau = Math.pow(pheromone[currentCity][j], alpha);
            double eta = Math.pow(1.0 / distance[currentCity][j], beta);
            probs[j] = tau * eta;
            sum += probs[j];
        }

        if (sum <= 0) {
            return unvisited.get(random.nextInt(unvisited.size()));
        }

        double r = random.nextDouble() * sum;
        double acc = 0;

        for (int j : unvisited) {
            acc += probs[j];
            if (acc >= r) return j;
        }

        return unvisited.get(0);
    }

    // ---------------------- Metodos para animação ----------------------

    public boolean finishedTour() {
        return tour.size() == numCities + 1;
    }

    public void moveOneStep(double[][] pheromone, double[][] distance, double alpha, double beta) {

        if (tour.size() == numCities) {
            tour.add(tour.get(0));
            unvisited.clear();
            return;
        }

        if (finishedTour()) return;

        int current = tour.get(tour.size() - 1);
        int next = selectNextCity(current, pheromone, distance, alpha, beta);

        tour.add(next);
        unvisited.remove((Integer) next);
    }

    public int getCurrentCity() {
        return tour.get(tour.size() - 1);
    }

    // ---------------------- Cálculo de custo ----------------------

    public void calculateTourLength(double[][] distance) {
        double sum = 0;

        for (int i = 0; i < tour.size() - 1; i++) {
            sum += distance[tour.get(i)][tour.get(i + 1)];
        }

        this.tourLength = sum;
    }

    // ---------------------- Getters ----------------------

    public double getTourLength() {
        return tourLength;
    }

    public List<Integer> getTour() {
        return Collections.unmodifiableList(tour);
    }

    // ---------------------- Clone ----------------------

    public Ant copy() {
        Ant a = new Ant(this.numCities, this.random);
        a.tour = new ArrayList<>(this.tour);
        a.unvisited = new ArrayList<>(this.unvisited);
        a.tourLength = this.tourLength;
        return a;
    }
}
