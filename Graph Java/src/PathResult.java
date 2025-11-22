/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: guarda caminho do Dijkistra para renderizar
 */

package src;
import java.util.List;

public class PathResult {
    public List<Integer> path;
    public int totalCost;

    public PathResult(List<Integer> path, int totalCost) {
        this.path = path;
        this.totalCost = totalCost;
    }
}