package src.Algorithms;

/*
 * Autor: Cristopher Resende
 * Data: 05/12/2025
 * Descrição: Classe que guarda dados de execução como números de iteração,
 * operação e tempo de execução para análise comparativa.
 * no fim das contas acabei usando só o tempo e nem adiantou fazer essa classe
 */

public class Metrics {
    
    private long iterations = 0;   // Contagem de loops
    private long operations = 0;   // Contagem de operações
    
    private long startTime = 0;    // Tempo de início (nanoTime)
    private long endTime = 0;      // Tempo de fim (nanoTime)

    // ----- Getters -----
    public long getIterations(){ return iterations; }
    public long getOperations(){ return operations; }
    public long getStartTime(){ return startTime; }
    public long getEndTime(){ return endTime; }

    public long getElapsedNano(){ return endTime - startTime; }

    public double getElapsedMillis(){
        return (endTime - startTime) / 1_000_000.0;
    }

    // ----- Soma valores -----
    public void sumIteration(long x){ this.iterations += x; }
    public void sumOperation(long x){ this.operations += x; }

    // ----- Controle do tempo -----
    public void start(){
        this.startTime = System.nanoTime();
    }

    public void stop(){
        this.endTime = System.nanoTime();
    }

    // ----- Reset geral -----
    public void reset() {
        operations = 0;
        iterations = 0;
        startTime = 0;
        endTime = 0;
    }

    @Override
    public String toString() {
        return "Time: " + getElapsedMillis() + " ms";
    }
}
