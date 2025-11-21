/*
 * Autor: Cristopher Resende
 * Data: 21/11/2025
 * Descrição: Arvore geradora minima
 */

package src;

import java.util.*;

public class AGM {


    // ----- retorna a AGM usando kruskal -----
    public static Graph kruskalMST(Graph g) {
        int n = g.getNodesNum();
        Graph mst = new Graph(n);

        // Ordena as arestas por peso
        List<Edge> edges = new ArrayList<>(g.getEdges());
        edges.sort(Comparator.comparingInt(Edge::getWeight));

        // Estrutura para union-find
        int[] parent = new int[n + 1];
        for (int i = 1; i <= n; i++) parent[i] = i;

        for (Edge e : edges) {
            int uRoot = find(parent, e.getNode1());
            int vRoot = find(parent, e.getNode2());

            if (uRoot != vRoot) { // se não forma ciclo
                mst.insertEdge(e.getNode1(), e.getNode2(), e.getWeight());
                parent[uRoot] = vRoot;
            }
        }

        return mst;
    }

    private static int find(int[] parent, int u) {
        if (parent[u] != u)
            parent[u] = find(parent, parent[u]); // path compression
        return parent[u];
    }
}
