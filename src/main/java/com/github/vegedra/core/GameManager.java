/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Gerenciador do jogo
*/

package com.github.vegedra.core;

import com.github.vegedra.audio.Sound;
import com.github.vegedra.cards.Card;
import com.github.vegedra.cards.CardGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameManager {

    // Variáveis e objetos
    public Player player;
    private UI ui;
    private static final int MAX_CARDS = 9;
    private Card[] activeCards = new Card[MAX_CARDS];
    private CardGenerator generator = new CardGenerator();
    public Timer timer, messageTimer;
    private VisibilityManager vm;
    private ActionListener cHandler;

    public int secondsElapsed = 0;
    public boolean firstTimePlaying = false;
    public int currentGameMode;
    private int clicksThisSecond = 0;
    public int rollCost;
    public int rollsMade = 0;

    // Estados de jogo
    public enum GameState { TITLE, GAME, GAME_OVER }
    public GameState state = GameState.TITLE;

    // Construtor
    public GameManager(Player player, UI ui, ActionListener cHandler, VisibilityManager vm) {
        this.player   = player;
        this.ui       = ui;
        this.cHandler = cHandler;
        this.vm       = vm;
        this.rollCost = 20;
    }

    public void setVisibilityManager(VisibilityManager vm) { this.vm = vm; }

    // Atualização de UI
    public void updateCounter()   { ui.counterLabel.setText(player.getCoins() + " moedas"); }
    public void updateCPSLabel()  { ui.cpsLabel.setText("Moedas por segundo: " + getTotalCPS()); }
    public void updateLuckLabel() { ui.luckLabel.setText("Sorte: " + player.getLuck() + "%"); }
    // Atualização do custo do sortear (UI e logica)
    public void updateRollCost() {
        if (ui != null && ui.rollButton != null) {
            // Formula para o custo do roll
            rollCost = 20 + (rollsMade * 15);

            // alternativa1: rollCost = (int)(20 + (rollsMade * 12) + Math.pow(1.15, rollsBought) * 10);
            // alternativa2: rollCost = (int)(20 * Math.pow(1.6, rollsMade));

            ui.rollButton.setText("Sortear (" + rollCost + " moedas)");
        }
    }

    // Timer principal
    public void startTimer() {
        timer = new Timer(1000, e -> {
            if (state == GameState.GAME_OVER) { timer.stop(); return; }
            secondsElapsed++;

            // Geração passiva de ouro
            player.changeCoins(getTotalCPS());

            // Sorte passiva
            player.changeLuck(Math.round(getTotalLuckPerSecond())); // sorte passiva

            // Oscilação + tendência natural da sorte
            updateLuck(0);

            updateRollCost();
            updateCounter();
            updateCPSLabel();
            updateLuckLabel();
            // TODO: checkCollectorSpawn();

            checkGameOver();
        });
        timer.start();
    }

    // Calculo de totais
    public int getTotalCPS() {
        int total = 0;

        // CPS base das cartas
        for (Card c : activeCards) {
            if (c != null) total += c.coinsPerSecond;
        }
        return total;
    }
    private float getTotalLuckPerSecond() {
        float total = 0;
        for (Card c : activeCards) {
            if (c != null) total += c.luckPerSecond;
        }
        return total;
    }

    // Oscilação da sorte (chamado a cada segundo, com cliques no período)
    private void updateLuck(int clicksInSecond) {
        // Tendência negativa cresce com o tempo
        float minutes = secondsElapsed / 60f;
        float tendency = 0.5f + minutes * 0.15f;
        tendency = Math.min(tendency, 3.5f);

        // Oscilação aleatória crescente
        float oscBase = 2f + minutes * 0.4f;
        oscBase = Math.min(oscBase, 12f);
        float variation = (float)(Math.random() * oscBase * 2) - oscBase;

        // Bônus por clique (inclui bônus das cartas)
        float clickBonus = 0.2f; // base
        for (Card c : activeCards) {
            if (c != null) clickBonus += c.luckPerClick;
        }

        float delta = variation - tendency + (clickBonus * clicksInSecond);
        player.changeLuck(Math.round(delta));
        updateLuckLabel();
    }

    // Clicker
    public void handleClick() {
        int baseGold = player.getClickValue();
        player.changeCoins(baseGold);
        Sound.CLICK.play();

        // Acumula cliques para o próximo tick de sorte
        // (precisamos de um contador de cliques no segundo atual)
        clicksThisSecond++; // variável de instância a ser adicionada
        updateCounter();
        checkGameOver();
    }

    // Sorteio da carta
    public void rollCard() {
        if (player.getCoins() < rollCost) {
            ui.showMessage("Moedas insuficientes!", Color.RED);
            return;
        }

        int emptyIndex = -1;
        for (int i = 0; i < activeCards.length; i++) {
            if (activeCards[i] == null) { emptyIndex = i; break; }
        }
        if (emptyIndex == -1) {
            ui.showMessage("Limite de cartas atingido!", Color.RED);
            return;
        }

        player.changeCoins(-rollCost);
        Sound.ROLL.play();
        rollsMade++;

        Card newCard = generator.generateCard(player.getLuck());
        activeCards[emptyIndex] = newCard;

        // Aplica efeitos da carta no jogador
        player.addClickValue(newCard.clickValue);

        updateCardUI(emptyIndex);
        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();
        ui.showMessage("Nova carta: " + newCard.name, Color.GREEN);
    }

    // Descarte de carta
    public void handleDiscard(int index) {
        Card c = activeCards[index];
        if (c == null) return;

        int discardCost = calcDiscardCost(c);
        if (player.getCoins() < discardCost) {
            ui.showMessage("Moedas insuficientes!", Color.RED);
            return;
        }

        player.changeCoins(-discardCost);
        player.addClickValue(-c.clickValue);    // Remove bônus de clique
        activeCards[index] = null;              // Esvazia o slot

        updateCardUI(index);
        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();

        ui.showMessage("Carta descartada (-" + discardCost + " moedas)", Color.GREEN);
        Sound.DISCARD.play();
    }

    // Custo de descarte: baseado em clickValue, CPS e efeito de sorte. */
    private int calcDiscardCost(Card c) {
        // Cartas mais poderosas custam mais pra descartar
        int base = (Math.abs(c.clickValue) + Math.abs(c.coinsPerSecond)) * 2;
        // Cartas de Sorte/Risco/Sinérgicas têm custo mínimo de 10
        if (c.type == Card.CardType.LUCK || c.type == Card.CardType.RISK || c.type == Card.CardType.SYNERGY) {
            base = Math.max(base, 10);
        }
        return base;
    }


    // UI das cartas
    // Cor das cartas de acordo com sua raridade
    private String getRarityColor(Card.Rarity rarity) {
        switch (rarity) {
            case MYTHIC:   return "#FF44FF";
            case RARE:     return "#44AAFF";
            case UNCOMMON: return "#44FF88";
            default:       return "#000000";
        }
    }
    // Atualiza UI das cartas
    public void updateCardUI(int index) {
        ui.cardSlots[index].removeAll();
        Card c = activeCards[index];
        ui.cardSlots[index].setLayout(new BorderLayout());

        if (c == null) {
            JLabel empty = new JLabel("Vazio", SwingConstants.CENTER);
            empty.setFont(new Font("Cambria", Font.PLAIN, 12));
            ui.cardSlots[index].add(empty, BorderLayout.CENTER);
            ui.cardSlots[index].setToolTipText(null);
        } else {
            // Garantir que nome e descrição não sejam nulos
            String cardName = (c.name != null) ? c.name : "Carta sem nome";
            String cardDesc = (c.desc != null) ? c.desc : "";

            // Nome com cor da raridade
            String rarityColor = getRarityColor(c.rarity);
            JLabel nameLabel = new JLabel(
                    "<html><font color='" + getRarityColor(c.rarity) + "'>" + cardName + "</font></html>",
                    SwingConstants.CENTER
            );
            nameLabel.setFont(new Font("Cambria", Font.BOLD, 11)); // tamanho ajustável

            // Botão de descarte pequeno
            JButton discard = new JButton("X");
            discard.setFocusPainted(false);
            discard.setMargin(new Insets(2, 4, 2, 4));
            discard.setFont(new Font("Cambria", Font.PLAIN, 10));
            discard.setActionCommand("discard_" + index);
            discard.addActionListener(cHandler);

            ui.cardSlots[index].add(nameLabel, BorderLayout.CENTER);
            ui.cardSlots[index].add(discard, BorderLayout.EAST);

            // Tooltip completo
            int discardCost = calcDiscardCost(c);
            String tooltip = "<html><b>" + cardName + " (" + c.rarity + ")</b><br><br>" +
                    cardDesc + "<br>" +
                    "Clique: " + c.clickValue + "<br>" +
                    "Moedas/s: " + c.coinsPerSecond + "<br>" +
                    "Sorte/clique: " + c.luckPerClick + "%<br>" +
                    "Sorte/s: " + c.luckPerSecond + "%<br><br>" +
                    "Descartar custa: " + discardCost + " moedas</html>";
            ui.cardSlots[index].setToolTipText(tooltip);
        }

        ui.cardSlots[index].revalidate();
        ui.cardSlots[index].repaint();
    }


    // Utilitários
    // Conta cartas ativas de um tipo específico
    public int countCardsOfType(Card.CardType type) {
        int count = 0;
        for (Card c : activeCards) {
            if (c != null && c.type == type) count++;
        }
        return count;
    }

    // Conta total de cartas ativas
    public int countActiveCards() {
        int count = 0;
        for (Card c : activeCards) { if (c != null) count++; }
        return count;
    }

    // Mostrar tempo formatado
    public String formatTime(int totalSeconds) {
        int hours   = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Para os timers
    public void stopGameTimers() {
        if (timer != null)        { timer.stop();        timer = null; }
        if (messageTimer != null) { messageTimer.stop(); messageTimer = null; }
    }

    // Game Over
    private void checkGameOver() {
        if (player.getLuck() <= 0 || player.getCoins() < 0) {
            state = GameState.GAME_OVER;
            vm.showGameOverScreen();
            System.out.println("Game Over!");
        }
    }

    // Modo de jogo
    public void configureGameMode(int mode) {
        currentGameMode = mode;
    }

    // Reset
    public void resetGame() {
        stopGameTimers();
        player.reset();

        // Remove cartas ativas
        for (int i = 0; i < activeCards.length; i++) {
            activeCards[i] = null;
            updateCardUI(i);
        }

        // Reseta variaveis
        secondsElapsed     = 0;
        rollsMade          = 0;
        rollCost           = 20;
        firstTimePlaying   = false;
        clicksThisSecond = 0;

        // Atualiza UI
        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();
    }

    // Fechar tudo
    public void shutdown() {
        stopGameTimers();
        Sound.closeAll();
    }
}