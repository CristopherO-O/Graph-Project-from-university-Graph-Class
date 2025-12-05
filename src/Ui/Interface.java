/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: essa classe cuida da interface grafica do programa
 * essa classe ja foi maior, tudo que esta na pasta Ui ja esteve uma vez aqui
 */


package src.Ui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import src.Algorithms.AGM;
import src.Algorithms.AntColony;
import src.Algorithms.Dijkstra;
import src.Algorithms.FordFulkerson;
import src.Algorithms.PERT;
import src.Algorithms.PathResult;
import src.Algorithms.VertexCoverLocalSearch;
import src.Core.Edge;
import src.Core.Graph;
import src.Core.NodePos;

public class Interface extends Canvas implements Runnable {

    // dimensões da janela
    public static int WIDTH = 1000, HEIGHT = 600;

    // camera
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    // arrastar com o mouse
    private int selectedNode = -1;
    private int offsetX = 0, offsetY = 0;

    // relativo ao grafo
    private Graph graph;
    private HashMap<Integer, NodePos> pos;
    private static int nodePosGen = 100;

    // caminho dijkstra
    private List<Integer> shortestPath = null;
    private int shortestCost = -1;

    // caminho PERT
    private PERT pert;
    private List<Edge> pertCriticalPath = null;
    private int pertDuration = -1;

    // AGM
    private Graph agmGraph = null;
    private boolean showAGM = false;

    // Ford-Fulkerson
    private int maxFlow;
    private boolean showMaxFlow;

    // minimum cover
    private Set<Integer> cameraNodes = new HashSet<>();
    private boolean showCameras = false;

    // Ciclo Hamiltoniano
    private List<Integer> hamiltonianCycle = null;
    private int hamiltonianCost = -1;
    private String FIMetrics;

    // melhor caminho da formiga
    private List<Integer> bestAntPath = null;
    private AntColony animColony = null;
    private boolean animatingAnts = false;
    
    // Controle de velocidade da animação: quantos frames por cada passo lógico
    private int framesPerStep = 10;
    private int animFrameCounter = 0;
    
    private List<Integer> animBestPath = null;
    private double animBestLength = Double.POSITIVE_INFINITY;
    
    // Tema/cores
    private ThemeManager themeManager;

    // Controladores modularizados
    private CameraController cameraController;
    private NodeInteractionController nodeInteractionController;
    private GraphRenderer graphRenderer;


