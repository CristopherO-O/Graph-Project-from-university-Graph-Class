/*
 * Autor: Cristopher Resende
 * Data: 21/11/2025
 * Descrição: Colônia de Formigas para o Problema do Caixeiro Viajante
 */

package src.Algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import src.Core.Edge;
import src.Core.Graph;

public class AntColony {

    private final int numCities;
    private final int numAnts;
    private final int numIterations;
    private final double alpha;
    private final double beta;
    private final double rho;
    private final double Q;
    private final double initialPheromone;

    private static final int MAX_STAGNATION_ITERATIONS = 30;
    private int stagnationCounter = 0;

    private final double[][] distanceMatrix;
    private double[][] pheromoneMatrix;
    private Ant bestAntEver;
    private final Random random;

    public Metrics metrics = new Metrics();

    private boolean improved;
    // animação
    private int currentIteration;
    private List<Ant> ants;

    // ----- construtor -----
    public AntColony(Graph graph, int numIterations, double alpha, double beta, double rho, double Q, double initialPheromone) {
        this.numCities = graph.getNodesNum();
        this.numAnts = numCities;
        this.numIterations = numIterations;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.Q = Q;
        this.initialPheromone = initialPheromone;
        this.random = new Random();

        this.distanceMatrix = initializeDistanceMatrix(graph);
        this.pheromoneMatrix = initializePheromoneMatrix(numCities, initialPheromone);
        this.bestAntEver = null;

        this.ants = new ArrayList<>();
        this.currentIteration = 0;
        initializeAnts();
    }

    // ----- Getters -----
    public List<Ant> getAnts() {
        return this.ants;
    }

    public int getBestCost() {
        return bestAntEver != null ? (int) bestAntEver.getTourLength() : Integer.MAX_VALUE;
    }
    
    public Ant getBestTourEver() {
        return this.bestAntEver;
    }

    private boolean allAntsFinished() {
        for (Ant ant : ants) {
            if (!ant.finishedTour()) return false;
        }
        return true;
    }

    // ----- Executa um passo da animação -----
    public boolean step() {

        if (currentIteration >= numIterations) return false;

        for (Ant ant : ants) {
            if (!ant.finishedTour()) {
                ant.moveOneStep(pheromoneMatrix, distanceMatrix, alpha, beta);
                ant.calculateTourLength(distanceMatrix);
            }
        }

        if (allAntsFinished()) {

            double previousBestLength = bestAntEver != null ? bestAntEver.getTourLength() : Double.MAX_VALUE;

            updatePheromones();
            updateBestTour();

            if (bestAntEver != null && bestAntEver.getTourLength() < previousBestLength) {
                stagnationCounter = 0;
            } else {
                stagnationCounter++;
            }

            if (stagnationCounter >= MAX_STAGNATION_ITERATIONS) {
                System.out.printf("[Animado] Parada por Estagnação na Iteração %d\n", currentIteration + 1);
                currentIteration = numIterations;
                return false;
            }

            currentIteration++;
            initializeAnts();
        }

        return true;
    }

    // ---------------------- Execução instantânea ----------------------
    public List<Integer> solveInstant() {

        System.out.println("---------------------------------------------");
        metrics.start();  // INÍCIO DA MEDIÇÃO

        bestAntEver = null;
        double lastBestLength = Double.MAX_VALUE;

        List<Ant> ants = null;

        for (int t = 1; t <= numIterations; t++) {

            metrics.sumIteration(1);

            ants = new ArrayList<>(numAnts);
            Ant bestAntThisIteration = null;

            for (int k = 0; k < numAnts; k++) {
                metrics.sumOperation(1);

                Ant a = new Ant(numCities, random);
                a.startTourRandomly();
                a.constructTour(pheromoneMatrix, distanceMatrix, alpha, beta);
                a.calculateTourLength(distanceMatrix);

                ants.add(a);

                if (bestAntThisIteration == null || a.getTourLength() < bestAntThisIteration.getTourLength()) {
                    bestAntThisIteration = a;
                }
            }

            if (bestAntEver == null || bestAntThisIteration.getTourLength() < bestAntEver.getTourLength()) {
                bestAntEver = bestAntThisIteration.copy();
                stagnationCounter = 0;
                lastBestLength = bestAntEver.getTourLength();
                improved = true;
            } else {
                stagnationCounter++;
                improved = false;
            }

            if (stagnationCounter >= MAX_STAGNATION_ITERATIONS) {
                System.out.printf("[AOC] Parada por Estagnação na Iteração %d (Não melhorou por %d iterações)\n",
                        t, MAX_STAGNATION_ITERATIONS);
                break;
            }

            updatePheromones(ants);

            if(improved){
                System.out.printf("[AOC] Iteração %d | Melhor = %.2f\n",
                        t, bestAntEver.getTourLength());
            }
        }

        updatePheromones(ants);

        metrics.stop();  // FIM DA MEDIÇÃO

        System.out.println("[AOC]: " + metrics.toString());
        return bestAntEver.getTour();
    }

    // ----- cria e retorna matriz de distancia -----
    private double[][] initializeDistanceMatrix(Graph graph) {
        double[][] matrix = new double[numCities + 1][numCities + 1];

        for (Edge edge : graph.getEdges()) {
            int u = edge.getNode1();
            int v = edge.getNode2();
            double weight = edge.getWeight();

            matrix[u][v] = weight;
            matrix[v][u] = weight;
        }
        return matrix;
    }

    // ----- Inicializa as formigas -----
    public void initializeAnts() {
        ants.clear();
        for (int k = 0; k < numAnts; k++) {
            Ant ant = new Ant(numCities, random);
            ant.startTourRandomly();
            ants.add(ant);
        }
    }

    // ----- cria e retorna matriz de feromonio -----
    private double[][] initializePheromoneMatrix(int n, double initialValue) {
        double[][] matrix = new double[n + 1][n + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (i != j) matrix[i][j] = initialValue;
            }
        }
        return matrix;
    }

    // ----- Atualiza a melhor solucao global -----
    private void updateBestTour() {
        for (Ant ant : ants) {
            if (bestAntEver == null || ant.getTourLength() < bestAntEver.getTourLength()) {
                bestAntEver = ant.copy();
            }
        }
    }

    private void updatePheromones() {
        updatePheromones(ants);
    }

    // ----- atualiza feromonio -----
    private void updatePheromones(List<Ant> ants) {

        for (int i = 1; i <= numCities; i++) {
            for (int j = 1; j <= numCities; j++) {
                if (i != j) {
                    pheromoneMatrix[i][j] = (1.0 - rho) * pheromoneMatrix[i][j];
                }
            }
        }

        for (Ant ant : ants) {
            double delta_tau = Q / ant.getTourLength();
            List<Integer> tour = ant.getTour();

            for (int i = 0; i < tour.size() - 1; i++) {
                int c1 = tour.get(i);
                int c2 = tour.get(i + 1);

                pheromoneMatrix[c1][c2] += delta_tau;
                pheromoneMatrix[c2][c1] += delta_tau;
            }
        }
    }
}
