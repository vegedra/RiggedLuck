/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Criação da tela e interface
*/

package com.github.vegedra.core;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.vegedra.audio.Sound;
import javax.swing.*;
import java.awt.*;

public class UI {

    // Variaveis e objetos
    private Player player;
    public static GameManager gameManager;
    private TimerManager timerManager;

    private Timer messageTimer;
    public JFrame window;
    public JLabel counterLabel, effectLabel, luckLabel, cpsLabel;
    public JButton rollButton;
    private static final int MAX_CARDS = 9;
    public JPanel[] cardSlots = new JPanel[MAX_CARDS];

    public JPanel titlePanel, gamePanel, pausePanel;
    private Font font1, font2, font3, buttonFont;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Construtor
    public UI(Player player) { this.player = player; }

    // Adiciona o Timer Manager no construtor depois
    public void setTimerManager(TimerManager tm) { this.timerManager = tm; }

    // Cria e carrega fontes
    private void createFont() {
        font1 = new Font("Cambria", Font.PLAIN, 32);
        font2 = new Font("Cambria", Font.PLAIN, 15);
        font3 = new Font("Cambria", Font.BOLD, 40);
        buttonFont = new Font("Cambria", Font.BOLD, 18);
    }

    // Cria a interface do jogo
    public void createUI(Main.ClickerHandler cHandler) {
        // Cria as fontes
        createFont();

        // Usa o FlatLaf para deixar o Swing mais bonito
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Erro ao iniciar o FlatLaf");
        }

        // Criação da janela do jogo
        window = new JFrame();
        window.setSize(800, 600);
        window.setResizable(false);
        window.setTitle("Rigged Luck");
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Botão de confirmação para fechar janela
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (gameManager == null) {
                    System.exit(0);
                    return;
                }

