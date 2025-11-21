/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: essa classe cria a estrutura das arestas
 */


package src;

public class Edge {
    private int node1;
    private int node2;
    private int weight;
    private String name;

    // ----- construtor -----
    public Edge(int node1, int node2, int weight) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
        this.name = "";
    }
    
    // ----- Construtor que aceita o nome para a aresta -----
    public Edge(int node1, int node2, int weight, String name) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
        this.name = name;
    }

    // ----- getters -----
    public int getNode1() { return node1; }
    public int getNode2() { return node2; }
    public int getWeight() { return weight; }
    public String getName() { return name; }
}