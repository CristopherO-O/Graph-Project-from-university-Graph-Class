/*
 * Autor: Cristopher Resende
 * Data: 24/11/2025 
 * Descrição: interação com os nós do grafo (seleção e movimentação) separada da Interface.java para melhor organização
 */

package src.Ui;

import java.awt.event.MouseEvent;
import src.Core.NodePos;

public class NodeInteractionController {
    
    private final Interface ui;

    public NodeInteractionController(Interface ui) {
        this.ui = ui;
    }

    // Tenta selecionar um nó. Retorna true se um nó foi selecionado.
    public boolean handleMousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        
        int selectedNode = -1;
        int offsetX = 0;
        int offsetY = 0;

        for (int node = 1; node <= ui.getGraph().getNodesNum(); node++) {
            NodePos n = ui.getPos().get(node);
            double worldMouseX = mx / ui.getZoom() + ui.getCamX();
            double worldMouseY = my / ui.getZoom() + ui.getCamY();
            double dx = worldMouseX - n.x;
            double dy = worldMouseY - n.y;

            if (dx * dx + dy * dy <= 100) {
                selectedNode = node;
                offsetX = (int) dx;
                offsetY = (int) dy;
                break;
            }
        }

        ui.setSelectedNode(selectedNode);
        ui.setOffsetX(offsetX);
        ui.setOffsetY(offsetY);
        
        return selectedNode != -1;
    }

    public void handleMouseReleased(MouseEvent e) {
        ui.setSelectedNode(-1);
    }

    public void handleMouseDragged(MouseEvent e) {
        if (ui.getSelectedNode() == -1) return;

        int mx = e.getX();
        int my = e.getY();
        
        NodePos n = ui.getPos().get(ui.getSelectedNode());
        double worldMouseX = mx / ui.getZoom() + ui.getCamX();
        double worldMouseY = my / ui.getZoom() + ui.getCamY();

        n.x = (int)(worldMouseX - ui.getOffsetX());
        n.y = (int)(worldMouseY - ui.getOffsetY());
    }
}