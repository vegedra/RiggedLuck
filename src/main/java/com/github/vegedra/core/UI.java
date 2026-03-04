package com.github.vegedra.core;

import javax.swing.*;
import java.awt.*;

public class UI {

    // Variaveis e objetos
    private Player player;
    public static GameManager gameManager;
    private Timer messageTimer;
    JLabel counterLabel, effectLabel, luckLabel, cpsLabel;
    JPanel[] cardSlots = new JPanel[4];
    Font font1, font2;

    public UI(Player player) {
        this.player = player;
    }

    // UI do jogo (JFrame)
    public void createUI(Main.ClickerHandler cHandler) {
        // Cria as fontes
        createFont();

        // Criação da janela do jogo
        JFrame window = new JFrame();
        window.setSize(800, 600);
        window.setResizable(false);
        window.setTitle("Rigged Luck");
        window.getContentPane().setBackground(Color.white);
        window.setLayout(null);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Botão de confirmação para fechar janela
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

                // Fecha o jogo
                if (option == javax.swing.JOptionPane.YES_OPTION) {
                    gameManager.shutdown();
                    System.exit(0);
                }
                // Se escolher "Não", a janela permanece aberta
            }
        });

        // Clicker (imagem)
        JPanel clickerPanel = new JPanel();
        clickerPanel.setBounds(80, 250, 250, 255);
        clickerPanel.setBackground(Color.white);
        clickerPanel.setBorder(null);
        window.add(clickerPanel);

        // Carrega a imagem do clicker
        ImageIcon circle = new ImageIcon(
                getClass().getResource("/images/circle.png"));

        // Cria o botão para a imagem
        JButton clickerButton = new JButton();
        clickerButton.setBackground(Color.white);
        clickerButton.setFocusPainted(false);
        clickerButton.setBorder(null);
        clickerButton.setIcon(circle);
        clickerButton.addActionListener(cHandler);
        clickerButton.setActionCommand("clicker");
        clickerPanel.add(clickerButton);

        // Panel para o contador
        JPanel counterPanel = new JPanel();
        counterPanel.setBounds(80, 120, 200, 100);
        counterPanel.setBackground(Color.white);
        counterPanel.setLayout(new GridLayout(2, 1));
        window.add(counterPanel);

        // Texto para o contador de moedas e efeitos (abaixo)
        counterLabel = new JLabel(player.getCoins() + " moedas");
        counterLabel.setForeground(Color.black);
        counterLabel.setFont(font1);
        counterPanel.add(counterLabel);

        JPanel effectPanel = new JPanel();
        effectPanel.setLayout(new GridLayout(2,1));
        effectPanel.setBackground(Color.white);
        counterPanel.add(effectPanel);

        effectLabel = new JLabel("");
        effectLabel.setFont(font2);
        effectLabel.setForeground(Color.darkGray);
        effectLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel cpsLabel = new JLabel("Moedas por segundo: 0");
        cpsLabel.setFont(font2);
        cpsLabel.setForeground(Color.gray);
        cpsLabel.setHorizontalAlignment(SwingConstants.LEFT);

        effectPanel.add(effectLabel);
        effectPanel.add(cpsLabel);

        // Guardar referência
        this.cpsLabel = cpsLabel;

        // Sorte
        luckLabel = new JLabel("Sorte: " + player.getLuck() + "%");
        luckLabel.setBounds(500, 30, 100, 50);
        luckLabel.setForeground(Color.black);
        luckLabel.setFont(font2);
        luckLabel.setToolTipText("Quanto maior a sorte, melhores os resultados da roleta!");
        window.add(luckLabel);

        // Botão da roleta
        JButton rollButton = new JButton("Roletar (10 moedas)");
        rollButton.setBounds(500, 70, 200, 50);
        rollButton.setBackground(Color.yellow);
        rollButton.setFont(font2);
        rollButton.setFocusPainted(false);
        rollButton.setActionCommand("roll");
        rollButton.addActionListener(cHandler);
        window.add(rollButton);

        // Cartas
        JPanel cardPanel = new JPanel();
        cardPanel.setBounds(500, 250, 250, 250);
        cardPanel.setLayout(new GridLayout(4, 1));

        // Cria os slots para as cartas
        for (int i = 0; i < 4; i++) {
            cardSlots[i] = new JPanel();
            cardSlots[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));

            JLabel empty = new JLabel("Vazio", SwingConstants.CENTER);
            cardSlots[i].add(empty);

            cardPanel.add(cardSlots[i]);
        }
        window.add(cardPanel);

        // Carrega e exibe tudo
        window.setVisible(true);
    }

    // Cria e carrega fontes
    public void createFont() {
        font1 = new Font("Cambria", Font.PLAIN, 32);
        font2 = new Font("Cambria", Font.PLAIN, 15);
    }

    // Mostrar mensagens no effectlabel
    public void showMessage(String message, Color color) {

        effectLabel.setText(message);
        effectLabel.setForeground(color);

        if (messageTimer != null && messageTimer.isRunning()) {
            messageTimer.stop();
        }

        messageTimer = new Timer(3000, e -> effectLabel.setText(""));
        messageTimer.setRepeats(false);
        messageTimer.start();
    }
}
