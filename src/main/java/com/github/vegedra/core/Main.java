package com.github.vegedra.core;

import com.github.vegedra.cards.Card;
import com.github.vegedra.cards.CardGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    // Variaveis e objetos
    JLabel counterLabel, effectLabel, luckLabel, cpsLabel;
    Card[] activeCards = new Card[4];
    JPanel[] cardSlots = new JPanel[4];
    Font font1, font2;
    ClickerHandler cHandler = new ClickerHandler();
    CardGenerator generator = new CardGenerator();
    Player player = new Player();
    Timer timer, messageTimer;

    // Inicio
    public static void main(String[] args) {
        new Main();
    }

    // Construtor
    public Main() {
        // Cria as fontes para serem usadas
        createFont();

        // Gerar tooltip mais rápido
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        // Cria a tela
        createUI();

        // Para gerar moedas passivamente
        startPassiveIncome();
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
        clickerPanel.setBackground(Color.white);
        clickerPanel.setBorder(null);
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

    // Atualizar labels
    public void updateCounter() {
        counterLabel.setText(player.getCoins() + " moedas");
    }
    public void updateCPSLabel() {
        cpsLabel.setText("Moedas por segundo: " + getTotalCPS());
    }

    // Timer (para geração passiva)
    public void startPassiveIncome() {

        timer = new Timer(1000, e -> {

            int totalCPS = getTotalCPS();

            player.addCoins(totalCPS);

            updateCounter();
            updateCPSLabel();
        });

        timer.start();
    }

    // Roleta uma carta nova
    public void rollCard() {

        int cost = 10;

        if (player.getCoins() < cost) {
            showMessage("Moedas insuficientes!", Color.RED);
            return;
        }

        // procura slot vazio
        int emptyIndex = -1;
        for (int i = 0; i < activeCards.length; i++) {
            if (activeCards[i] == null) {
                emptyIndex = i;
                break;
            }
        }

        // Sem espaço para carta nova
        if (emptyIndex == -1) {
            showMessage("Limite de cartas atingido!", Color.RED);
            return;
        }

        // Subtrai o custo da roleta
        player.addCoins(-cost);

        // Cria nova carta de acordo com Sorte atual
        Card newCard = generator.generateCard(player.getLuck());

        // Adiciona a carta nova
        activeCards[emptyIndex] = newCard;
        player.applyCard(newCard);

        // Atualiza a UI
        updateCardUI(emptyIndex);
        updateCounter();
        updateCPSLabel();
        showMessage("Nova carta adquirida!", Color.GREEN);
    }

    // Atualiza a UI das cartas
    public void updateCardUI(int index) {

        cardSlots[index].removeAll();
        Card c = activeCards[index];

        cardSlots[index].setLayout(new BorderLayout());

        if (c == null) {
            JLabel empty = new JLabel("Vazio", SwingConstants.CENTER);
            cardSlots[index].add(empty, BorderLayout.CENTER);
            cardSlots[index].setToolTipText(null); // remove tooltip se não houver carta

        } else {
            int discardCost = (Math.abs(c.value) + c.coinsPerSecond) * 2;
            String valueText = (c.value >= 0 ? "+" : "") + c.value;

            // Mostra apenas o nome
            JLabel name = new JLabel(c.name, SwingConstants.CENTER);

            JButton discard = new JButton("X");
            discard.setFocusPainted(false);
            discard.setMargin(new Insets(2,6,2,6));
            discard.setActionCommand("discard_" + index);
            discard.addActionListener(cHandler);

            cardSlots[index].add(name, BorderLayout.CENTER);
            cardSlots[index].add(discard, BorderLayout.EAST);

            // Tooltip
            String tooltipText =
                    "<html>" +
                            "<b>" + c.name + "</b><br><br>" +
                            c.desc + "<br><br>" +
                            "Click: " + valueText + "<br>" +
                            "CPS: " + c.coinsPerSecond + "<br>" +
                            "Descartar custa: " + discardCost +
                            "</html>";

            cardSlots[index].setToolTipText(tooltipText);
        }

        cardSlots[index].revalidate();
        cardSlots[index].repaint();
    }

    // Pega quantas moedas são geradas por segundo
    public int getTotalCPS() {
        int total = 0;
        for (Card c : activeCards) {
            if (c != null) {
                total += c.coinsPerSecond;
            }
        }
        return total;
    }

    // Mostrar mensagens no effectlabel
    public void showMessage(String message, Color color) {

        effectLabel.setText(message);
        effectLabel.setForeground(color);

        // Se já existir timer, para ele
        if (messageTimer != null && messageTimer.isRunning()) {
            messageTimer.stop();
        }

        messageTimer = new Timer(3000, e -> {
            effectLabel.setText("");
        });

        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    // Ações e eventos (click)
    public class ClickerHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            String action = event.getActionCommand();

            switch (action) {
                // Clique no circulo
                case "clicker":
                    // Incrementa moedas ao clicar
                    player.addCoins(player.getClickValue());

                    // Oscilação da sorte após cada clique
                    int value = (int)(Math.random() * 5) - 2; // -2 até +2
                    player.changeLuck(value);

                    // Atualiza
                    updateCounter();
                    luckLabel.setText("Sorte: " + player.getLuck() + "%");
                    break;

                // Clique na roleta (nova carta)
                case "roll":
                    rollCard();
                    break;

                // Descartar carta
                default:
                    if (action.startsWith("discard_")) {

                        int index = Integer.parseInt(action.split("_")[1]);
                        Card c = activeCards[index];

                        if (c != null) {

                            int discardCost = Math.abs(c.value) * 2;

                            if (player.getCoins() >= discardCost) {

                                player.addCoins(-discardCost);
                                player.addClickValue(-c.value);
                                activeCards[index] = null;

                                updateCardUI(index);
                                updateCounter();
                                updateCPSLabel();

                                showMessage("Carta descartada (-" + discardCost + ")", Color.green);
                            } else {
                                showMessage("Moedas insuficientes para descartar!", Color.RED);
                            }

                            counterLabel.setText(player.getCoins() + " moedas");
                        }
                    }
            }
        }
    }
}