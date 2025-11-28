/*
 * Autor: Cristopher Resende
 * Data: 24/11/2025
 * Descrição: controle da camera que foi separado da Interface para melhor organização
 */

package src.Ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class CameraController implements MouseWheelListener {
    
    private final Interface ui;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean panning = false;

    public CameraController(Interface ui) {
        this.ui = ui;
    }
    
    public boolean isPanning() {
        return panning;
    }

    public void reset() {
        panning = false;
        lastMouseX = 0;
        lastMouseY = 0;
    }

    public void handleMousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        panning = true;
    }

    public void handleMouseReleased(MouseEvent e) {
        panning = false;
    }

    public void handleMouseDragged(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        int dx = mx - lastMouseX;
        int dy = my - lastMouseY;
        
        // Atualiza a posição da câmera na Interface
        ui.setCamX(ui.getCamX() - dx / ui.getZoom());
        ui.setCamY(ui.getCamY() - dy / ui.getZoom());

        lastMouseX = mx;
        lastMouseY = my;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double delta = 0.1 * e.getPreciseWheelRotation();
        double newZoom = ui.getZoom() - delta;
        newZoom = Math.max(0.1, Math.min(10, newZoom));
        ui.setZoom(newZoom);
    }
}