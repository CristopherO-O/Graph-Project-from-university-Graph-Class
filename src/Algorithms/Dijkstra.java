package src.Algorithms;

import src.Core.Graph;
import src.Core.Edge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dijkstra {

    public static void findPaths(Graph graph, int origin) {

        int n = graph.getNodesNum();
        List<Edge> edges = graph.getEdges();

        // nodeTable[i][0] = visitado (0 = Não, 1 = Sim)
        // nodeTable[i][1] = distancia (Integer.MAX_VALUE = infinito)
        int[][] nodeTable = new int[n + 1][2];
        int[] parent = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            nodeTable[i][1] = Integer.MAX_VALUE;
            nodeTable[i][0] = 0;
            parent[i] = -1;
        }

        nodeTable[origin][1] = 0;
        parent[origin] = origin;

        for (int i = 1; i <= n; i++) {

            int u = -1;
            int mindist = Integer.MAX_VALUE;

            for (int j = 1; j <= n; j++) {
                if (nodeTable[j][0] == 0 && nodeTable[j][1] < mindist) {
                    mindist = nodeTable[j][1];
                    u = j;
                }
            }

            if (u == -1)
                break;

            nodeTable[u][0] = 1;

            // Relaxamento (Percorre a lista de arestas para encontrar vizinhos de u)
            for (Edge e : edges) {
                if (e.getNode1() == u) {

                    int v = e.getNode2();
                    int w = e.getWeight();

                    if (nodeTable[u][1] != Integer.MAX_VALUE &&
                        nodeTable[u][1] + w < nodeTable[v][1]) {

                        nodeTable[v][1] = nodeTable[u][1] + w;
                        parent[v] = u;
                    }
                }
            }
        }

        System.out.println("Distance from node " + origin + ":");
        for (int i = 1; i <= n; i++) {

            if (nodeTable[i][1] == Integer.MAX_VALUE) {
                System.out.println("Node " + i + " = INF");
            } else {

                System.out.print("Node " + i + " = " + nodeTable[i][1] + " | Path: ");

                // Reconstrói o caminho invertido
                ArrayList<Integer> path = new ArrayList<>();
                int curr = i;

                while (curr != origin) {
                    if (curr == -1) break;
                    path.add(curr);
                    curr = parent[curr];
                }
                path.add(origin);

                // Imprime na ordem correta
                for (int k = path.size() - 1; k >= 0; k--) {
                    System.out.print(path.get(k));
                    if (k > 0) System.out.print(" -> ");
                }
                System.out.println();
            }
        }
    }
    

    public static PathResult findPathToTarget(Graph graph, int origin, int target) {

        int n = graph.getNodesNum();
        List<Edge> edges = graph.getEdges();

        // nodeTable[i][0] = visitado
        // nodeTable[i][1] = distancia
        int[][] nodeTable = new int[n + 1][2];
        int[] parent = new int[n + 1];

        // 1. Inicializa
        for (int i = 1; i <= n; i++) {
            nodeTable[i][1] = Integer.MAX_VALUE;
            nodeTable[i][0] = 0;
            parent[i] = -1;
        }

        nodeTable[origin][1] = 0;
        parent[origin] = origin;

        for (int i = 1; i <= n; i++) {
            int u = -1, mindist = Integer.MAX_VALUE;

            // Escolhe nó não visitado com menor distância
            for (int j = 1; j <= n; j++) {
                if (nodeTable[j][0] == 0 && nodeTable[j][1] < mindist) {
                    mindist = nodeTable[j][1];
                    u = j;
                }
            }

            if (u == -1) break;
            nodeTable[u][0] = 1;

            // Relaxamento
            for (Edge e : edges) {
                if (e.getNode1() == u) {
                    int v = e.getNode2();
                    int w = e.getWeight();
                    
                    if (nodeTable[u][1] != Integer.MAX_VALUE &&
                        nodeTable[u][1] + w < nodeTable[v][1]) {
                        
                        nodeTable[v][1] = nodeTable[u][1] + w;
                        parent[v] = u;
                    }
                }
            }
        }

        // Reconstroi o caminho ate o target
        ArrayList<Integer> path = new ArrayList<>();
        if (nodeTable[target][1] == Integer.MAX_VALUE) {
            return new PathResult(null, -1);
        }

        int curr = target;
        while (curr != origin) {
             if (curr == -1) return new PathResult(null, -1);
            path.add(curr);
            curr = parent[curr];
        }
        path.add(origin);
        Collections.reverse(path);

        return new PathResult(path, nodeTable[target][1]);
    }
}