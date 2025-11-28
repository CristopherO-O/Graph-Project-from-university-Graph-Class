/*
 * Autor: Cristopher Resende
 * Data: 24/11/2025 
 * Descrição: gerenciamento dos temas da interface grafica, separado da Interface.java para melhor organização
 */

package src.Ui;

import java.awt.Color;

public class ThemeManager {
    private Color backgroundColor;
    private Color nodeColor;
    private Color nodeTextColor;
    private Color edgeColor;
    private Color hudColor;
    private Color cameraNodeColor;
    private Color animAntColor;
    private Color bestAntColor;

    public ThemeManager() {
        
    }

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

    // Getters para as cores
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getNodeColor() { return nodeColor; }
    public Color getNodeTextColor() { return nodeTextColor; }
    public Color getEdgeColor() { return edgeColor; }
    public Color getHudColor() { return hudColor; }
    public Color getCameraNodeColor() { return cameraNodeColor; }
    public Color getAnimAntColor() { return animAntColor; }
    public Color getBestAntColor() { return bestAntColor; }
}