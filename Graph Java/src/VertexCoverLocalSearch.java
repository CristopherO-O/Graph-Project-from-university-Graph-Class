/*
 * Autor: Cristopher Resende
 * Data: 21/11/2025
 * Descrição: algoritimo guloso kkkkkkkkkkk
 */

package src;

import java.util.*;

public class VertexCoverLocalSearch {

    public VertexCoverLocalSearch() {}

    public static Set<Integer> solve(Graph g) {
        ArrayList<Edge> list = new ArrayList<>();
        for (src.Edge e : g.getEdges()) {
            list.add(new Edge(e.getNode1(), e.getNode2()));
        }
        return vertexCoverFromMaximalMatching(g.getNodesNum(), list);
    }

    public static Set<Integer> vertexCoverFromMaximalMatching(int n, List<Edge> edges) {
        Set<Integer> cover = new HashSet<>();
        boolean[] matched = new boolean[n + 1]; 
        // Greedy maximal matching: percorre arestas e, se ambos endpoints livres, marca ambos
        for (Edge e : edges) {
            if (!matched[e.u] && !matched[e.v]) {
                matched[e.u] = true;
                matched[e.v] = true;
                cover.add(e.u);
                cover.add(e.v);
            }
        }
        removeRedundant(edges, cover);

        return cover;
    }

    // ----- Remove vértices redundantes da cobertura ------
    private static void removeRedundant(List<Edge> edges, Set<Integer> cover) {
        boolean changed = true;
        while (changed) {
            changed = false;
            List<Integer> list = new ArrayList<>(cover);
            for (int v : list) {
                cover.remove(v);
                if (!isValidCover(edges, cover)) {
                    cover.add(v); // é necessário
                } else {
                    changed = true; // removemos com sucesso
                }
            }
        }
    }

    // ----- Verifica se todas as arestas são cobertas pelo conjunto ------
    private static boolean isValidCover(List<Edge> edges, Set<Integer> cover) {
        for (Edge e : edges) {
            if (!cover.contains(e.u) && !cover.contains(e.v)) return false;
        }
        return true;
    }

    // Classe Edge interna
    public static class Edge {
        int u, v;
        Edge(int u, int v) { this.u = u; this.v = v; }
    }
}
