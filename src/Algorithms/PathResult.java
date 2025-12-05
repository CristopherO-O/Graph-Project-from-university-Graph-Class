/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: guarda caminho do Dijkistra para renderizar
 */

package src.Algorithms;
import java.util.List;

public class PathResult {
    public List<Integer> path;
    public int totalCost;

    public Metrics metrics = new Metrics(); 

    public PathResult(List<Integer> path, int totalCost) {
        this.path = path;
        this.totalCost = totalCost;
    }

    
    public PathResult(List<Integer> path, int totalCost, Metrics metrics) {
        this.path = path;
        this.totalCost = totalCost;
        this.metrics = metrics;
    }

}