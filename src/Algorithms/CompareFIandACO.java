package src.Algorithms;

import src.Core.Graph;

public class CompareFIandACO {

    private final Graph graph;
    public int total;
    public int ACOBest = Integer.MAX_VALUE;

    public CompareFIandACO(Graph graph) {
        this.graph = graph;
    }

    public void runComparison() {

        int runs = 30;

        // -----------------------------
        // FARHEST INSERTION (FI)
        // -----------------------------
        double[] fiTimes = new double[runs];

        for (int i = 0; i < runs; i++) {
            FarthestInsertion fi = new FarthestInsertion(graph);
            fi.findHamiltonianCycle();
            fiTimes[i] = fi.metrics.getElapsedMillis();
            total = fi.getTotalCost();
        }

        double fiAvg = average(fiTimes);

        System.out.println("====================================");
        System.out.println(" FARTHEST INSERTION AVERAGE");
        System.out.println(" Total Cost: " + total + " ms");
        System.out.println(" Average: " + fiAvg + " ms");
        System.out.println("====================================");


        // -----------------------------
        // ANT COLONY (ACO)
        // -----------------------------
        double[] acoTimes = new double[runs];

        for (int i = 0; i < runs; i++) {

            AntColony aco = new AntColony(
                    graph,
                    100,       // numIterations
                    1.0,       // alpha
                    3.0,       // beta
                    0.5,       // rho
                    100,       // Q
                    0.00001    // initialPheromone
            );

            aco.solveInstant();
            acoTimes[i] = aco.metrics.getElapsedMillis();
            int cost = aco.getBestCost();
            if (cost < ACOBest) ACOBest = cost;
        }

        double acoAvg = average(acoTimes);

        System.out.println("====================================");
        System.out.println(" ANT COLONY AVERAGE");
        System.out.println(" Best Cost: " + ACOBest);
        System.out.println(" Average: " + acoAvg + " ms");
        System.out.println("====================================");
        System.out.println("\n\n\n\n\n");
        System.out.println("====================================");
        System.out.println(" FARTHEST INSERTION AVERAGE");
        System.out.println(" Total Cost: " + total + " ms");
        System.out.println(" Average: " + fiAvg + " ms");
        System.out.println("====================================");
        System.out.println("////////////////////////////////////");
        System.out.println("====================================");
        System.out.println(" ANT COLONY AVERAGE");
        System.out.println(" Best Cost: " + ACOBest);
        System.out.println(" Average: " + acoAvg + " ms");
        System.out.println("====================================");

    }

    private double average(double[] list) {
        double sum = 0;
        for (double d : list) sum += d;
        return sum / list.length;
    }
}