/*
 * Autor: Cristopher Resende
 * Data: 24/11/2025 
 * Descrição: essa classe cuida de toda a logica de desenho do grafo na interface grafica
 */

package src.Ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import src.Algorithms.Ant;
import src.Algorithms.AntColony;
import src.Core.Edge;
import src.Core.Graph;
import src.Core.NodePos;

public class GraphRenderer {

    private final Interface ui;

    public GraphRenderer(Interface ui) {
        this.ui = ui;
    }

    // >>>>>>>>>>----- desenha o grafo -----<<<<<<<<<<
    public void drawGraph(Graphics g) {

        Graph drawGraph = ui.isShowAGM() ? ui.getAgmGraph() : ui.getGraph();

        if (drawGraph == null) return;
        ThemeManager tm = ui.getThemeManager();

        // garantia de Graphics2D
        Graphics2D g2 = (Graphics2D) g;
        Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Desenha arestas (Padrão e Pesos) — agora com setas indicando direção
        g2.setColor(tm.getEdgeColor());
        for (Edge e : drawGraph.getEdges()) {
            NodePos a = ui.getPos().get(e.getNode1());
            NodePos b = ui.getPos().get(e.getNode2());

            int ax = toScreenX(a.x);
            int ay = toScreenY(a.y);
            int bx = toScreenX(b.x);
            int by = toScreenY(b.y);

            drawEdgeWithArrow(g2, ax, ay, bx, by);

            int mx = (int) (((a.x + b.x) / 2.0 - ui.getCamX()) * ui.getZoom());
            int my = (int) (((a.y + b.y) / 2.0 - ui.getCamY()) * ui.getZoom());

            int fontSize = (int) (12 * ui.getZoom());
            fontSize = Math.max(fontSize, 6);
            g2.setFont(g2.getFont().deriveFont((float) fontSize));
            g2.setColor(tm.getEdgeColor());

            if (e.getName().isEmpty()) {
                g2.drawString(String.valueOf(e.getWeight()), mx, my);
            } else {
                g2.drawString(e.getName(), mx, my);
            }
        }

        // restaura cor padrão do tema
        g2.setColor(tm.getEdgeColor());

        // 2. Desenha Caminhos/Especiais (apenas se não for AGM)
        if (!ui.isShowAGM()) {
            drawSpecialPaths(g2, tm);
        }

        // 3. Desenha Nós
        drawNodes(g2, tm);

        // restaura AA
        if (oldAA != null) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }

    // ---------- Helper: converte coordenadas do mundo para tela ----------
    private int toScreenX(double worldX) {
        return (int) ((worldX - ui.getCamX()) * ui.getZoom());
    }

    private int toScreenY(double worldY) {
        return (int) ((worldY - ui.getCamY()) * ui.getZoom());
    }

    // ---------- Desenha aresta + seta apontando para (x2,y2) ----------
    private void drawEdgeWithArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        // Ajuste para não desenhar a linha cruzando o nó (respeita o raio do nó)
        int nodeRadius = (int) (10 * ui.getZoom());
        nodeRadius = Math.max(nodeRadius, 3);

