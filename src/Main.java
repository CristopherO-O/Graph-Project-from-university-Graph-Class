/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: Main
 */

package src;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;

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

            // calcula o algoritimo instantaneamente
            JButton btnANT = new JButton("Instant Ant");
            btnANT.addActionListener(e -> inter.executarAntInstantaneo());
            topPanel.add(btnANT);

            //executa o algoritimo de forma visual
            JButton btnAntAnim = new JButton("Animated Ant");
            btnAntAnim.addActionListener(e -> inter.executarAntAnimado());
            topPanel.add(btnAntAnim);


            // Adiciona o painel ao topo da janela
            frame.add(topPanel, java.awt.BorderLayout.NORTH);

            // >>>>>Painel inferior (para o botão Limpar Caminho)<<<<<
            javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
            bottomPanel.setLayout(new java.awt.FlowLayout());

            // Botao Clear
            javax.swing.JButton btnClear = new javax.swing.JButton("Clear Results");
            btnClear.addActionListener(e -> inter.clearPath());
            bottomPanel.add(btnClear);

            // Adiciona o em baixo da janela
            frame.add(bottomPanel, java.awt.BorderLayout.SOUTH);

            // janela  
            frame.add(inter);
            frame.setTitle("Melhor exercicios de grafos que você vai ver na vida");
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            g.printGraph();
            
            new Thread(inter).start();

        } catch (Exception e) {
            System.out.println("Error loading Graph:");
            e.printStackTrace();
        }
    }
}
