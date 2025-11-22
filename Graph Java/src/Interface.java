/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025 (altero isso aqui toda hora)
 * Descrição: essa classe cuida da interface grafica do programa
 * por preguica isso acabou virando uma super classe, tarde de mais
 */


package src;

import java.awt.FontMetrics;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


public class Interface extends Canvas implements Runnable{
    
    //dimensões da janela
    public static int WIDTH = 1000, HEIGHT = 600;

    // camera
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    // arrasto camera
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean panning = false;

    //arrastar com o mouse
    private int selectedNode = -1;
    private int offsetX = 0, offsetY = 0;


    //relativo ao grafo
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

    //minimum cover
    private Set<Integer> cameraNodes = new HashSet<>();
    private boolean showCameras = false;

    //melhor caminho da formiga
    private List<Integer> bestAntPath = null;
    private AntColony animColony = null;
    private boolean animatingAnts = false;
    // Tema/cores
    private java.awt.Color backgroundColor = Color.white;
    private java.awt.Color nodeColor = Color.blue;
    private java.awt.Color nodeTextColor = Color.yellow;
    private java.awt.Color edgeColor = Color.black;
    private java.awt.Color hudColor = Color.black;
    private java.awt.Color cameraNodeColor = Color.red;
    private java.awt.Color animAntColor = new Color(255, 0, 0, 120);
    private java.awt.Color bestAntColor = Color.BLUE;
    // Controle de velocidade da animação: quantos frames por cada passo lógico
    private int framesPerStep = 2; // 2 frames por passo -> metade da velocidade atual
    private int animFrameCounter = 0;
    // caminho melhor atual durante animação (atualiza conforme animColony)
    private List<Integer> animBestPath = null;
    private double animBestLength = Double.POSITIVE_INFINITY;

