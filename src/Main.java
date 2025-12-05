/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: Main
 */

package src;

import java.awt.Color;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import src.Algorithms.CompareFIandACO;
import src.Core.Graph;
import src.Ui.Interface;

public class Main {
    public static void main(String[] args) {
        try {
            Graph g = null;

        String[] options = {"TXT", "GML"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Escolha o tipo de arquivo para carregar:",
            "Carregar Grafo",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

            try {
                if(choice == 0){
                    g = Graph.loadGraphFromFile("Graph.txt");
                } else if(choice == 1){ 
                    g = Graph.loadGraphFromGML("Graph.gml");
                } else {
                    System.exit(0);
                }
            } catch(IOException e){
                JOptionPane.showMessageDialog(null, "Erro ao carregar o grafo: " + e.getMessage());
                System.exit(0);
            }

            final Graph gForLambda = g;

            Interface inter = new Interface(g);
            javax.swing.JFrame frame = new javax.swing.JFrame();

            // >>>>>Painel superior (para vários botões)<<<<<
            javax.swing.JPanel topPanel = new javax.swing.JPanel();
            topPanel.setLayout(new java.awt.FlowLayout());

            // Botao adicionar aresta
            javax.swing.JButton btnAdd = new javax.swing.JButton("Add Edge");
            btnAdd.addActionListener(e -> inter.addArestaManual());
            topPanel.add(btnAdd);

            // Botao calcular dijkstra
            javax.swing.JButton btnDijk = new javax.swing.JButton("Dijkstra");
            btnDijk.addActionListener(e -> inter.calcularDijkstra());
            topPanel.add(btnDijk);

            // Botao de calcular PERT
            javax.swing.JButton btnPERT = new javax.swing.JButton("PERT");
            btnPERT.addActionListener(e -> inter.calcularPERT());
            topPanel.add(btnPERT);

            // Botao AGM
            javax.swing.JButton btnAGM = new javax.swing.JButton("AGM");
            btnAGM.addActionListener(e -> inter.calcularAGM());
            topPanel.add(btnAGM);

            // Botao Ford-Fulkerson
            javax.swing.JButton btnFORD = new javax.swing.JButton("Ford-Fulkerson");
            btnFORD.addActionListener(e -> inter.calculaForFulkerson());
            topPanel.add(btnFORD);

            // Botao Cobertura Minima
            javax.swing.JButton btnCOVER = new javax.swing.JButton("Minimum Cover");
            btnCOVER.addActionListener(e -> inter.calcularCobertura());
            topPanel.add(btnCOVER);

            // Farthest Insertion
            javax.swing.JButton btnHamiltonian = new javax.swing.JButton("Farthest Insertion");
            btnHamiltonian.addActionListener(e -> inter.calcularHamiltonianCycle());
            topPanel.add(btnHamiltonian);

            // calcula o algoritimo instantaneamente
            JButton btnANT = new JButton("Instant Ant");
            btnANT.addActionListener(e -> {
                double[] params = showACOParameterDialog(frame,
                        100, 1.0, 3.0, 0.5, 100, 0.00001);
                if (params != null) {
                    inter.executarAntInstantaneo((int) params[0], params[1], params[2], params[3], params[4], params[5]);
                }
            });
            topPanel.add(btnANT);

            //executa o algoritimo de forma visual
            JButton btnAntAnim = new JButton("Animated Ant");
            btnAntAnim.addActionListener(e -> {
                double[] params = showACOParameterDialog(frame,
                        100, 1.0, 3.0, 0.5, 100, 0.00001);
                if (params != null) {
                    inter.executarAntAnimado((int) params[0], params[1], params[2], params[3], params[4], params[5]);
                }
            });
            topPanel.add(btnAntAnim);

            // Tema escuro (ligado por padrão)
            final boolean[] darkState = new boolean[] { true };
            JButton btnTheme = new JButton("Light Theme");
            btnTheme.addActionListener(e -> {
                darkState[0] = !darkState[0];
                applyAppTheme(frame, inter, darkState[0]);
                btnTheme.setText(darkState[0] ? "Light Theme" : "Dark Theme");
            });
            topPanel.add(btnTheme);


            // Adiciona o painel ao topo da janela
            frame.add(topPanel, java.awt.BorderLayout.NORTH);

            // >>>>>Painel inferior (para o botão Limpar Caminho)<<<<<
            javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
            bottomPanel.setLayout(new java.awt.FlowLayout());

            // Botao Clear
            javax.swing.JButton btnClear = new javax.swing.JButton("Clear Results");
            btnClear.addActionListener(e -> inter.clearPath());
            bottomPanel.add(btnClear);

            // Botão Compare FI vs ACO
            javax.swing.JButton btnCompare = new javax.swing.JButton("Compare FI vs ACO");
            btnCompare.addActionListener(e -> {

                try {
                    CompareFIandACO cmp = new CompareFIandACO(gForLambda);
                    cmp.runComparison();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erro ao comparar algoritmos:\n" + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            bottomPanel.add(btnCompare);

            // Adiciona o em baixo da janela
            frame.add(bottomPanel, java.awt.BorderLayout.SOUTH);

            // janela  
            frame.add(inter);
            frame.setTitle("Melhor exercicios de grafos que você vai ver na vida");
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

            // aplica tema inicial (dark por padrão)
            applyAppTheme(frame, inter, darkState[0]);

            frame.setVisible(true);

            g.printGraph();
            
            new Thread(inter).start();

        } catch (Exception e) {
            System.out.println("Error loading Graph:");
            e.printStackTrace();
        }
    }

    // mostra um diálogo para editar parâmetros do ACO e retorna array: {iterations, alpha, beta, rho, Q, initialPheromone}
    private static double[] showACOParameterDialog(java.awt.Component parent, int defIter, double defAlpha, double defBeta, double defRho, double defQ, double defInitP) {
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(0,2));

        panel.add(new javax.swing.JLabel("Iterations:"));
        javax.swing.JTextField iterField = new javax.swing.JTextField(String.valueOf(defIter));
        panel.add(iterField);

        panel.add(new javax.swing.JLabel("Alpha:"));
        javax.swing.JTextField alphaField = new javax.swing.JTextField(String.valueOf(defAlpha));
        panel.add(alphaField);

        panel.add(new javax.swing.JLabel("Beta:"));
        javax.swing.JTextField betaField = new javax.swing.JTextField(String.valueOf(defBeta));
        panel.add(betaField);

        panel.add(new javax.swing.JLabel("Rho (evap):"));
        javax.swing.JTextField rhoField = new javax.swing.JTextField(String.valueOf(defRho));
        panel.add(rhoField);

        panel.add(new javax.swing.JLabel("Q (deposit):"));
        javax.swing.JTextField qField = new javax.swing.JTextField(String.valueOf(defQ));
        panel.add(qField);

        panel.add(new javax.swing.JLabel("Initial pheromone:"));
        javax.swing.JTextField initField = new javax.swing.JTextField(String.valueOf(defInitP));
        panel.add(initField);

        int result = javax.swing.JOptionPane.showConfirmDialog(parent, panel, "ACO Parameters", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (result != javax.swing.JOptionPane.OK_OPTION) return null;

        try {
            int it = Integer.parseInt(iterField.getText());
            double a = Double.parseDouble(alphaField.getText());
            double b = Double.parseDouble(betaField.getText());
            double r = Double.parseDouble(rhoField.getText());
            double q = Double.parseDouble(qField.getText());
            double ip = Double.parseDouble(initField.getText());
            return new double[] { it, a, b, r, q, ip };
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(parent, "Invalid input - using defaults.");
            return new double[] { defIter, defAlpha, defBeta, defRho, defQ, defInitP };
        }
    }

    private static void applyAppTheme(javax.swing.JFrame frame, Interface inter, boolean dark) {
        // atualiza cores internas do canvas
        inter.setDarkTheme(dark);

        if (dark) {
            UIManager.put("Button.background", new Color(60, 63, 65));
            UIManager.put("Button.foreground", Color.WHITE);
        } else {
            UIManager.put("Button.background", null);
            UIManager.put("Button.foreground", null);
        }

        // atualiza a UI existente
        SwingUtilities.updateComponentTreeUI(frame);

        // Aplicar fundo escuro apenas aos painéis/botões dentro da janela principal
        java.awt.Color panelBg = dark ? new Color(40, 44, 48) : null;
        java.awt.Color btnBg = dark ? new Color(60, 63, 65) : null;
        java.awt.Color btnFg = dark ? Color.WHITE : null;

        java.awt.Component[] comps = frame.getContentPane().getComponents();
        for (java.awt.Component c : comps) {
            applyComponentThemeRecursive(c, panelBg, btnBg, btnFg);
        }
    }

    private static void applyComponentThemeRecursive(java.awt.Component comp, java.awt.Color panelBg, java.awt.Color btnBg, java.awt.Color btnFg) {
        if (comp instanceof javax.swing.JPanel) {
            if (panelBg != null) comp.setBackground(panelBg);
        }
        if (comp instanceof javax.swing.JButton) {
            javax.swing.JButton b = (javax.swing.JButton) comp;
            if (btnBg != null) b.setBackground(btnBg);
            if (btnFg != null) b.setForeground(btnFg);
        }
        if (comp instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) comp).getComponents()) {
                applyComponentThemeRecursive(child, panelBg, btnBg, btnFg);
            }
        }
    }
}
