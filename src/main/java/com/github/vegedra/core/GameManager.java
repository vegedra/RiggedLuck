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
import com.github.vegedra.collectors.CobradorManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameManager {

    // Variáveis e objetos
    public final Player player;
    private final UI ui;
    private final CardUI cardUI;
    private final TimerManager timerManager;

    private static final int MAX_CARDS = 9;
    private Card[] activeCards = new Card[MAX_CARDS];
    private CardGenerator generator = new CardGenerator();
    private VisibilityManager vm;
    private ActionListener cHandler;
    private CobradorManager cobradorManager;

    public boolean firstTimePlaying = false;
    public int currentGameMode;
    public int rollCost;
    public int rollsMade = 0;
    public boolean doubleAllActive = false;

    // Estados de jogo
    public enum GameState { TITLE, GAME, PAUSED, GAME_OVER }
    public GameState state = GameState.TITLE;

    // Construtor
    public GameManager(Player player, UI ui, ActionListener cHandler, VisibilityManager vm) {
        this.player   = player;
        this.ui       = ui;
        this.cHandler = cHandler;
        this.vm       = vm;
        this.rollCost = 20;

        this.cardUI = new CardUI(ui, this, cHandler);
        this.timerManager = new TimerManager(player, this);
        this.cobradorManager = new CobradorManager(ui, player, this, cHandler);
    }

    public void setVisibilityManager(VisibilityManager vm) { this.vm = vm; }
    public TimerManager getTimerManager() { return timerManager; }
    public CobradorManager getCobradorManager()   { return cobradorManager; }

    // Atualização de UI
    public void updateCounter()   { ui.counterLabel.setText(player.getCoins() + " moedas"); }
    public void updateCPSLabel()  { ui.cpsLabel.setText("Moedas/segundo: " + computeTotalCPS()); }
    public void updateLuckLabel() {
        ui.luckLabel.setText("Sorte: " + player.getLuck() + "%");

        // Dependendo do nivel de sorte atual, a cor muda
        int luck = player.getLuck();
        if (luck <= 25) {
            ui.luckLabel.setForeground(Color.red);
        } else if (luck >= 75) {
            ui.luckLabel.setForeground(Color.green);
        } else {
            ui.luckLabel.setForeground(Color.black);
        }
    }
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
    public void updateClickValueDisplay() {
        if (ui != null && ui.clickValueLabel != null) {
            int total = computeTotalClickValue();
            ui.clickValueLabel.setText("Moedas/clique: +" + total);
        }
    }

    // Getters
    public Card[] getActiveCards() { return activeCards; }
    public void showGamePanel() { vm.showGamePanel(); }
    public void showPauseScreen() { vm.showPauseScreen(); }

    // Clicker
    public void handleClick() {
        int baseGold = computeTotalClickValue();
        player.changeCoins(baseGold);
        Sound.CLICK.play();

        // Drenagem por clique de cobradores adaptativos (AdaptiveMode.CLICK_DRAIN)
        if (cobradorManager != null) {
            int clickDrain = cobradorManager.getClickDrainPerHit();
            if (clickDrain > 0) {
                player.changeCoins(-clickDrain);
            }
        }

        // Acumula cliques para o próximo tick de sorte
        timerManager.incrementClicksThisSecond();
        updateCounter();
        updateClickValueDisplay();
        checkGameOver();
    }

    // Sorteio da carta
    public void rollCard() {
        if (player.getCoins() < rollCost) {
            ui.showMessage("Moedas insuficientes!", Color.RED);
            return;
        }

        // Procura slot vazio
        int emptyIndex = findEmptyCardSlot();
        if (emptyIndex == -1) {
            ui.showMessage("Limite de cartas atingido!", Color.RED);
            return;
        }

        // Atualiza
        player.changeCoins(-rollCost);
        Sound.ROLL.play();
        rollsMade++;

        // Gera carta nova
        Card newCard = generator.generateCard(player.getLuck(), activeCards);
        placeCard(emptyIndex, newCard);

        // Atualiza
        refreshUI();
        ui.showMessage("Nova carta: " + newCard.name, Color.GREEN);
    }


    // Concede uma carta de recompensa ao derrotar um cobrador
    public boolean grantFightRewardCard() {
        // Simula sorte maior para tender a cartas mais raras
        int boostedLuck = Math.min(100, player.getLuck() + 40);
        Card rewardCard = generator.generateCard(boostedLuck, activeCards);

        int emptyIndex = findEmptyCardSlot();
        if (emptyIndex != -1) {
            placeCard(emptyIndex, rewardCard);
            refreshUI();
            ui.showMessage("Recompensa: " + rewardCard.name + "!", Color.YELLOW);
            return true;
        }

        // Slots cheios: oferece substituição
        int replaceIndex = promptCardReplacement(rewardCard);
        if (replaceIndex >= 0) {
            replaceCard(replaceIndex, rewardCard);
            refreshUI();
            ui.showMessage("Recompensa: " + rewardCard.name + "!", Color.YELLOW);
            return true;
        }

        return false; // Jogador recusou a substituição
    }

    // Coloca a carta num slot vazio e aplica seus efeitos
    private void placeCard(int index, Card card) {
        activeCards[index] = card;
        player.addClickValue(card.clickValue);
        cardUI.updateCardUI(index, card);
    }

    // Substitui a carta em um slot, removendo os efeitos da antiga
    private void replaceCard(int index, Card newCard) {
        Card oldCard = activeCards[index];
        if (oldCard != null) {
            player.addClickValue(-oldCard.clickValue);
        }
        activeCards[index] = newCard;
        player.addClickValue(newCard.clickValue);
        cardUI.updateCardUI(index, newCard);
    }

    // Retorna o primeiro slot vazio, ou -1 se todos cheios
    private int findEmptyCardSlot() {
        for (int i = 0; i < activeCards.length; i++) {
            if (activeCards[i] == null) return i;
        }
        return -1;
    }

    /**
     * Exibe um diálogo para o jogador escolher qual carta substituir.
     * Retorna o índice escolhido, ou -1 se cancelado.
     */
    private int promptCardReplacement(Card newCard) {
        String[] options = new String[activeCards.length + 1];
        for (int i = 0; i < activeCards.length; i++) {
            Card c = activeCards[i];
            options[i] = (i + 1) + ". " + c.name + " (" + c.rarity + ")";
        }
        options[activeCards.length] = "Cancelar";

        String message = "<html><b>Cartas cheias!</b><br><br>"
                + "Nova carta: <b>" + newCard.name + "</b> (" + newCard.rarity + ")<br>"
                + newCard.desc + "<br><br>"
                + "Escolha qual carta substituir:</html>";

        int choice = JOptionPane.showOptionDialog(
                ui.window, message, "Substituir Carta",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[activeCards.length]
        );

        if (choice < 0 || choice == activeCards.length) return -1;
        return choice;
    }


    // Cartas de uso ativo
    public void activateCard(int index) {
        Card c = activeCards[index];

        // Ignora se slot vazio, carta não é ativa, ou já foi usada
        if (c == null || !c.active || c.used) return;

        // Marca como usada antes de executar
        c.used = true;

        // Os tipos de efeito
        switch (c.activeEffect) {
            case DOUBLE_CLICK:
                // A Força: dobra o valor de clique do player por 5 segundos
                // Guarda o valor original, aplica o dobro, agenda restauração via Timer
                int originalClick = player.getClickValue();
                player.addClickValue(originalClick * 2);
                new Timer(5000, e -> {
                    player.addClickValue(originalClick); // restaura após 5s
                    ((Timer) e.getSource()).stop();       // para o timer de uso único
                }).start();
                ui.showMessage("A Força ativada! Cliques dobrados por 5s.", Color.ORANGE);
                break;

            case DOUBLE_ALL:
                // A Lua Cheia: dobra TODOS os efeitos por 60 segundos
                // Por ora seta uma flag no GameManager que os cálculos consultam
                doubleAllActive = true;
                new Timer(60000, e -> {
                    doubleAllActive = false;
                    ((Timer) e.getSource()).stop();
                }).start();
                ui.showMessage("A Lua Cheia ativada! Tudo dobrado por 1 minuto.", Color.MAGENTA);
                break;

            case DEATH_RESET:
                // A Morte: descarta todas as cartas, ganha 500 por carta, reseta sorte para 50
                int cardCount = 0;
                for (int i = 0; i < activeCards.length; i++) {
                    if (activeCards[i] != null && i != index) { // não conta ela mesma
                        player.addClickValue(-activeCards[i].clickValue); // remove bônus
                        activeCards[i] = null;
                        cardUI.updateCardUI(i, activeCards[i]);
                        cardCount++;
                    }
                }
                player.changeCoins(cardCount * 500); // 500 por carta destruída
                player.setLuck(50);                  // reseta sorte para 50%
                refreshUI();
                ui.showMessage("A Morte! +" + (cardCount * 500) + " moedas. Sorte resetada.", Color.RED);
                break;

            case SECOND_CHANCE:
                // Verificada no game over
                break;

            default:
                break;
        }
        cardUI.updateCardUI(index, activeCards[index]);  // slot fica cinza/consumido
    }

    public void activateJulgamento() {
        for (Card c : activeCards) {
            if (c != null && c.id.equals("o_julgamento")) {
                // Dobra a geração passiva por 3s: equivale a ganhar 3x o CPS atual
                int bonus = computeTotalCPS() * 3;
                player.changeCoins(bonus);
                updateCounter();
                ui.showMessage("O Julgamento! +" + bonus + " moedas!", Color.YELLOW);
                return;
            }
        }
    }

    // Calcula sorte e moedas/segundo
    public float computeTotalLuckPerSecond() {
        float total = 0;
        for (Card c : activeCards) {
            if (c != null) total += c.luckPerSecond;
        }
        // O Mundo: dobra tudo se 9 cartas
        if (hasCard("o_mundo") && countActiveCards() == 9) total *= 2;
        return total;
    }
    public int computeTotalCPS() {
        int base = 0;
        for (Card c : activeCards) {
            if (c != null) base += c.coinsPerSecond;
        }
        // A Sacerdotisa: +2/s por carta PASSIVE
        if (hasCard("a_sacerdotisa")) base += countCardsOfType(Card.CardType.PASSIVE) * 2;
        // O Mundo: dobra tudo se 9 cartas
        if (hasCard("o_mundo") && countActiveCards() == 9) base *= 2;
        return base;
    }
    public int computeTotalClickValue() {
        int base = player.getClickValue();
        // O Mago: +2/clique por carta CLICK
        if (hasCard("o_mago")) base += countCardsOfType(Card.CardType.CLICK) * 2;
        if (hasCard("o_mundo") && countActiveCards() == 9) base *= 2;
        if (cobradorManager != null) {
            base = Math.max(1, base - cobradorManager.getTotalClickPenalty());
        }
        return base;
    }
    public boolean hasCard(String id) {
        for (Card c : activeCards) {
            if (c != null && c.id.equals(id)) return true;
        }
        return false;
    }

    // Descarte de carta
    public void handleDiscard(int index) {
        Card c = activeCards[index];
        if (c == null) return;

        // Custo do descarte
        int sellValue = calcSellValue(c);
        player.addClickValue(-c.clickValue);
        player.changeCoins(sellValue);
        activeCards[index] = null;

        // Atualiza UI
        cardUI.updateCardUI(index, activeCards[index]);
        refreshUI();

        ui.showMessage("Carta vendida (+" + sellValue + " moedas)", Color.GREEN);
        Sound.DISCARD.play();
    }

    /**
     * Valor de venda: ~40% do poder da carta + bônus por raridade.
     * Mínimo de 5 moedas para sempre valer algo.
     */
    public int calcSellValue(Card c) {
        int power = (Math.abs(c.clickValue) * 3) + (Math.abs(c.coinsPerSecond) * 4);
        int rarityBonus;
        switch (c.rarity) {
            case MYTHIC:   rarityBonus = 120; break;
            case RARE:     rarityBonus = 50;  break;
            case UNCOMMON: rarityBonus = 20;  break;
            default:       rarityBonus = 5;   break;
        }
        return Math.max(5, (int)(power * 0.4f) + rarityBonus);
    }

    // Mantido por compatibilidade com CardUI (tooltip)
    public int calcDiscardCost(Card c) { return calcSellValue(c); }


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

    // Atualiza toda a UI
    private void refreshUI() {
        updateCounter();
        updateClickValueDisplay();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();
    }

    // Game Over
    public void checkGameOver() {
        if (player.getLuck() <= 0 || player.getCoins() < 0) {
            // Verifica se A Estrela está disponível antes de game over
            for (int i = 0; i < activeCards.length; i++) {
                Card c = activeCards[i];
                if (c != null && c.activeEffect == Card.ActiveEffect.SECOND_CHANCE && !c.used) {
                    c.used = true;
                    player.setLuck(15); // segunda chance: sorte vai pra 15%
                    updateLuckLabel();
                    cardUI.updateCardUI(i, activeCards[i]);
                    ui.showMessage("A Estrela te salvou! Sorte: 15%", Color.CYAN);
                    return; // não declara game over
                }
            }
            // Game over de verdade
            state = GameState.GAME_OVER;
            vm.showGameOverScreen();
        }
    }

    // Modo de jogo
    public void configureGameMode(int mode) {
        currentGameMode = mode;
    }

    // Reset
    public void resetGame() {
        timerManager.stopGameTimers();
        player.reset();

        // Remove cartas ativas
        for (int i = 0; i < activeCards.length; i++) {
            activeCards[i] = null;
            cardUI.updateCardUI(i, activeCards[i]);
        }

        // Reseta variaveis
        timerManager.resetCounters();
        rollsMade = 0;
        rollCost = 20;
        firstTimePlaying = false;

        if (cobradorManager != null) cobradorManager.reset();

        // Atualiza UI
        refreshUI();
    }

    // Fechar tudo
    public void shutdown() {
        timerManager.stopGameTimers();
        Sound.closeAll();
    }
}
