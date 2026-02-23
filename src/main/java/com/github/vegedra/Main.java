package com.github.vegedra;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    // Variaveis e objetos
    JLabel counterLabel, effectLabel;
    Font font1, font2;
    ClickerHandler cHandler = new ClickerHandler();
    int coinCounter;

    // Inicio
    public static void main(String[] args) {
        new Main();
    }

    // Construtor
    public Main() {
        // Inicializa o contador
        coinCounter = 0;

        // Cria as fontes para serem usadas
        createFont();

        // Cria a tela
        createUI();
    }

    // Cria e carrega as fontes
    public void createFont() {
        font1 = new Font("Cambria", Font.PLAIN, 32);
        font2 = new Font("Cambria", Font.PLAIN, 15);
    }

    // UI do jogo (JFrame)
    public void createUI() {
        // Criação da janela do jogo
        JFrame window = new JFrame();
        window.setSize(800, 600);
        window.setResizable(false);
        window.setTitle("Rigged Luck");
        window.getContentPane().setBackground(Color.white);
        window.setLayout(null);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Botão de confirmação fechar janela
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = javax.swing.JOptionPane.showConfirmDialog(
                        window,
                        "Tem certeza que deseja sair do jogo?",
                        "Fechar jogo",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE
                );

                if (option == javax.swing.JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
                // Se escolher "Não", a janela permanece aberta
            }
        });

        // Clicker (imagem)
        JPanel clickerPanel = new JPanel();
        clickerPanel.setBounds(80, 250, 250, 255);
        window.add(clickerPanel);

        // Carrega a imagem de fundo
        ImageIcon circle = new ImageIcon(
                getClass().getResource("/images/circle.png"));

        // Cria o botão para a imagem
        JButton clickerButton = new JButton();
        clickerButton.setBackground(Color.white);
        clickerButton.setFocusPainted(false);
        clickerButton.setBorder(null);
        clickerButton.setIcon(circle);
        clickerButton.addActionListener(cHandler);
        clickerPanel.add(clickerButton);

        // Panel para o contador
        JPanel counterPanel = new JPanel();
        counterPanel.setBounds(80, 120, 200, 100);
        counterPanel.setBackground(Color.white);
        counterPanel.setLayout(new GridLayout(2, 1));
        window.add(counterPanel);

        // Texto para o contador de moedas e efeitos (abaixo)
        counterLabel = new JLabel(coinCounter + " moedas");
        counterLabel.setForeground(Color.black);
        counterLabel.setFont(font1);
        counterPanel.add(counterLabel);

        effectLabel = new JLabel();
        effectLabel.setForeground(Color.black);
        effectLabel.setFont(font2);
        counterPanel.add(effectLabel);

        // Carrega e exibe tudo
        window.setVisible(true);
    }

    // Ações e eventos (click)
    public class ClickerHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // Incrementa moedas ao clicar
            coinCounter++;
            counterLabel.setText(coinCounter + " moedas");
            //System.out.println(coinCounter);
        }
    }
}