/*
 * Autor: Cristopher Resende
 * Data: 21/11/2025
 * Descrição: Fluxo maximo
 */

package src;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class FordFulkerson {
    public static int maxFlow(Graph g, int source, int sink) {
        int n = g.getNodesNum();
        int[][] capacity = new int[n + 1][n + 1];
        int[][] flow = new int[n + 1][n + 1];

        for (Edge e : g.getEdges()) {
            capacity[e.getNode1()][e.getNode2()] = e.getWeight();
        }

        int maxFlow = 0;

        while (true) {
            int[] parent = new int[n + 1];
            Arrays.fill(parent, -1);

            //BFS para encontrar caminho aumentante
            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);
            parent[source] = source;

            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.poll();
                for (int v = 1; v <= n; v++) {
                    if (capacity[u][v] - flow[u][v] > 0 && parent[v] == -1) {
                        parent[v] = u;
                        queue.add(v);
                    }
                }
            }
            if (parent[sink] == -1) break;

            //Determina o fluxo mínimo no caminho encontrado
            int pathFlow = Integer.MAX_VALUE;
            int v = sink;
            while (v != source) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v] - flow[u][v]);
                v = u;
            }

            // Atualiza o fluxo no caminho
            v = sink;
            while (v != source) {
                int u = parent[v];
                flow[u][v] += pathFlow;
                flow[v][u] -= pathFlow;
                v = u;
            }
            maxFlow += pathFlow;
        }

        return maxFlow;
    }

}
