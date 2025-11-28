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
    // Parâmetros do Ant System
    private final int numCities;
    private final int numAnts; // m = número de formigas (sugestão: m = número de vértices) 
    private final int numIterations;
    private final double alpha; // Pondera a influência do feromônio
    private final double beta;  // Pondera a influência do custo do caminho
    private final double rho;   // Taxa de evaporação do feromônio (0 < rho <= 1)
    private final double Q;     // Variável definida pelo usuário para o depósito de feromônio
    private final double initialPheromone; // Tau_0 (sugestão: 10^-6)

    private static final int MAX_STAGNATION_ITERATIONS = 50; //maximo de estagnacao
    private int stagnationCounter = 0;
    
    // Estruturas de Dados
    private final double[][] distanceMatrix; // Matriz de distâncias (d_ij)
    private double[][] pheromoneMatrix;    // Matriz de feromônio (tau_ij)
    private Ant bestAntEver;               //Melhor solução global
    private final Random random;

    //animacao
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
        
        // Inicializa as matrizes (indexadas de 1 a N)
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
    // 1. Parada por Número Máximo de Iterações
    if (currentIteration >= numIterations) return false;

    // Cada formiga se move um passo
    for (Ant ant : ants) {
        if (!ant.finishedTour()) {
            ant.moveOneStep(pheromoneMatrix, distanceMatrix, alpha, beta);
            ant.calculateTourLength(distanceMatrix); // atualiza comprimento parcial
        }
    }

    // Se todas as formigas terminaram o tour, atualiza feromônio e melhor solução
    if (allAntsFinished()) {
        
        // Armazena o comprimento do melhor tour ANTES da atualização
        double previousBestLength = bestAntEver != null ? bestAntEver.getTourLength() : Double.MAX_VALUE;

        updatePheromones();
        updateBestTour(); // Atualiza bestAntEver

        // 2. Verificação de Estagnação
        if (bestAntEver != null && bestAntEver.getTourLength() < previousBestLength) {
            // Houve melhoria na solução global
            stagnationCounter = 0;
        } else {
            // Não houve melhoria: incrementa o contador
            stagnationCounter++;
        }
        
        // 3. Parada por Estagnação
        if (stagnationCounter >= MAX_STAGNATION_ITERATIONS) {
            System.out.printf("[Animado] Parada por Estagnação na Iteração %d\n", currentIteration + 1);
            currentIteration = numIterations; // Força a parada do loop externo
            return false; // Sinaliza que a simulação terminou
        }

        // Se não houve estagnação, continua para a próxima iteração
        currentIteration++;
        initializeAnts(); // reinicia as formigas para próxima iteração
    }

    return true;
}

    // ---------------------- Resolve instantaneo (sem animacao) ----------------------
    public List<Integer> solveInstant() {

        System.out.println("Running instant Ant System...");

        bestAntEver = null;
        double lastBestLength = Double.MAX_VALUE; // Rastreia o melhor comprimento da iteração anterior

        for (int t = 1; t <= numIterations; t++) {

            // ... (Criação de formigas e construção de tour) ...

            List<Ant> ants = new ArrayList<>(numAnts);
            Ant bestAntThisIteration = null; // Melhor formiga encontrada nesta iteração

            for (int k = 0; k < numAnts; k++) {
                Ant a = new Ant(numCities, random);
                a.startTourRandomly();
                a.constructTour(pheromoneMatrix, distanceMatrix, alpha, beta);
                a.calculateTourLength(distanceMatrix);
                ants.add(a);

                // Rastreia a melhor formiga local desta iteração
                if (bestAntThisIteration == null || a.getTourLength() < bestAntThisIteration.getTourLength()) {
                    bestAntThisIteration = a;
                }
            }

            // ---------------------- Lógica de Estagnação ----------------------
            
            // 1. Atualiza o melhor tour global e verifica se houve melhora
            if (bestAntEver == null || bestAntThisIteration.getTourLength() < bestAntEver.getTourLength()) {
                bestAntEver = bestAntThisIteration.copy();
                
                // Houve melhoria: zera o contador de estagnação
                stagnationCounter = 0;
                lastBestLength = bestAntEver.getTourLength(); // Novo recorde
                
            } else {
                // Não houve melhoria: incrementa o contador
                stagnationCounter++;
            }
            
            // 2. Critério de Parada: Estagnação
            if (stagnationCounter >= MAX_STAGNATION_ITERATIONS) {
                System.out.printf("[Instant] Parada por Estagnação na Iteração %d (Não melhorou por %d iterações)\n",
                        t, MAX_STAGNATION_ITERATIONS);
                break; // Sai do loop 'for'
            }
            
            // ------------------------------------------------------------------

            // atualiza matriz
            updatePheromones(ants);

            System.out.printf("[Instant] Iteração %d | Melhor = %.2f\n",
                    t, bestAntEver.getTourLength());
        }

        // Garante que a matriz de feromônio seja atualizada com o melhor caminho
        updatePheromones(ants); // Pode ser removido, pois já está no loop, mas garante a última atualização.
        
        return bestAntEver.getTour();
    }


    // ----- cria e retorna matriz de distancia -----
    private double[][] initializeDistanceMatrix(Graph graph) {
        // Matriz de N+1 x N+1 (para usar índices de 1 a N)
        double[][] matrix = new double[numCities + 1][numCities + 1];
        
        for (Edge edge : graph.getEdges()) {
            int u = edge.getNode1();
            int v = edge.getNode2();
            double weight = edge.getWeight();
            // O PCV simétrico tem dist(i,j) = dist(j,i) 
            matrix[u][v] = weight;
            matrix[v][u] = weight;
        }
        return matrix;
    }
    
    // ----- Inicializa as formigas (chamar antes de cada iteração animada) -----
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
                if (i != j) {
                    matrix[i][j] = initialValue;
                }
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

    // ----- Atualiza feromônios usando a lista interna de formigas da animacao -----
    private void updatePheromones() {
        updatePheromones(ants);
    }


    // ----- atualiza os feromonios -----
    private void updatePheromones(List<Ant> ants) {
        // evapora feromonio
        for (int i = 1; i <= numCities; i++) {
            for (int j = 1; j <= numCities; j++) {
                if (i != j) {
                    // (1 - rho) * tau_ij
                    pheromoneMatrix[i][j] = (1.0 - rho) * pheromoneMatrix[i][j]; 
                }
            }
        }
        
        // deposita feromonio
        for (Ant ant : ants) {
            double delta_tau = Q / ant.getTourLength(); // Q / Lk [cite: 199]
            List<Integer> tour = ant.getTour();
            
            // Percorre a rota
            for (int i = 0; i < tour.size() - 1; i++) {
                int city1 = tour.get(i);
                int city2 = tour.get(i + 1);
                
                // tau_ij = tau_ij + Delta_tau_ij^k [cite: 193]
                pheromoneMatrix[city1][city2] += delta_tau;
                // No PCV simétrico
                pheromoneMatrix[city2][city1] += delta_tau;
            }
        }
    }
}