    // ----- Construtor -----
    public Interface(Graph graph){
        this.graph = graph;
        if(graph.getNodesNum() >= nodePosGen){
            this.pos = generateForceDirectedPositions(graph);
        } else {
            this.pos = generateNodePositions(graph.getNodesNum());
        }

        this.pert = new PERT(graph);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        camX = 0;
        camY = 0;
        zoom = 1.0;

        selectedNode = -1;
        offsetX = offsetY = 0;
        lastMouseX = lastMouseY = 0;
        panning = false;

        // ----- clique e seleção -----
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                lastMouseX = mx;
                lastMouseY = my;
                selectedNode = -1;

                for (int node = 1; node <= graph.getNodesNum(); node++) {
                    NodePos n = pos.get(node);
                    double worldMouseX = mx / zoom + camX;
                    double worldMouseY = my / zoom + camY;
                    double dx = worldMouseX - n.x;
                    double dy = worldMouseY - n.y;

                    if (dx * dx + dy * dy <= 100) {
                        selectedNode = node;
                        offsetX = (int) dx;
                        offsetY = (int) dy;
                        break;
                    }
                }

                if (selectedNode == -1) {
                    panning = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedNode = -1;
                panning = false;
            }
        });

        // ----- arrastar nós ou câmera -----
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                int dx = mx - lastMouseX;
                int dy = my - lastMouseY;

                if (selectedNode != -1) {
                    NodePos n = pos.get(selectedNode);
                    double worldMouseX = mx / zoom + camX;
                    double worldMouseY = my / zoom + camY;

                    n.x = (int)(worldMouseX - offsetX);
                    n.y = (int)(worldMouseY - offsetY);
                } else if (panning) {
                    camX -= dx / zoom;
                    camY -= dy / zoom;
                }

                lastMouseX = mx;
                lastMouseY = my;
            }
        });

        // ----- zoom -----
        this.addMouseWheelListener(e -> {
            double delta = 0.1 * e.getPreciseWheelRotation();
            zoom -= delta;
            zoom = Math.max(0.1, Math.min(10, zoom));
        });
    }

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

    // ----- cria uma grid e posiciona os nos dentro das celulas aleatoriamente -----
    // substituido pelo generateForceDirectedPositions()
    private HashMap<Integer, NodePos> generateNodeGridPosition(int total) {
        HashMap<Integer, NodePos> map = new HashMap<>();
        int padding = 45;

        int cols = (int) Math.ceil(Math.sqrt(total));
        int rows = (int) Math.ceil((double) total / cols);

        double cellWidth = (WIDTH - 2 * padding) / (double) cols;
        double cellHeight = (HEIGHT - 2 * padding) / (double) rows;

        int node = 1;
        for (int r = 0; r < rows && node <= total; r++) {
            for (int c = 0; c < cols && node <= total; c++) {
                int x = padding + (int)(c * cellWidth + Math.random() * cellWidth);
                int y = padding + (int)(r * cellHeight + Math.random() * cellHeight);

                map.put(node, new NodePos(x, y));
                node++;
            }
        }

        return map;
    }

    // ----- deixa o grafo mais bonito aos olhos -----
    private HashMap<Integer, NodePos> generateForceDirectedPositions(Graph graph) {

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
    public void clearPath(){
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
    }

    // ----- botao dijkstra -----
    public void calcularDijkstra() {
        try {
            String s1 = javax.swing.JOptionPane.showInputDialog("Origin:");
            String s2 = javax.swing.JOptionPane.showInputDialog("Destination:");

            if (s1 == null || s2 == null) return;

            int origin = Integer.parseInt(s1);
            int target = Integer.parseInt(s2);

            PathResult result = graph.dijkstraPath(origin, target);

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

            int source= Integer.parseInt(s1);
            int sink = Integer.parseInt(s2);

            maxFlow = FordFulkerson.maxFlow(graph,source,sink);
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
            int w  = Integer.parseInt(s3);

            //recalcula caso adicionado um no novo
            int maior = Math.max(n1, n2);
            if (maior > graph.getNodesNum()) {
                graph.setNodesNum(maior);
                if(maior >=nodePosGen){
                    this.pos = generateForceDirectedPositions(graph);
                }else{
                    this.pos = generateNodePositions(maior);
                }
            }

            graph.insertEdge(n1, n2, w);

            // Atualiza o desenho
            if(graph.getNodesNum() >=nodePosGen){
                this.pos = generateForceDirectedPositions(graph);
            }else{
                this.pos = generateNodePositions(graph.getNodesNum());
            }

            System.out.println("Edge added: " + n1 + " -> " + n2 + " (weight " + w +")");

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid input!");
        }
    }

    // ----- botao ant instantaneo -----
    public void executarAntInstantaneo() {
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

    // Tema escuro: true = dark, false = light
    public void setDarkTheme(boolean dark) {
        if (dark) {
            backgroundColor = new Color(25, 30, 35);
            nodeColor = new Color(0, 180, 200);
            nodeTextColor = Color.WHITE;
            edgeColor = new Color(100, 120, 130);
            hudColor = Color.WHITE;
            cameraNodeColor = new Color(255, 165, 0);
            animAntColor = new Color(255, 140, 0);
            bestAntColor = new Color(60, 220, 120);
        } else {
            backgroundColor = Color.white;
            nodeColor = Color.blue;
            nodeTextColor = Color.yellow;
            edgeColor = Color.black;
            hudColor = Color.black;
            cameraNodeColor = Color.red;
            animAntColor = new Color(255, 0, 0, 120);
            bestAntColor = Color.BLUE;
        }
    }

    // >>>>>>>>>>----- desenha o grafo -----<<<<<<<<<<
    private void drawGraph(Graphics g) {

        Graph drawGraph = showAGM ? agmGraph : graph; 

        if (drawGraph == null) return;

        g.setColor(edgeColor);
        for (Edge e : drawGraph.getEdges()) {
            NodePos a = pos.get(e.getNode1());
            NodePos b = pos.get(e.getNode2());
            
            int ax = (int)((a.x - camX) * zoom);
            int ay = (int)((a.y - camY) * zoom);
            int bx = (int)((b.x - camX) * zoom);
            int by = (int)((b.y - camY) * zoom);

            g.drawLine(ax, ay, bx, by);

            int mx = (int)(((a.x + b.x)/2.0 - camX) * zoom);
            int my = (int)(((a.y + b.y)/2.0 - camY) * zoom);

            int fontSize = (int)(12 * zoom); // tamanho base 12
            fontSize = Math.max(fontSize, 6); // limite minimo para nao sumir
            g.setFont(g.getFont().deriveFont((float) fontSize));

            if (e.getName().isEmpty()) {
                g.setFont(g.getFont().deriveFont((float) fontSize));
                g.drawString(String.valueOf(e.getWeight()), mx, my);
            } else { 
                g.setFont(g.getFont().deriveFont((float) fontSize));
                g.drawString(e.getName(), mx, my);
            }
            
        }

        if (!showAGM) {
            // Caminho critico PERT
            if (pertCriticalPath != null) {
                g.setColor(Color.red);
                for (Edge e : pertCriticalPath) {
                    NodePos a = pos.get(e.getNode1());
                    NodePos b = pos.get(e.getNode2());
                    int ax = (int)((a.x - camX) * zoom);
                    int ay = (int)((a.y - camY) * zoom);
                    int bx = (int)((b.x - camX) * zoom);
                    int by = (int)((b.y - camY) * zoom);

                    g.drawLine(ax, ay, bx, by);
                }
            }

            // Caminho Dijkstra
            if (shortestPath != null && shortestPath.size() > 1) {
                g.setColor(Color.red);
                for (int i = 0; i < shortestPath.size() - 1; i++) {
                    int a = shortestPath.get(i);
                    int b = shortestPath.get(i + 1);
                    NodePos p1 = pos.get(a);
                    NodePos p2 = pos.get(b);
                    
                    int ax = (int)((p1.x - camX) * zoom);
                    int ay = (int)((p1.y - camY) * zoom);
                    int bx = (int)((p2.x - camX) * zoom);
                    int by = (int)((p2.y - camY) * zoom);

                    g.drawLine(ax, ay, bx, by);
                }
            }

            //desenha melhor caminho formiga instantaneo
            if (bestAntPath != null && bestAntPath.size() > 1) {
                g.setColor(Color.RED);
                for (int i = 0; i < bestAntPath.size() - 1; i++) {
                    int a = bestAntPath.get(i);
                    int b = bestAntPath.get(i+1);

                    NodePos p1 = pos.get(a);
                    NodePos p2 = pos.get(b);

                    int ax = (int)((p1.x - camX) * zoom);
                    int ay = (int)((p1.y - camY) * zoom);
                    int bx = (int)((p2.x - camX) * zoom);
                    int by = (int)((p2.y - camY) * zoom);

                    g.drawLine(ax, ay, bx, by);
                }
            }

            if (animatingAnts && animColony != null) {
                //reduz a velocidade da animação
                animFrameCounter++;
                boolean stillRunning = true;
                if (animFrameCounter % framesPerStep == 0) {
                    stillRunning = animColony.step();
                }

                // atualiza animBestPath a partir do melhor encontrado na colônia (se houver)
                if (animColony != null && animColony.getBestTourEver() != null) {
                    Ant bestAnt = animColony.getBestTourEver();
                    if (bestAnt.getTourLength() < animBestLength) {
                        animBestLength = bestAnt.getTourLength();
                        animBestPath = bestAnt.getTour();
                    } else if (animBestPath == null) {
                        animBestPath = bestAnt.getTour();
                    }
                }

                // desenha o melhor caminho conhecido até agora
                if (animBestPath != null && animBestPath.size() > 1) {
                    Graphics2D g2 = (Graphics2D) g;
                    java.awt.Stroke oldStroke = g2.getStroke();
                    g2.setColor(bestAntColor);
                    g2.setStroke(new BasicStroke(2.0f));
                    for (int i = 0; i < animBestPath.size() - 1; i++) {
                        int a = animBestPath.get(i);
                        int b = animBestPath.get(i + 1);
                        NodePos p1 = pos.get(a);
                        NodePos p2 = pos.get(b);

                        int ax = (int)((p1.x - camX) * zoom);
                        int ay = (int)((p1.y - camY) * zoom);
                        int bx = (int)((p2.x - camX) * zoom);
                        int by = (int)((p2.y - camY) * zoom);

                        g2.drawLine(ax, ay, bx, by);
                    }
                    g2.setStroke(oldStroke);
                }

                // desenha cada formiga
                g.setColor(animAntColor);
                for (Ant ant : animColony.getAnts()) {
                    List<Integer> tour = ant.getTour();
                    for (int i = 0; i < tour.size() - 1; i++) {
                        int a = tour.get(i);
                        int b = tour.get(i + 1);
                        NodePos p1 = pos.get(a);
                        NodePos p2 = pos.get(b);

                        int ax = (int)((p1.x - camX) * zoom);
                        int ay = (int)((p1.y - camY) * zoom);
                        int bx = (int)((p2.x - camX) * zoom);
                        int by = (int)((p2.y - camY) * zoom);

                        g.drawLine(ax, ay, bx, by);
                    }
                }

                if (animFrameCounter % framesPerStep == 0 && !stillRunning) {
                    if (animColony != null && animColony.getBestTourEver() != null) {
                        bestAntPath = animColony.getBestTourEver().getTour();
                    }
                    animatingAnts = false;
                }
            }
        }

        
        // Desenha nos
        for (int node = 1; node <= graph.getNodesNum(); node++) {
            NodePos n = pos.get(node);

            int screenX = (int)((n.x - camX) * zoom);
            int screenY = (int)((n.y - camY) * zoom);

            if (showCameras && cameraNodes.contains(node)) {
                g.setColor(cameraNodeColor);
            } else {
                g.setColor(nodeColor);
            }

            int radius = (int)(10 * zoom);
            g.fillOval(screenX - radius, screenY - radius, radius * 2, radius * 2);

            // desenha o texto centralizado
            String text = String.valueOf(node);
            int nodeFontSize = (int)(12 * zoom);
            nodeFontSize = Math.max(nodeFontSize, 6);
            g.setFont(g.getFont().deriveFont((float) nodeFontSize));

            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            g.setColor(nodeTextColor);
            g.drawString(text, screenX - textWidth / 2, screenY + textHeight / 2);
        }
    }

    // desenha HUD
    private void drawHUD(Graphics g) {

        g.setFont(g.getFont().deriveFont((float) 15)); // tamanho da font da HUD
        if (!showAGM) {
            if (pertCriticalPath != null) {
                g.setColor(hudColor);
                g.setFont(g.getFont().deriveFont((float) 12));
                g.drawString("Critical path in red!", 20, HEIGHT - 35);
                g.drawString("Total project duration: " + pertDuration, 20, HEIGHT - 20);
            }

            if (shortestPath != null && shortestPath.size() > 1) {
                g.setColor(hudColor);
                g.drawString("Total cost: " + shortestCost, 20, HEIGHT - 20);
            }

            if(showMaxFlow){
                g.setColor(hudColor);
                g.drawString("Max Flow: " + this.maxFlow, 20, HEIGHT - 20);
            }

             if(showCameras){
                g.setColor(hudColor);
                g.drawString("Minimum cover: " + cameraNodes.size(), 20, HEIGHT - 20);
            }

            if(pertCriticalPath == null && shortestPath == null && !showMaxFlow && !showCameras){
                g.setColor(hudColor);
                g.drawString("Number of edges: " + graph.getQuantity(), 20, HEIGHT - 35);
                g.drawString("Number of nodes: " + graph.getNodesNum(), 20, HEIGHT - 20);
            }
        } else {
            g.setColor(hudColor);
            g.drawString("Total Cost: " + agmGraph.getTotalWeight(), 20, HEIGHT - 20);
        }

    }

    // renderiza
    public void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        int w = getWidth();
        int h = getHeight();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, w, h);

        // >>>>>adicionar oque sera rederizado:<<<<<
        drawGraph(g);
        drawHUD(g); 
        //>>>>>fim<<<<<

        bs.show();
    }

    // ----- thread -----
    public void run(){
        while(true){
            render();
            try{
                Thread.sleep(1000/20);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        } 
        
    }

}