                switch (gameManager.state) {
                    case TITLE:
                        int optionTitle = JOptionPane.showConfirmDialog(
                                window,
                                "Tem certeza que deseja sair do jogo?",
                                "Fechar jogo",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (optionTitle == JOptionPane.YES_OPTION) {
                            gameManager.shutdown();
                            System.exit(0);
                        }
                        break;

                    case GAME:
                    case PAUSED:
                    case GAME_OVER:
                        int optionGame = JOptionPane.showConfirmDialog(
                                window,
                                "Deseja realmente voltar ao menu inicial?\nO progresso atual será perdido.",
                                "Voltar ao menu",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (optionGame == JOptionPane.YES_OPTION) {
                            timerManager.stopGameTimers();
                            switchTo("title");
                            gameManager.state = GameManager.GameState.TITLE;
                            Sound.BG1.playMusic();
                        }
                        break;

                    default:
                        // Qualquer outro estado não previsto (segurança)
                        break;
                }
            }
        });

        // Container principal (cardLayout)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setPreferredSize(new Dimension(800, 600));
        window.setContentPane(mainPanel);

        // Game Panel - tela de jogo
        gamePanel = new JPanel();
        gamePanel.setLayout(null);  // absolute positioning
        gamePanel.setBackground(Color.white);

        // Clicker (circulo magico)
        JPanel clickerPanel = new JPanel();
        clickerPanel.setBounds(55, 190, 250, 255);
        clickerPanel.setBackground(Color.white);
        clickerPanel.setBorder(null);
        gamePanel.add(clickerPanel);
        // Imagem do clicker
        ImageIcon circle = new ImageIcon(getClass().getResource("/images/circle.png"));
        JButton clickerButton = new JButton();
        clickerButton.setBackground(Color.white);
        clickerButton.setFocusPainted(false);
        clickerButton.setBorder(null);
        clickerButton.setIcon(circle);
        clickerButton.addActionListener(cHandler);
        clickerButton.setActionCommand("clicker");
        clickerPanel.add(clickerButton);

        // Counter panel
        JPanel counterPanel = new JPanel();
        counterPanel.setBounds(50, 40, 200, 100);
        counterPanel.setBackground(Color.white);
        counterPanel.setLayout(new GridLayout(2, 1));
        gamePanel.add(counterPanel);

        // Counter label
        counterLabel = new JLabel(player.getCoins() + " moedas");
        counterLabel.setForeground(Color.black);
        counterLabel.setFont(font1);
        counterPanel.add(counterLabel);

        // Effect panel (textos e mensagens)
        JPanel effectPanel = new JPanel();
        effectPanel.setLayout(new GridLayout(2, 1));
        effectPanel.setBackground(Color.white);
        counterPanel.add(effectPanel);
        // Effect label
        effectLabel = new JLabel("");
        effectLabel.setFont(font2);
        effectLabel.setForeground(Color.darkGray);
        effectLabel.setHorizontalAlignment(SwingConstants.LEFT);
        // Coins per Second
        cpsLabel = new JLabel("Moedas por segundo: 0");
        cpsLabel.setFont(font2);
        cpsLabel.setForeground(Color.gray);
        cpsLabel.setHorizontalAlignment(SwingConstants.LEFT);

        effectPanel.add(effectLabel);
        effectPanel.add(cpsLabel);

        // Luck label
        luckLabel = new JLabel("Sorte: " + player.getLuck() + "%");
        luckLabel.setBounds(500, 30, 100, 50);
        luckLabel.setForeground(Color.black);
        luckLabel.setFont(font2);
        luckLabel.setToolTipText("Quanto maior a sorte, melhores os resultados de 'Sortear'!\nNão deixe chegar em 0%!");
        gamePanel.add(luckLabel);

        // Roll button
        rollButton = new JButton("Sortear (20 moedas)");    // É atualizado no gameManager
        rollButton.setBounds(500, 70, 200, 50);
        rollButton.setBackground(Color.yellow);
        rollButton.setFont(font2);
        rollButton.setFocusPainted(false);
        rollButton.setActionCommand("roll");
        rollButton.addActionListener(cHandler);
        gamePanel.add(rollButton);

        // Botão de pausa
        JButton pauseButton = new JButton();
        pauseButton.setBounds(740, 10, 50, 50);
        pauseButton.setFocusPainted(false);
        pauseButton.setBorder(null);
        pauseButton.setActionCommand("pause");
        pauseButton.addActionListener(cHandler);
        // Imagem - Tenta carregar a imagem de fundo
        try {
            ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/images/pause.png"));
            pauseButton.setIcon(pauseIcon);
        } catch (Exception e) {
            // Fallback: fundo azul se a imagem não carregar
            pauseButton.setBackground(Color.gray);
        }
        gamePanel.add(pauseButton);

        // Slots das cartas - estilo hotbar horizontal
        JPanel cardPanel = new JPanel();
        cardPanel.setBounds(50, 480, 700, 100);                 // x=50, y=450, largura 700, altura 100
        cardPanel.setLayout(new GridLayout(1, 9, 5, 0));    // 1 linha, 9 colunas, espaçamento horizontal 5px
        cardPanel.setBackground(Color.white);
        cardPanel.setBorder(BorderFactory.createTitledBorder("Cartas Ativas"));
        for (int i = 0; i < 9; i++) {
            cardSlots[i] = new JPanel();
            cardSlots[i].setBackground(Color.white);
            cardSlots[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cardSlots[i].setLayout(new BorderLayout());
            JLabel empty = new JLabel("Vazio", SwingConstants.CENTER);
            empty.setFont(new Font("Cambria", Font.PLAIN, 10));
            cardSlots[i].add(empty, BorderLayout.CENTER);
            cardPanel.add(cardSlots[i]);
        }
        gamePanel.add(cardPanel);

        // Menu Inicial
        titlePanel = new JPanel();
        titlePanel.setLayout(null);
        titlePanel.setBackground(Color.white);
        // Titulo
        JLabel titleLabel = new JLabel("Rigged Luck", SwingConstants.CENTER);
        titleLabel.setBounds(250, 150, 300, 50);
        titleLabel.setFont(font3);
        titleLabel.setForeground(Color.black);
        titlePanel.add(titleLabel);
        // Iniciar jogo
        JButton startButton = new JButton("Iniciar Jogo");
        startButton.setBounds(300, 300, 200, 50);
        startButton.setFont(buttonFont);
        startButton.setFocusPainted(false);
        startButton.setActionCommand("start");
        startButton.addActionListener(cHandler);
        titlePanel.add(startButton);
        // Sair
        JButton exitButton = new JButton("Sair");
        exitButton.setBounds(300, 370, 200, 50);
        exitButton.setFont(buttonFont);
        exitButton.setFocusPainted(false);
        exitButton.setActionCommand("exit");
        exitButton.addActionListener(cHandler);
        titlePanel.add(exitButton);
        // Versão e copyright
        JLabel versionLabel = new JLabel("Versão 0.0.9 © 2026 Digital Cake Studio", SwingConstants.CENTER);
        versionLabel.setBounds(245, 560, 300, 30);
        versionLabel.setFont(new Font("Cambria", Font.PLAIN, 16));
        versionLabel.setForeground(Color.gray);
        titlePanel.add(versionLabel);

        // Menu de pausa
        pausePanel = new JPanel();
        pausePanel.setLayout(null);
        pausePanel.setBackground(Color.white);

        JLabel pausedLabel = new JLabel("Jogo Pausado!", SwingConstants.CENTER);
        pausedLabel.setFont(font3);
        pausedLabel.setForeground(Color.black);
        pausedLabel.setBounds(200, 200, 400, 100);
        pausePanel.add(pausedLabel);

        JButton resumeButton = new JButton("Continuar");
        resumeButton.setBounds(300, 350, 200, 50);
        resumeButton.setFont(buttonFont);
        resumeButton.setFocusPainted(false);
        resumeButton.setActionCommand("resume");
        resumeButton.addActionListener(cHandler);
        pausePanel.add(resumeButton);

        JButton exitToMenuButton = new JButton("Sair");
        exitToMenuButton.setBounds(300, 415, 200, 50);
        exitToMenuButton.setFont(buttonFont);
        exitToMenuButton.setFocusPainted(false);
        exitToMenuButton.setActionCommand("exitToMenu");
        exitToMenuButton.addActionListener(cHandler);
        pausePanel.add(exitToMenuButton);

        mainPanel.add(pausePanel, "pause");

        // Adiciona os panels pro cardLayout
        mainPanel.add(titlePanel, "title");
        mainPanel.add(gamePanel, "game");

        // Se o GameManager já existir, atualiza o custo da roleta
        if (gameManager != null) {
            gameManager.updateRollCost();
        }

        window.pack();  // Ajusta a janela pro tamanho selecionado
        window.setVisible(true);
    }

    // Trocar telas no VisibilityManager
    public void switchTo(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    // Mostrar mensagens
    public void showMessage(String message, Color color) {
        effectLabel.setText(message);
        effectLabel.setForeground(color);

        // Reinicia o timer da exibição das mensagens
        if (messageTimer != null && messageTimer.isRunning()) {
            messageTimer.stop();
        }
        messageTimer = new Timer(3000, e -> effectLabel.setText(""));
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    // Mensagem inicial
    public void introMessage(int mode) {
        String title = "História";
        String msg = "";

        switch (mode) {
            // Normal
            case 0:
                msg = "Ao iniciar um ritual com o objetivo de ficar rico, você se encontra preso no mesmo...\n" +
                        "Consiga o máximo de moedas que conseguir antes de encontrar seu iminente fim!";
                break;
            // Time Attack
            case 1:
                msg = "Ao iniciar um ritual com o objetivo de ficar rico, você se encontra preso no mesmo...\n" +
                        "Consiga o número de moedas solicitado dentro do tempo limite!";
                break;
            // Maldição (Sandbox)
            case 2:
                msg = "Ao iniciar um ritual com o objetivo de ficar rico, você se encontra preso no mesmo...\n" +
                        "Consiga juntar 1 bilhão de moedas o mais rápido que conseguir!";
                break;
            default:
                return;
        }
        JOptionPane.showMessageDialog(window, msg, title, JOptionPane.PLAIN_MESSAGE);
    }

    // Seleção do modo de jogo
    public int gameModeSelect() {
        String message =
                "<html><b>Escolha o modo de jogo:</b><br><br>" +
                        "<b>Normal</b> - Modo padrão infinito.<br>" +
                        "<b>Time-Attack</b> - Consiga moedas antes do tempo acabar!<br>" +
                        "<b>Maldição</b> - Consiga juntar 1 bilhão de moedas o mais rápido possível!<br>" +
                        "</html>";
        Object[] options = { "Normal", "Time-Attack", "Maldição" };

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
