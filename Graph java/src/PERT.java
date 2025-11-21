/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: PERT/CP
 */

package src;

import java.util.*;

public class PERT {
    
    private Graph graph;
    private int[] early;
    private int[] late; 
    private List<Edge> criticalPath;

    // ----- getters ------
    public int getEarly(int node) { return early[node];}
    public int getLate(int node) { return late[node]; }
    public List<Edge> getCriticalPath() { return criticalPath; }

    // ----- construtor -----
    public PERT(Graph graph) {
        this.graph = graph;
        int n = graph.getNodesNum() + 1; 
        early = new int[n];
        late  = new int[n];
        criticalPath = new ArrayList<>();
    }

    

    public void calcularPERT() {
        int n = graph.getNodesNum();

        // Forward pass
        Arrays.fill(early, 0);
        boolean changed;
        do {
            changed = false;
            for (Edge e : graph.getEdges()) {
                int u = e.getNode1();
                int v = e.getNode2();
                int w = e.getWeight();
                if (early[u] + w > early[v]) {
                    early[v] = early[u] + w;
                    changed = true;
                }
            }
        } while (changed);

        // Backward pass
        int maxTime = 0;
        for (int i = 1; i <= n; i++) maxTime = Math.max(maxTime, early[i]);
        Arrays.fill(late, maxTime);

        do {
            changed = false;
            for (Edge e : graph.getEdges()) {
                int u = e.getNode1();
                int v = e.getNode2();
                int w = e.getWeight();
                if (late[v] - w < late[u]) {
                    late[u] = late[v] - w;
                    changed = true;
                }
            }
        } while (changed);

        // Identificar caminho crítico
        criticalPath.clear();
        for (Edge e : graph.getEdges()) {
            int u = e.getNode1();
            int v = e.getNode2();
            int w = e.getWeight();
            int folga = late[v] - early[u] - w;
            if (folga == 0) {
                criticalPath.add(e);
            }
        }

        // imprimir resultados
        System.out.println("Earliest times: " + Arrays.toString(Arrays.copyOfRange(early,1,n+1)));
        System.out.println("Latest times: " + Arrays.toString(Arrays.copyOfRange(late,1,n+1)));
        System.out.println("Critical Path:");
        for (Edge e : criticalPath) {
            System.out.println(e.getNode1() + " -> " + e.getNode2() + " (dur: " + e.getWeight() + ")");
        }
        System.out.println("Total project duration: " + maxTime);
    }


    public int getProjectDuration() {
        int n = graph.getNodesNum();
        int maxTime = 0;
        for (int i = 1; i <= n; i++) maxTime = Math.max(maxTime, early[i]);
        return maxTime;
    }
}
