package src.Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import src.Core.Graph;

/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: Heurística de Inserção do Vértice Mais Distante
 */

public class FarthestInsertion {

    private final Graph graph;
    private final int nodesNum;
    public int gTotalCost;

    public Metrics metrics = new Metrics();

    public FarthestInsertion(Graph graph) {
        this.graph = graph;
        this.nodesNum = graph.getNodesNum();
        gTotalCost = -1;
    }
    
    public int getTotalCost() { return gTotalCost; }

    private int cost(int u, int v) {
        metrics.sumOperation(1); // acesso ao grafo conta como operação
        return graph.getEdgeWeight(u, v);
    }

    public PathResult findHamiltonianCycle() {

        System.err.println("---------------------------------------------\n");
        metrics.reset();
        metrics.start();
        metrics.sumIteration(1);

        if (nodesNum < 2) {
            return new PathResult(Collections.singletonList(1), 0);
        }

        int start = -1;
        int second = -1;
        int maxDist = -1;
        for (src.Core.Edge e : graph.getEdges()) {
            metrics.sumIteration(1);

            int u = e.getNode1();
            int v = e.getNode2();
            int w = e.getWeight();

            metrics.sumOperation(1);

            if (w != Graph.INF && w > maxDist) {
                maxDist = w;
                start = u;
                second = v;
            }
        }

        if (start == -1 || second == -1) {
            return new PathResult(null, Graph.INF);
        }

        // ----- Ciclo inicial -----
        List<Integer> cycle = new ArrayList<>();
        cycle.add(start);
        cycle.add(second);
        cycle.add(start);

        // ----- Conjunto de não visitados -----
        Set<Integer> unvisited = new HashSet<>();
        for (int i = 1; i <= nodesNum; i++) {
            metrics.sumIteration(1);
            if (i != start && i != second) unvisited.add(i);
        }

        // ====== LOOP PRINCIPAL DE INSERÇÃO ======
        while (!unvisited.isEmpty()) {

            metrics.sumIteration(1);

            int farthestNode = -1;
            int maxMinDist = -1;

            // --- achar o mais distante do ciclo ---
            for (int r : unvisited) {
                metrics.sumIteration(1);

                int minDistToCycle = Graph.INF;

                for (int v : cycle) {
                    metrics.sumIteration(1);
                    int d = cost(v, r);
                    metrics.sumOperation(1);

                    if (d < minDistToCycle) minDistToCycle = d;
                }

                metrics.sumOperation(1);
                if (minDistToCycle != Graph.INF && minDistToCycle > maxMinDist) {
                    farthestNode = r;
                    maxMinDist = minDistToCycle;
                }
            }

            if (farthestNode == -1) break;

            // --- Inserção com menor custo ---
            int bestI = -1;
            int minCostIncrease = Graph.INF;

            for (int k = 0; k < cycle.size() - 1; k++) {

                metrics.sumIteration(1);

                int i = cycle.get(k);
                int j = cycle.get(k + 1);

                int cost_ir = cost(i, farthestNode);
                int cost_rj = cost(farthestNode, j);
                int cost_ij = cost(i, j);

                metrics.sumOperation(3);

                if (cost_ir != Graph.INF && cost_rj != Graph.INF && cost_ij != Graph.INF) {
                    int costIncrease = cost_ir + cost_rj - cost_ij;
                    metrics.sumOperation(1);

                    if (costIncrease < minCostIncrease) {
                        bestI = i;
                        minCostIncrease = costIncrease;
                    }
                }
            }

            if (bestI != -1) {
                int insertionIndex = cycle.indexOf(bestI);
                cycle.add(insertionIndex + 1, farthestNode);
            }

            unvisited.remove(farthestNode);
        }

        // ====== Calcular custo total ======
        int totalCost = 0;

        for (int i = 0; i < cycle.size() - 1; i++) {
            metrics.sumIteration(1);

            int u = cycle.get(i);
            int v = cycle.get(i + 1);

            int edgeCost = cost(u, v);
            metrics.sumOperation(1);

            if (edgeCost == Graph.INF) {
                return new PathResult(null, Graph.INF);
            }

            totalCost += edgeCost;
        }

        metrics.stop();
        System.out.println("[FI]Total Cost: " + totalCost + "\n[FI]" + metrics.toString());
        gTotalCost = totalCost;
        return new PathResult(cycle, totalCost, metrics);
    }

}