/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    User Interface
*/

package com.github.vegedra.core;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class UI {

    // Variaveis e objetos
    private Player player;
    public static GameManager gameManager;
    private Timer messageTimer;
    public JFrame window;
    public JLabel counterLabel, effectLabel, luckLabel, cpsLabel;
    public JPanel[] cardSlots = new JPanel[4];

    // Panels for title screen and game screen (to be used by VisibilityManager)
    public JPanel titlePanel, gamePanel;
    private Font font1, font2;

    // Construtor
    public UI(Player player) {
        this.player = player;
    }

    // Cria e carrega fontes
    public void createFont() {
        font1 = new Font("Cambria", Font.PLAIN, 32);
        font2 = new Font("Cambria", Font.PLAIN, 15);
    }

    // UI do jogo (JFrame)
    public void createUI(Main.ClickerHandler cHandler) {
        // Cria as fontes
        createFont();

        // Carrega o FlatLaf (deixa o swing mais bonito)
        try {
            UIManager.setLookAndFeel( new FlatLightLaf() );
        } catch( Exception ex ) {
            System.err.println( "Erro em iniciar o FlatLaf" );
        }

        // Criação da janela do jogo
        window = new JFrame();
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
                    if (gameManager != null) {
                        gameManager.shutdown();
                    }
                    System.exit(0);
                }
                // Se escolher "Não", a janela permanece aberta
            }
        });

        // Game Panel (jogo)
        gamePanel = new JPanel();
        gamePanel.setBounds(0, 0, 800, 600);
        gamePanel.setLayout(null);
        gamePanel.setBackground(Color.white);
        gamePanel.setVisible(false); // Start hidden, title screen visible first

        // Clicker (imagem)
        JPanel clickerPanel = new JPanel();
        clickerPanel.setBounds(80, 250, 250, 255);
        clickerPanel.setBackground(Color.white);
        clickerPanel.setBorder(null);
        gamePanel.add(clickerPanel);

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
        gamePanel.add(counterPanel);

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

        cpsLabel = new JLabel("Moedas por segundo: 0");
        cpsLabel.setFont(font2);
        cpsLabel.setForeground(Color.gray);
        cpsLabel.setHorizontalAlignment(SwingConstants.LEFT);

        effectPanel.add(effectLabel);
        effectPanel.add(cpsLabel);

        // Sorte
        luckLabel = new JLabel("Sorte: " + player.getLuck() + "%");
        luckLabel.setBounds(500, 30, 100, 50);
        luckLabel.setForeground(Color.black);
        luckLabel.setFont(font2);
        luckLabel.setToolTipText("Quanto maior a sorte, melhores os resultados da roleta!");
        gamePanel.add(luckLabel);

        // Botão da roleta
        JButton rollButton = new JButton("Sortear (20 moedas)");
        rollButton.setBounds(500, 70, 200, 50);
        rollButton.setBackground(Color.yellow);
        rollButton.setFont(font2);
        rollButton.setFocusPainted(false);
        rollButton.setActionCommand("roll");
        rollButton.addActionListener(cHandler);
        gamePanel.add(rollButton);

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
        gamePanel.add(cardPanel);

        // Menu Inicial
        titlePanel = new JPanel();
        titlePanel.setBounds(0, 0, 800, 600);
        titlePanel.setBackground(Color.white);
        titlePanel.setLayout(null);
        titlePanel.setVisible(true);

        JLabel titleLabel = new JLabel("Rigged Luck", SwingConstants.CENTER);
        titleLabel.setBounds(250, 150, 300, 50);
        titleLabel.setFont(new Font("Cambria", Font.BOLD, 40));
        titleLabel.setForeground(Color.black);
        titlePanel.add(titleLabel);

        /*
        JLabel subtitleLabel = new JLabel("x");
        subtitleLabel.setBounds(250, 200, 300, 30);
        subtitleLabel.setFont(new Font("Cambria", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.gray);
        titlePanel.add(subtitleLabel);
        */

        JButton startButton = new JButton("Iniciar Jogo");
        startButton.setBounds(300, 300, 200, 50);
        startButton.setFont(new Font("Cambria", Font.BOLD, 18));
        //startButton.setBackground(Color.green);
        startButton.setFocusPainted(false);
        startButton.setActionCommand("start");
        startButton.addActionListener(cHandler);
        titlePanel.add(startButton);

        JButton exitButton = new JButton("Sair");
        exitButton.setBounds(300, 370, 200, 50);
        exitButton.setFont(new Font("Cambria", Font.BOLD, 18));
        //exitButton.setBackground(Color.red);
        exitButton.setFocusPainted(false);
        exitButton.setActionCommand("exit");
        exitButton.addActionListener(cHandler);
        titlePanel.add(exitButton);

        JLabel versionLabel = new JLabel("Versão 0.0.2 - © 2026 Digital Cake Studio", SwingConstants.CENTER);
        versionLabel.setBounds(250, 500, 300, 30);
        versionLabel.setFont(new Font("Cambria", Font.PLAIN, 16));
        versionLabel.setForeground(Color.gray);
        titlePanel.add(versionLabel);

        // Adiciona os panels no window
        window.add(titlePanel);
        window.add(gamePanel);

        // Carrega e exibe tudo
        window.setVisible(true);
    }

    // Mostrar mensagens no effectlabel
    public void showMessage(String message, Color color) {

        // Aplica o texto e cor
        effectLabel.setText(message);
        effectLabel.setForeground(color);

        // Duração de mensagem
        if (messageTimer != null && messageTimer.isRunning()) {
            messageTimer.stop();
        }
        messageTimer = new Timer(3000, e -> effectLabel.setText(""));
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    // Mensagem de contexto da historia
    public void introMessage(int mode) {
        switch(mode) {
            // Normal
            case 1:
                JOptionPane.showMessageDialog(window,
                        "Ao iniciar um ritual com o objetivo de ficar rico, você se encontra preso no mesmo...\nConsiga o máximo de moedas que conseguir antes de encontrar seu iminente fim!",
                        "História",
                        JOptionPane.PLAIN_MESSAGE);
            // Time Attack
            case 2:
                JOptionPane.showMessageDialog(window,
                "Ao iniciar um ritual com o objetivo de ficar rico, você se encontra preso no mesmo...\nConsiga o número de moedas solicitado dentro do tempo limite!",
                "História",
                JOptionPane.PLAIN_MESSAGE);
            // Maldição
            case 3:
                JOptionPane.showMessageDialog(window,
                        "Ao iniciar um ritual com o objetivo de ficar rico, você se encontra preso no mesmo...\nConsiga juntar 1 bilhão de moedas o mais rápido que conseguir!",
                        "História",
                        JOptionPane.PLAIN_MESSAGE);
        }
    }

    // Opção de modo de jogo
    public int gameModeSelect() {
        // Texto
        String message =
                "<html><b>Escolha o modo de jogo:</b><br><br>" +
                        "<b>Normal</b> - Modo padrão infinito.<br>" +
                        "<b>Time-Attack</b> - Consiga moedas antes do tempo acabar!<br>" +
                        "<b>Sandbox</b> - Consiga juntar 1 bilhão de moedas o mais rápido possível!<br>" +
                        "</html>";
        // Opções
        Object[] options = {
                "Normal",
                "Time-Attack",
                "Maldição"
        };

        return JOptionPane.showOptionDialog(
                null,
                message,
                "Modo de Jogo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null
        );
    }
}