        // vetor direção de x1,y1 -> x2,y2
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1.0) return;

        double ux = dx / dist;
        double uy = dy / dist;

        // novo fim da linha: parar a (ponta) antes do centro do nó
        int endX = (int) Math.round(x2 - ux * nodeRadius);
        int endY = (int) Math.round(y2 - uy * nodeRadius);

        // e começar após o raio do nó de origem (opcional — aqui mantemos do centro)
        int startX = x1;
        int startY = y1;

        // desenha linha principal (com stroke fino)
        java.awt.Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(startX, startY, endX, endY);
        g2.setStroke(oldStroke);

        // desenha ponta da seta no endX,endY apontando para x2,y2
        drawArrowHead(g2, endX, endY, ux, uy);
    }

    // ---------- desenha a ponta da seta na posição (tipX,tipY) com direção unitária (ux,uy) ----------
    private void drawArrowHead(Graphics2D g2, int tipX, int tipY, double ux, double uy) {
        // arrow size escala com zoom
        int arrowSize = (int) (12 * ui.getZoom());
        arrowSize = Math.max(arrowSize, 6);

        // perpendicular vector
        double px = -uy;
        double py = ux;

        // base points: tip - (ux * size) +/- (perp * size * 0.5)
        double baseX = tipX - ux * arrowSize;
        double baseY = tipY - uy * arrowSize;

        int ax = (int) Math.round(baseX + px * (arrowSize * 0.5));
        int ay = (int) Math.round(baseY + py * (arrowSize * 0.5));

        int bx = (int) Math.round(baseX - px * (arrowSize * 0.5));
        int by = (int) Math.round(baseY - py * (arrowSize * 0.5));

        // desenha triângulo (duas linhas formando a ponta)
        java.awt.Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawLine(tipX, tipY, ax, ay);
        g2.drawLine(tipX, tipY, bx, by);
        g2.setStroke(oldStroke);
    }

    // Lógica de desenho de caminhos especiais
    private void drawSpecialPaths(Graphics g, ThemeManager tm) {
        Graphics2D g2 = (Graphics2D) g;

        // Caminho critico PERT
        if (ui.getPertCriticalPath() != null) {
            g2.setColor(Color.red);
            java.awt.Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(2.0f));
            for (Edge e : ui.getPertCriticalPath()) {
                NodePos a = ui.getPos().get(e.getNode1());
                NodePos b = ui.getPos().get(e.getNode2());
                int ax = toScreenX(a.x);
                int ay = toScreenY(a.y);
                int bx = toScreenX(b.x);
                int by = toScreenY(b.y);

                drawEdgeWithArrow(g2, ax, ay, bx, by);
            }
            g2.setStroke(oldStroke);
        }

        // Caminho Dijkstra
        if (ui.getShortestPath() != null && ui.getShortestPath().size() > 1) {
            g2.setColor(Color.red);
            drawPath(g2, ui.getShortestPath());
        }

        // Desenha o Ciclo Hamiltoniano (Farthest Insertion)
        if (ui.getHamiltonianCycle() != null && ui.getHamiltonianCycle().size() > 1) {
            // Usamos uma cor distinta, como verde, para o ciclo
            g2.setColor(new Color(0, 180, 0)); 
            drawPath(g2, ui.getHamiltonianCycle());
        }

        // Desenha melhor caminho formiga instantaneo
        if (ui.getBestAntPath() != null && ui.getBestAntPath().size() > 1) {
            g2.setColor(Color.RED);
            drawPath(g2, ui.getBestAntPath());
        }

        // Animação de Colônia de Formigas
        if (ui.isAnimatingAnts() && ui.getAnimColony() != null) {
            drawAntAnimation(g2, tm);
        }
    }

    // Método auxiliar para desenhar um caminho (usado por Dijkstra e Ant instantâneo)
    private void drawPath(Graphics g, List<Integer> path) {
        Graphics2D g2 = (Graphics2D) g;
        java.awt.Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < path.size() - 1; i++) {
            int a = path.get(i);
            int b = path.get(i + 1);
            NodePos p1 = ui.getPos().get(a);
            NodePos p2 = ui.getPos().get(b);

            int ax = toScreenX(p1.x);
            int ay = toScreenY(p1.y);
            int bx = toScreenX(p2.x);
            int by = toScreenY(p2.y);

            drawEdgeWithArrow(g2, ax, ay, bx, by);
        }
        g2.setStroke(oldStroke);
    }

    // Lógica de atualização e desenho da animação ACO
    public void updateAntAnimationState() {
        int framesPerStep = ui.getFramesPerStep();
        int animFrameCounter = ui.getAnimFrameCounter();
        AntColony animColony = ui.getAnimColony();

        animFrameCounter++;
        ui.setAnimFrameCounter(animFrameCounter);

        boolean stillRunning = true;
        if (animFrameCounter % framesPerStep == 0) {
            stillRunning = animColony.step();
        }

        // Atualiza animBestPath
        if (animColony != null && animColony.getBestTourEver() != null) {
            Ant bestAnt = animColony.getBestTourEver();
            if (bestAnt.getTourLength() < ui.getAnimBestLength()) {
                ui.setAnimBestLength(bestAnt.getTourLength());
                ui.setAnimBestPath(bestAnt.getTour());
            } else if (ui.getAnimBestPath() == null) {
                ui.setAnimBestPath(bestAnt.getTour());
            }
        }

        // Finaliza a animação
        if (animFrameCounter % framesPerStep == 0 && !stillRunning) {
            if (animColony != null && animColony.getBestTourEver() != null) {
                ui.setBestAntPath(animColony.getBestTourEver().getTour());
            }
            ui.setAnimatingAnts(false);
        }
    }

    // Lógica de desenho da animação ACO
    private void drawAntAnimation(Graphics g, ThemeManager tm) {
        Graphics2D g2 = (Graphics2D) g;

        // Desenha o melhor caminho conhecido até agora
        if (ui.getAnimBestPath() != null && ui.getAnimBestPath().size() > 1) {
            java.awt.Stroke oldStroke = g2.getStroke();
            g2.setColor(tm.getBestAntColor());
            g2.setStroke(new BasicStroke(2.0f));
            drawPath(g2, ui.getAnimBestPath());
            g2.setStroke(oldStroke);
        }

        // Desenha cada formiga
        g2.setColor(tm.getAnimAntColor());
        for (Ant ant : ui.getAnimColony().getAnts()) {
            if (ant.getTour().size() > 1) {
                drawPath(g2, ant.getTour());
            }
        }
    }

    // Lógica de desenho de nós
    private void drawNodes(Graphics g, ThemeManager tm) {
        Graphics2D g2 = (Graphics2D) g;
        for (int node = 1; node <= ui.getGraph().getNodesNum(); node++) {
            NodePos n = ui.getPos().get(node);

            int screenX = toScreenX(n.x);
            int screenY = toScreenY(n.y);

            // Cor do nó (cobertura mínima tem prioridade)
            if (ui.isShowCameras() && ui.getCameraNodes().contains(node)) {
                g2.setColor(tm.getCameraNodeColor());
            } else {
                g2.setColor(tm.getNodeColor());
            }

            int radius = (int) (10 * ui.getZoom());
            g2.fillOval(screenX - radius, screenY - radius, radius * 2, radius * 2);

            // Desenha o texto centralizado
            String text = String.valueOf(node);
            int nodeFontSize = (int) (12 * ui.getZoom());
            nodeFontSize = Math.max(nodeFontSize, 6);
            g2.setFont(g2.getFont().deriveFont((float) nodeFontSize));

            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            g2.setColor(tm.getNodeTextColor());
            g2.drawString(text, screenX - textWidth / 2, screenY + textHeight / 2);
        }
    }

    // desenha HUD
    public void drawHUD(Graphics g) {
        ThemeManager tm = ui.getThemeManager();
        g.setColor(tm.getHudColor());
        g.setFont(g.getFont().deriveFont((float) 12));

        if (!ui.isShowAGM()) {

            if (ui.getPertCriticalPath() != null) {
                g.drawString("Critical path in red!", 20, ui.HEIGHT - 35);
                g.drawString("Total project duration: " + ui.getPertDuration(), 20, ui.HEIGHT - 20);

            } else if (ui.getShortestPath() != null && ui.getShortestPath().size() > 1) {
                g.drawString("Total cost: " + ui.getShortestCost(), 20, ui.HEIGHT - 20);

            } else if (ui.getHamiltonianCycle() != null && ui.getHamiltonianCycle().size() > 1) { // NOVO ESTADO
                g.drawString("Total Cost: " + ui.getHamiltonianCost(), 20, ui.HEIGHT - 20);
                g.drawString("" + ui.getFIMetrics(), 20, ui.HEIGHT - 40);

            }else if (ui.isShowMaxFlow()) {
                g.drawString("Max Flow: " + ui.getMaxFlow(), 20, ui.HEIGHT - 20);

            } else if (ui.isShowCameras()) {
                g.drawString("Minimum cover: " + ui.getCameraNodes().size(), 20, ui.HEIGHT - 20);

            } else {
                g.drawString("Number of edges: " + ui.getGraph().getQuantity(), 20, ui.HEIGHT - 35);
                g.drawString("Number of nodes: " + ui.getGraph().getNodesNum(), 20, ui.HEIGHT - 20);

            }
            } else {
                g.drawString("Total Cost (AGM): " + ui.getAgmGraph().getTotalWeight(), 20, ui.HEIGHT - 20);
            }
    }
}