    // ----- Construtor -----
    public Interface(Graph graph) {
        this.graph = graph;
        
        // Inicializa o ThemeManager
        this.themeManager = new ThemeManager();
        
        // Inicializa os controladores de interação
        this.cameraController = new CameraController(this);
        this.nodeInteractionController = new NodeInteractionController(this);

        // Inicializa o renderizador
        this.graphRenderer = new GraphRenderer(this);

        if (graph.getNodesNum() >= nodePosGen) {
            this.pos = generateForceDirectedPositions(graph);
        } else {
            this.pos = generateNodePositions(graph.getNodesNum());
        }

        this.pert = new PERT(graph);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Inicializa a câmera e seleção
        resetCameraAndSelection();

        // Adiciona listeners dos controladores
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!nodeInteractionController.handleMousePressed(e)) {
                    cameraController.handleMousePressed(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                nodeInteractionController.handleMouseReleased(e);
                cameraController.handleMouseReleased(e);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedNode != -1) {
                    nodeInteractionController.handleMouseDragged(e);
                } else if (cameraController.isPanning()) {
                    cameraController.handleMouseDragged(e);
                }
            }
        });

        this.addMouseWheelListener((MouseWheelListener) cameraController);
    }
    
    // Método auxiliar para reinicializar o estado
    private void resetCameraAndSelection() {
        camX = 0;
        camY = 0;
        zoom = 1.0;
        selectedNode = -1;
        offsetX = offsetY = 0;
        cameraController.reset();
    }

    // Métodos getters para acesso pelos controladores e renderizador
    public Graph getGraph() { return graph; }
    public HashMap<Integer, NodePos> getPos() { return pos; }
    public double getCamX() { return camX; }
    public double getCamY() { return camY; }
    public double getZoom() { return zoom; }
    public int getSelectedNode() { return selectedNode; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public ThemeManager getThemeManager() { return themeManager; }
    public List<Integer> getShortestPath() { return shortestPath; }
    public List<Edge> getPertCriticalPath() { return pertCriticalPath; }
    public List<Integer> getBestAntPath() { return bestAntPath; }
    public AntColony getAnimColony() { return animColony; }
    public boolean isAnimatingAnts() { return animatingAnts; }
    public boolean isShowAGM() { return showAGM; }
    public Set<Integer> getCameraNodes() { return cameraNodes; }
    public boolean isShowCameras() { return showCameras; }
    public List<Integer> getAnimBestPath() { return animBestPath; }
    public int getFramesPerStep() { return framesPerStep; }
    public int getAnimFrameCounter() { return animFrameCounter; }
    public int getShortestCost() { return shortestCost; }
    public int getPertDuration() { return pertDuration; }
    public boolean isShowMaxFlow() { return showMaxFlow; }
    public int getMaxFlow() { return maxFlow; }
    public Graph getAgmGraph() { return agmGraph; }
    public List<Integer> getHamiltonianCycle() { return hamiltonianCycle; } 
    public int getHamiltonianCost() { return hamiltonianCost; }
    public String getFIMetrics() { return FIMetrics; }


    // Métodos setters para modificação pelos controladores
    public void setCamX(double camX) { this.camX = camX; }
    public void setCamY(double camY) { this.camY = camY; }
    public void setZoom(double zoom) { this.zoom = zoom; }
    public void setSelectedNode(int selectedNode) { this.selectedNode = selectedNode; }
    public void setOffsetX(int offsetX) { this.offsetX = offsetX; }
    public void setOffsetY(int offsetY) { this.offsetY = offsetY; }
    public void setAnimFrameCounter(int animFrameCounter) { this.animFrameCounter = animFrameCounter; }
    public void setAnimatingAnts(boolean animatingAnts) { this.animatingAnts = animatingAnts; }
    public void setAnimBestPath(List<Integer> path) { this.animBestPath = path; }
    public void setAnimBestLength(double length) { this.animBestLength = length; }
    public double getAnimBestLength() { return animBestLength; }
    public void setBestAntPath(List<Integer> path) { this.bestAntPath = path; }


    // ----- calcula posição dos nós -----
    private HashMap<Integer, NodePos> generateNodePositions(int total) {
        HashMap<Integer, NodePos> map = new HashMap<>();

        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2;
        int radius = 250;

        double angleStep = 2 * Math.PI / total;
        double angle = 0;

        for (int node = 1; node <= total; node++) {
            int x = (int)(centerX + radius * Math.cos(angle));
            int y = (int)(centerY + radius * Math.sin(angle));
            map.put(node, new NodePos(x, y));
            angle += angleStep;
        }

        return map;
    }

    
    // ----- um algoritimo para exibir grafos grandes de forma menos pior-----
    private HashMap<Integer, NodePos> generateForceDirectedPositions(Graph graph) {
        // ... (Lógica Force-Directed igual à original) ...
        int n = graph.getNodesNum();
        HashMap<Integer, NodePos> pos = new HashMap<>();

        int W = WIDTH, H = HEIGHT;
        double area = W * H;
        double k = Math.sqrt(area / n);

        // posições iniciais aleatórias
        for (int node = 1; node <= n; node++) {
            pos.put(node, new NodePos(
                (int)(Math.random() * W),
                (int)(Math.random() * H)
            ));
        }

        for (int iter = 0; iter < 80; iter++) {

            double[] dispX = new double[n+1];
            double[] dispY = new double[n+1];

            // repulsão entre todos os nós
            for (int v = 1; v <= n; v++) {
                NodePos pv = pos.get(v);
                for (int u = v+1; u <= n; u++) {
                    NodePos pu = pos.get(u);

                    double dx = pv.x - pu.x;
                    double dy = pv.y - pu.y;
                    double dist = Math.sqrt(dx*dx + dy*dy) + 0.01;

                    double force = (k * k) / dist;

                    dispX[v] += dx / dist * force;
                    dispY[v] += dy / dist * force;

                    dispX[u] -= dx / dist * force;
                    dispY[u] -= dy / dist * force;
                }
            }

            // atração
            for (Edge e : graph.getEdges()) {
                int v = e.getNode1();
                int u = e.getNode2();

                NodePos pv = pos.get(v);
                NodePos pu = pos.get(u);

                double dx = pv.x - pu.x;
                double dy = pv.y - pu.y;
                double dist = Math.sqrt(dx*dx + dy*dy) + 0.01;

                double force = (dist * dist) / k;

                dispX[v] -= dx / dist * force;
                dispY[v] -= dy / dist * force;

                dispX[u] += dx / dist * force;
                dispY[u] += dy / dist * force;
            }

            // aplica deslocamentos com limite
            double temp = W * 0.02;
            for (int v = 1; v <= n; v++) {
                NodePos p = pos.get(v);

                double dx = dispX[v];
                double dy = dispY[v];

                double len = Math.sqrt(dx*dx + dy*dy);
                if (len > 0) {
                    dx = dx / len * Math.min(len, temp);
                    dy = dy / len * Math.min(len, temp);
                }

                p.x = (int)(p.x + dx);
                p.y = (int)(p.y + dy);
            }
        }

        return pos;
    }

    //>>>>>----- BOTOES-----<<<<<

    // ----- botao AGM -----
    public void calcularAGM() {
        agmGraph = AGM.kruskalMST(graph);
        showAGM = true;
    }

    // ----- botao pert -----
    public void calcularPERT() {
        if (!graph.isDAG()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Graph is not acyclic!");
            return;
        }
        pert.calcularPERT();
        pertCriticalPath = pert.getCriticalPath();
        pertDuration = pert.getProjectDuration();
    }

    // ----- botao limpar caminho -----
    public void clearPath() {
        shortestPath = null;
        shortestCost = -1;
        pertCriticalPath = null;
        bestAntPath = null;
        pertDuration = -1;
        showAGM = false;
        showMaxFlow = false;
        this.showCameras = false;
        animatingAnts = false;
        animColony = null;
        animBestPath = null;
        animBestLength = Double.POSITIVE_INFINITY;
        hamiltonianCycle = null; 
        hamiltonianCost = -1;
    }

    // ----- botao dijkstra -----
    public void calcularDijkstra() {
        try {
            String s1 = javax.swing.JOptionPane.showInputDialog("Origin:");
            String s2 = javax.swing.JOptionPane.showInputDialog("Destination:");

            if (s1 == null || s2 == null) return;

            int origin = Integer.parseInt(s1);
            int target = Integer.parseInt(s2);

            PathResult result = Dijkstra.findPathToTarget(graph, origin, target);

            shortestPath = result.path;
            shortestCost = result.totalCost;

            if (shortestPath == null) {
                javax.swing.JOptionPane.showMessageDialog(null, "no Path Exists!");
            } else {
                System.out.println("Shortest path: " + shortestPath + " | cost = " + shortestCost);
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid input!");
        }
    }

    // ----- botao Ford-Fulkerson -----
    public void calculaForFulkerson() {

        try {
            String s1 = javax.swing.JOptionPane.showInputDialog("Source: ");
            String s2 = javax.swing.JOptionPane.showInputDialog("Sink: ");

            if (s1 == null || s2 == null) return;

            int source = Integer.parseInt(s1);
            int sink = Integer.parseInt(s2);

            maxFlow = FordFulkerson.maxFlow(graph, source, sink);
            showMaxFlow = true;

            System.out.println("Max Flow: " + maxFlow);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid input!");
        }
    }

    //----- Botao coberturaMinima
    public void calcularCobertura() {
        this.cameraNodes = VertexCoverLocalSearch.solve(graph);
        this.showCameras = true;

        System.out.println("Minimum Cover: " + cameraNodes.size());
    }

    // ----- botao de adicionar aresta ------
    public void addArestaManual() {
        try {
            String s1 = javax.swing.JOptionPane.showInputDialog("Node 1:");
            String s2 = javax.swing.JOptionPane.showInputDialog("Node 2:");
            String s3 = javax.swing.JOptionPane.showInputDialog("Weight:");

            if (s1 == null || s2 == null || s3 == null) return;

            int n1 = Integer.parseInt(s1);
            int n2 = Integer.parseInt(s2);
            int w = Integer.parseInt(s3);

            //recalcula caso adicionado um no novo
            int maior = Math.max(n1, n2);
            if (maior > graph.getNodesNum()) {
                graph.setNodesNum(maior);
                if (maior >= nodePosGen) {
                    this.pos = generateForceDirectedPositions(graph);
                } else {
                    this.pos = generateNodePositions(maior);
                }
            }

            graph.insertEdge(n1, n2, w);

            // Atualiza o desenho
            if (graph.getNodesNum() >= nodePosGen) {
                this.pos = generateForceDirectedPositions(graph);
            } else {
                this.pos = generateNodePositions(graph.getNodesNum());
            }

            System.out.println("Edge added: " + n1 + " -> " + n2 + " (weight " + w + ")");

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid input!");
        }
    }

    // ----- botao ant instantaneo -----
    public void executarAntInstantaneo() {
        clearPath();
        executarAntInstantaneo(100, 1.0, 3.0, 0.5, 100, 0.00001);
    }

    // ----- Versão com parâmetros -----
    public void executarAntInstantaneo(int numIterations, double alpha, double beta, double rho, double Q, double initialPheromone) {
        AntColony colony = new AntColony(graph,
                numIterations,
                alpha,
                beta,
                rho,
                Q,
                initialPheromone);

        bestAntPath = colony.solveInstant();
        repaint();
    }

    //----- botao ant animado -----
    public void executarAntAnimado() {
        executarAntAnimado(100, 1.0, 3.0, 0.5, 100, 0.00001);
    }

    // Versão animada com parâmetros
    public void executarAntAnimado(int numIterations, double alpha, double beta, double rho, double Q, double initialPheromone) {
        if (animatingAnts) return;
        animColony = new AntColony(graph,
                numIterations,
                alpha,
                beta,
                rho,
                Q,
                initialPheromone);
        animColony.initializeAnts();
        animatingAnts = true;
        animFrameCounter = 0;
        // reset estado da melhor rota animada
        animBestPath = null;
        animBestLength = Double.POSITIVE_INFINITY;
    }

    // ----- Botão Ciclo Hamiltoniano (Farthest Insertion) -----
    public void calcularHamiltonianCycle() {

        clearPath();

        try {
            src.Algorithms.FarthestInsertion fi = new src.Algorithms.FarthestInsertion(graph);
            src.Algorithms.PathResult result = fi.findHamiltonianCycle(); // sem parâmetro

            hamiltonianCycle = result.path;
            hamiltonianCost = result.totalCost;
            FIMetrics = result.metrics.toString();

            if (hamiltonianCycle == null || hamiltonianCost == Graph.INF) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Ciclo Hamiltoniano não encontrado (grafo incompleto ou inacessível).");
            } else {
                System.out.println("Ciclo Hamiltoniano (Farthest Insertion): " + hamiltonianCycle);
                System.out.println("Custo Total: " + hamiltonianCost);
                repaint();
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro no cálculo do ciclo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tema escuro: true = dark, false = light
    public void setDarkTheme(boolean dark) {
        themeManager.setDarkTheme(dark);
    }

    // renderiza
    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        int w = getWidth();
        int h = getHeight();
        g.setColor(themeManager.getBackgroundColor());
        g.fillRect(0, 0, w, h);

        // >>>>>adicionar oque sera rederizado:<<<<<
        graphRenderer.drawGraph(g);
        graphRenderer.drawHUD(g);
        //>>>>>fim<<<<<

        bs.show();
    }

    // ----- thread -----
    public void run() {
        while (true) {
            if (animatingAnts) {
                // Lógica de passo de animação movida para o renderizador para atualizar o estado
                graphRenderer.updateAntAnimationState();
            }
            render();
            try {
                Thread.sleep(1000 / 20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}