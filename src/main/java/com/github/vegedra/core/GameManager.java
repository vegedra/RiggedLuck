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

    // Variaveis e objetos
    public Player player;
    private UI ui;
    private Card[] activeCards = new Card[4];
    private CardGenerator generator = new CardGenerator();
    public Timer timer, messageTimer;
    private ActionListener cHandler;
    public int secondsElapsed = 0;                     // Tempo de jogo
    public boolean firstTimePlaying = false;
    public int currentGameMode;   // stores the chosen mode (0,1,2)

    // Estados de jogo
    public enum GameState {
        TITLE,
        GAME,
        GAME_OVER
    }
    // Estado padrão ao iniciar
    public GameState state = GameState.TITLE;

    // Construtor
    public GameManager(Player player, UI ui, ActionListener cHandler) {
        this.player = player;
        this.ui = ui;
        this.cHandler = cHandler;
    }

    // Atualizar labels
    public void updateCounter() {
        ui.counterLabel.setText(player.getCoins() + " moedas");
    }
    public void updateCPSLabel() {
        ui.cpsLabel.setText("Moedas por segundo: " + getTotalCPS());
    }
    public void updateLuckLabel() {
        ui.luckLabel.setText("Sorte: " + player.getLuck() + "%");
    }

    // Timer (para geração passiva e tempo de jogo)
    public void startPassiveIncome() {
        timer = new Timer(1000, e -> {
            // Incrementa o tempo de jogo (segundos)
            secondsElapsed++;

            int totalCPS = getTotalCPS();
            player.addCoins(totalCPS);

            // Atualiza a UI
            updateCounter();
            updateCPSLabel();

            // TODO: Spawn dos cobradores
            //checkCollectorSpawn();
        });

        timer.start();
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
    // Formatar tempo de jogo - mostrar no game over
    public String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Altera o modo de jogo - TODO
    public void configureGameMode(int mode) {
        currentGameMode = mode;
        // Any mode‑specific setup (e.g., timers, goals) can be added here later.
        // The intro message is now shown in VisibilityManager.showGameScreen()
    }

    // Verifica condições de game over
    private void checkGameOver() {
        if (player.getLuck() <= 0 || player.getCoins() < 0) {
            // Game over
            state = GameState.GAME_OVER;

            // Notifica o VisibilityManager (precisa ser acessível)
            // Isso será tratado pelo Main através de um timer ou callback (TODO!)
        }
    }

    // Roletar uma carta nova
    public void rollCard() {
        // Custo para roletar
        int cost = 20;

        // Se nao tiver moedas suficientes
        if (player.getCoins() < cost) {
            ui.showMessage("Moedas insuficientes!", Color.RED);
            return;
        }

        // Procura slot vazio
        int emptyIndex = -1;
        for (int i = 0; i < activeCards.length; i++) {
            if (activeCards[i] == null) {
                emptyIndex = i;
                break;
            }
        }

        // Sem espaço para carta nova
        if (emptyIndex == -1) {
            ui.showMessage("Limite de cartas atingido!", Color.RED);
            return;
        }

        //  TODO: chance baixa de spawnar um cobrador instantaneamente

        // Subtrai o custo da roleta
        player.addCoins(-cost);
        // Toca sfx
        Sound.ROLL.play();

        // Cria nova carta de acordo com Sorte atual
        Card newCard = generator.generateCard(player.getLuck());

        // Adiciona a carta nova
        activeCards[emptyIndex] = newCard;
        player.applyCard(newCard);

        // Atualiza a UI
        updateCardUI(emptyIndex);
        updateCounter();
        updateCPSLabel();
        ui.showMessage("Nova carta adquirida!", Color.GREEN);
    }

    // Atualiza o slot das cartas
    public void updateCardUI(int index) {
        // Limpa o slot
        ui.cardSlots[index].removeAll();

        // Pega a carta do slot
        Card c = activeCards[index];

        // Define o layout
        ui.cardSlots[index].setLayout(new BorderLayout());

        // Se não houver carta
        if (c == null) {
            JLabel empty = new JLabel("Vazio", SwingConstants.CENTER);
            ui.cardSlots[index].add(empty, BorderLayout.CENTER);
            ui.cardSlots[index].setToolTipText(null); // remove tooltip se não houver carta

        } else {
            // Exbibe carta e calcula valor do descarte
            int discardCost = (Math.abs(c.value) + c.coinsPerSecond) * 2;
            String valueText = (c.value >= 0 ? "+" : "") + c.value;

            // Mostra apenas o nome
            JLabel name = new JLabel(c.name, SwingConstants.CENTER);

            // Descarte - atualiza UI
            JButton discard = new JButton("X");
            discard.setFocusPainted(false);
            discard.setMargin(new Insets(2,6,2,6));
            discard.setActionCommand("discard_" + index);
            discard.addActionListener(cHandler);

            ui.cardSlots[index].add(name, BorderLayout.CENTER);
            ui.cardSlots[index].add(discard, BorderLayout.EAST);

            // Tooltip
            String tooltipText =
                    "<html>" +
                            "<b>" + c.name + "</b><br><br>" +
                            c.desc + "<br><br>" +
                            "Click: " + valueText + "<br>" +
                            "CPS: " + c.coinsPerSecond + "<br>" +
                            "Descartar custa: " + discardCost +
                            "</html>";

            ui.cardSlots[index].setToolTipText(tooltipText);
        }

        // Recarrega e exibe
        ui.cardSlots[index].revalidate();
        ui.cardSlots[index].repaint();
    }

    // Clicker
    public void handleClick() {
        // Incrementa moedas ao clicar e toca efeito sonoro
        player.addCoins(player.getClickValue());
        Sound.CLICK.play();

        // Oscilação da sorte após cada clique
        int value = (int)(Math.random() * 5) - 2; // -2 até +2
        player.changeLuck(value);

        // Atualiza
        updateCounter();
        ui.luckLabel.setText("Sorte: " + player.getLuck() + "%");
    }

    // Descartar carta
    public void handleDiscard(int index) {
        // Pega a carta no slot informado
        Card c = activeCards[index];

        // Se existir carta
        if (c != null) {
            // Custo do descarte baseado nos valores da carta
            int discardCost = (Math.abs(c.value) + c.coinsPerSecond) * 2;

            // Se tiver moedas, descarta
            if (player.getCoins() >= discardCost) {
                player.addCoins(-discardCost);

                // Retira os efeitos
                player.addClickValue(-c.value);
                player.removeCPS(c.coinsPerSecond);

                // Remove a carta do array
                activeCards[index] = null;

                // Atualiza a UI
                updateCardUI(index);
                updateCounter();
                updateCPSLabel();

                ui.showMessage("Carta descartada (-" + discardCost + ")", Color.green);
                Sound.DISCARD.play();
            } else {
                ui.showMessage("Moedas insuficientes para descartar!", Color.RED);
            }
            updateCounter();
        }
    }

    // Reiniciar o jogo
    public void resetGame() {
        // Reset player
        player.reset();

        // Limpa cartas ativas
        for (int i = 0; i < activeCards.length; i++) {
            activeCards[i] = null;
            updateCardUI(i);
        }

        // Reinicia o timer
        secondsElapsed = 0;
        firstTimePlaying = false;

        // Atualiza UI
        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
    }

    // Parar timers e sons
    public void shutdown() {
        if (timer != null) timer.stop();
        if (messageTimer != null) messageTimer.stop();
        Sound.closeAll();
    }
}