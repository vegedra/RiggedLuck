/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

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

    // =========================================================================
    // Variáveis e objetos
    // =========================================================================

    public Player player;
    private UI ui;
    private static final int MAX_CARDS = 4;         // TODO: MUDAR PARA 9 JUNTO COM A UI
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

    // Efeitos acumulados das cartas ativas — recalculados em recalcCardEffects()
    private float totalOscMultiplier   = 1f;    // Multiplicador da oscilação da sorte
    private float totalTendencyReduc   = 0f;    // Redução da tendência negativa (%/s)
    private float totalLuckPerSecond   = 0f;    // Sorte passiva por segundo
    private int   luckFloor            = 0;     // Sorte mínima garantida (0 = sem piso)
    private int   luckCeil             = 100;   // Sorte máxima garantida (100 = sem teto)
    private boolean doubleTendency     = false; // O Diabo sorte/risco: dobra tendência
    private boolean doubleAllLuck      = false; // A Lua Negra normal: dobra efeitos de sorte
    private boolean invertAllLuck      = false; // A Lua Negra invertida: inverte efeitos de sorte
    private boolean amplifyAll         = false; // O Mundo sinérgico (5+ cartas): ×1.5
    private boolean reduceAll          = false; // O Mundo invertido (5+ cartas): ×0.5

    // Pulso do Julgamento
    private int julgamentoTimer        = 0;     // Conta os segundos até o próximo pulso
    private static final int PULSO_INTERVALO = 60;

    // Estados de jogo
    public enum GameState { TITLE, GAME, GAME_OVER }
    public GameState state = GameState.TITLE;

    // =========================================================================
    // Construtor
    // =========================================================================

    public GameManager(Player player, UI ui, ActionListener cHandler, VisibilityManager vm) {
        this.player   = player;
        this.ui       = ui;
        this.cHandler = cHandler;
        this.vm       = vm;
        this.rollCost = 20;
    }

    public void setVisibilityManager(VisibilityManager vm) { this.vm = vm; }

    // =========================================================================
    // Atualização de labels
    // =========================================================================

    public void updateCounter()   { ui.counterLabel.setText(player.getCoins() + " moedas"); }
    public void updateCPSLabel()  { ui.cpsLabel.setText("Moedas por segundo: " + getTotalCPS()); }
    public void updateLuckLabel() { ui.luckLabel.setText("Sorte: " + player.getLuck() + "%"); }

    public void updateRollCost() {
        if (ui != null && ui.rollButton != null) {
            rollCost = (int)(20 * Math.pow(1.6, rollsMade));
            ui.rollButton.setText("Sortear (" + rollCost + " moedas)");
        }
    }

    // =========================================================================
    // Timer principal
    // =========================================================================

    public void startTimer() {
        timer = new Timer(1000, e -> {
            if (state == GameState.GAME_OVER) { timer.stop(); return; }

            secondsElapsed++;

            int clicks = clicksThisSecond;
            clicksThisSecond = 0;

            // Geração passiva de ouro
            player.addCoins(getTotalCPS());

            // Sorte passiva por segundo (cartas LUCK/PASSIVE com luckPerSecond)
            applyPassiveLuckPerSecond();

            // Pulso do Julgamento (a cada 60s)
            tickJulgamento();

            // Oscilação + tendência natural da sorte
            updateLuck(clicks);

            updateRollCost();
            updateCounter();
            updateCPSLabel();

            // TODO: checkCollectorSpawn();
        });
        timer.start();
    }

    // =========================================================================
    // Cálculo de CPS total
    // =========================================================================

    public int getTotalCPS() {
        int total = 0;

        // CPS base das cartas
        for (Card c : activeCards) {
            if (c == null) continue;
            int cps = c.coinsPerSecond;

            // O Mundo passiva: escala por cartas de Clique equipadas
            if (hasSpecialEffect(c, "PASSIVE_PER_CLICK_CARD")) {
                cps += 3 * countCardsOfType(Card.CardType.CLICK);
            } else if (hasSpecialEffect(c, "INV_PASSIVE_PER_CLICK_CARD")) {
                cps -= 2 * countCardsOfType(Card.CardType.CLICK);
            }

            // Alta Sacerdotisa: escala por cartas Passivas
            if (hasSpecialEffect(c, "SYNERGY_PER_PASSIVE_CARD_BONUS")) {
                cps += 4 * countCardsOfType(Card.CardType.PASSIVE);
            } else if (hasSpecialEffect(c, "SYNERGY_PER_PASSIVE_CARD_PENALTY")) {
                cps -= 2 * countCardsOfType(Card.CardType.PASSIVE);
            }

            total += cps;
        }

        // O Mundo invertido (5+ cartas): ×0.5
        if (reduceAll) total = (int)(total * 0.5f);
        // O Mundo (5+ cartas): ×1.5
        if (amplifyAll) total = (int)(total * 1.5f);

        return total;
    }

    // =========================================================================
    // Sorte passiva por segundo (cartas que têm luckPerSecond)
    // =========================================================================

    private void applyPassiveLuckPerSecond() {
        float delta = totalLuckPerSecond;

        // A Lua Cheia sinérgica: +0.5%/s por carta de Sorte
        for (Card c : activeCards) {
            if (c == null) continue;
            if (hasSpecialEffect(c, "SYNERGY_PER_LUCK_CARD_BONUS")) {
                delta += 0.5f * countCardsOfType(Card.CardType.LUCK);
            } else if (hasSpecialEffect(c, "SYNERGY_PER_LUCK_CARD_PENALTY")) {
                delta -= 0.3f * countCardsOfType(Card.CardType.LUCK);
            }
        }

        delta = applyLuckModifiers(delta);

        if (delta != 0) {
            player.changeLuck(Math.round(delta));
            clampLuck();
        }
    }

    // =========================================================================
    // Pulso do Julgamento (a cada 60s)
    // =========================================================================

    private void tickJulgamento() {
        boolean hasNormal   = hasCardWithEffect("PULSE_DOUBLE_60S");
        boolean hasInverted = hasCardWithEffect("PULSE_STOP_60S");
        if (!hasNormal && !hasInverted) { julgamentoTimer = 0; return; }

        julgamentoTimer++;
        if (julgamentoTimer < PULSO_INTERVALO) return;
        julgamentoTimer = 0;

        if (hasNormal) {
            // Dobra CPS e sorte por 3s via timer extra
            int bonusCPS  = getTotalCPS();
            float bonusLuck = totalLuckPerSecond;
            player.addCoins(bonusCPS * 3);
            player.changeLuck(Math.round(bonusLuck * 3));
            ui.showMessage("✨ O Julgamento pulsa! CPS e sorte dobrados por 3s", new Color(255, 215, 0));
        } else {
            // Inverte: sorte cai 5%
            player.changeLuck(-5);
            ui.showMessage("💀 O Julgamento invertido! Sorte -5%", Color.RED);
        }
        clampLuck();
        updateLuckLabel();
        updateCounter();
    }

    // =========================================================================
    // Oscilação e tendência natural da sorte (tick por segundo)
    // =========================================================================

    private void updateLuck(int clicks) {
        float minutesPassed = secondsElapsed / 60f;

        // Tendência negativa crescente
        float baseTendency = 0.5f + (minutesPassed * 0.15f);
        baseTendency = Math.min(baseTendency, 3.5f);

        // Dobra a tendência se O Diabo (sorte ou risco) estiver ativo
        if (doubleTendency) baseTendency *= 2f;

        // Redução de tendência pelas cartas (Sacerdotisa, Roda da Fortuna passiva etc.)
        baseTendency = Math.max(0f, baseTendency - totalTendencyReduc);

        // Oscilação aleatória crescente
        float baseOsc = 2f + (minutesPassed * 0.4f);
        baseOsc = Math.min(baseOsc, 12f);
        baseOsc *= totalOscMultiplier;   // Temperança reduz, O Carro/Diabo aumenta

        float variation = (float)(Math.random() * (baseOsc * 2)) - baseOsc;
        variation -= baseTendency;

        // Bônus por clique (base)
        float bonusClick = 0.2f;

        // Cartas que dão sorte por clique (O Mago, A Imperatriz clique etc.)
        for (Card c : activeCards) {
            if (c == null) continue;
            bonusClick += c.luckPerClick;

            // Mago Supremo sinérgico: +0.3% sorte/clique por carta de Clique
            if (hasSpecialEffect(c, "SYNERGY_PER_CLICK_CARD_BONUS")) {
                bonusClick += 0.3f * countCardsOfType(Card.CardType.CLICK);
            } else if (hasSpecialEffect(c, "SYNERGY_PER_CLICK_CARD_PENALTY")) {
                bonusClick -= 0.2f * countCardsOfType(Card.CardType.CLICK);
            }
        }

        float result = variation + (bonusClick * clicks);
        result = applyLuckModifiers(result);

        player.changeLuck(Math.round(result));
        clampLuck();
        updateLuckLabel();
        checkGameOver();
    }

    // =========================================================================
    // Modificadores globais de sorte (Lua Negra, O Mundo etc.)
    // =========================================================================

    /**
     * Aplica doubleAllLuck / invertAllLuck / amplifyAll / reduceAll sobre um delta de sorte.
     */
    private float applyLuckModifiers(float delta) {
        if (doubleAllLuck) delta *= 2f;
        if (invertAllLuck) delta *= -1f;
        if (amplifyAll)    delta *= 1.5f;
        if (reduceAll)     delta *= 0.5f;
        return delta;
    }

    // =========================================================================
    // Recalcula todos os efeitos passivos das cartas ativas
    // Chamar sempre que uma carta é adicionada ou removida.
    // =========================================================================

    private void recalcCardEffects() {
        // Zera tudo
        totalOscMultiplier = 1f;
        totalTendencyReduc = 0f;
        totalLuckPerSecond = 0f;
        luckFloor          = 0;
        luckCeil           = 100;
        doubleTendency     = false;
        doubleAllLuck      = false;
        invertAllLuck      = false;

        int cardCount = countActiveCards();
        amplifyAll = (cardCount >= 5 && hasCardWithEffect("SYNERGY_5CARDS_AMPLIFY"));
        reduceAll  = (cardCount >= 5 && hasCardWithEffect("SYNERGY_5CARDS_REDUCE"));

        for (Card c : activeCards) {
            if (c == null) continue;

            // Oscilação
            // Multiplicadores empilham de forma aditiva sobre 1.0
            // Ex: Temperança 0.6 + Diabo 2.0 → média ponderada, não stacking exponencial
            if (c.oscMultiplier != 1f) {
                totalOscMultiplier += (c.oscMultiplier - 1f);
            }

            // Tendência
            totalTendencyReduc += c.tendencyReduction;

            // Sorte por segundo
            totalLuckPerSecond += c.luckPerSecond;

            // Efeitos especiais passivos
            String fx = c.specialEffect;
            if (fx == null) continue;

            switch (fx) {
                case "LUCK_FLOOR_25": luckFloor = Math.max(luckFloor, 25); break;
                case "LUCK_CEIL_60": luckCeil  = Math.min(luckCeil,  60); break;
                case "DOUBLE_TENDENCY": doubleTendency = true; break;
                case "DOUBLE_ALL_LUCK_EFFECTS": doubleAllLuck  = true; break;
                case "INVERT_ALL_LUCK_EFFECTS": invertAllLuck  = true; break;
                // Os demais (COLLECTOR_*, ACTIVE_*, CLICK_CHANCE_*) são tratados
                // em seus respectivos métodos (handleClick, handleCollector etc.)
            }
        }

        // Garante que oscMultiplier não vire negativo
        totalOscMultiplier = Math.max(0.1f, totalOscMultiplier);
    }

    // =========================================================================
    // Aplica/desfaz efeitos de uma carta no Player
    // =========================================================================

    /** Aplica efeitos imediatos e de clique/CPS ao equipar uma carta. */
    private void applyCardToPlayer(Card c) {
        // Click value e CPS — Player já soma/subtrai
        player.addClickValue(c.clickValue);
        // (CPS é calculado dinamicamente em getTotalCPS(), não precisa guardar no Player)

        // Mago Supremo sinérgico — click extra por carta de Clique
        if (hasSpecialEffect(c, "SYNERGY_PER_CLICK_CARD_BONUS")) {
            player.addClickValue(3 * countCardsOfType(Card.CardType.CLICK));
        }

        // Sorte imediata ao equipar
        float luck = c.luckBonus;

        // A Lua Cheia: +5% sorte por carta de Sorte já equipada
        if (hasSpecialEffect(c, "SYNERGY_PER_LUCK_CARD_BONUS")) {
            luck += 5f * countCardsOfType(Card.CardType.LUCK);
        } else if (hasSpecialEffect(c, "SYNERGY_PER_LUCK_CARD_PENALTY")) {
            luck -= 3f * countCardsOfType(Card.CardType.LUCK);
        }

        luck = applyLuckModifiers(luck);
        if (luck != 0) player.changeLuck(Math.round(luck));

        clampLuck();
    }

    /** Desfaz efeitos ao descartar uma carta. */
    private void removeCardFromPlayer(Card c) {
        player.addClickValue(-c.clickValue);

        // Desfaz bônus do Mago Supremo
        if (hasSpecialEffect(c, "SYNERGY_PER_CLICK_CARD_BONUS")) {
            player.addClickValue(-(3 * countCardsOfType(Card.CardType.CLICK)));
        }
        // Nota: luckBonus não é revertido — é um bônus pontual ao equipar.
    }

    // =========================================================================
    // Roleta / adição de carta
    // =========================================================================

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

        player.addCoins(-rollCost);
        Sound.ROLL.play();
        rollsMade++;

        Card newCard = generator.generateCard(player.getLuck());
        activeCards[emptyIndex] = newCard;

        // Recalcula efeitos passivos ANTES de aplicar o bônus imediato
        recalcCardEffects();
        applyCardToPlayer(newCard);

        updateCardUI(emptyIndex);
        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();

        String tag = newCard.inverted ? " [INVERTIDA]" : "";
        ui.showMessage("Nova carta: " + newCard.name + tag, newCard.inverted ? Color.ORANGE : Color.GREEN);
    }

    // =========================================================================
    // Descarte
    // =========================================================================

    public void handleDiscard(int index) {
        Card c = activeCards[index];
        if (c == null) return;

        int discardCost = calcDiscardCost(c);

        if (player.getCoins() < discardCost) {
            ui.showMessage("Moedas insuficientes para descartar!", Color.RED);
            return;
        }

        // Caso especial: A Morte — descarta TODAS as cartas
        if (hasSpecialEffect(c, "ACTIVE_NUKE_GAIN") || hasSpecialEffect(c, "ACTIVE_NUKE_LOSS")) {
            handleNuke(c);
            return;
        }

        player.addCoins(-discardCost);
        removeCardFromPlayer(c);
        activeCards[index] = null;

        recalcCardEffects();

        updateCardUI(index);
        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();

        ui.showMessage("Carta descartada (-" + discardCost + " moedas)", Color.GREEN);
        Sound.DISCARD.play();
    }

    /** A Morte: descarta todas as cartas e aplica ouro/sorte por carta. */
    private void handleNuke(Card morte) {
        boolean gain = hasSpecialEffect(morte, "ACTIVE_NUKE_GAIN");
        int cardCount = countActiveCards();

        for (int i = 0; i < activeCards.length; i++) {
            if (activeCards[i] != null) {
                removeCardFromPlayer(activeCards[i]);
                activeCards[i] = null;
                updateCardUI(i);
            }
        }

        if (gain) {
            player.addCoins(100 * cardCount);
            player.changeLuck(2 * cardCount);
            ui.showMessage("💀 A Morte! +" + (100 * cardCount) + " ouro, +" + (2 * cardCount) + "% sorte", Color.GREEN);
        } else {
            player.addCoins(-(50 * cardCount));
            player.changeLuck(-(cardCount));
            ui.showMessage("💀 A Morte invertida! -" + (50 * cardCount) + " ouro, -" + cardCount + "% sorte", Color.RED);
        }

        recalcCardEffects();
        clampLuck();
        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();
        Sound.DISCARD.play();
        checkGameOver();
    }

    /** Custo de descarte: baseado em clickValue, CPS e efeito de sorte. */
    private int calcDiscardCost(Card c) {
        // Cartas mais poderosas custam mais pra descartar
        int base = (Math.abs(c.clickValue) + Math.abs(c.coinsPerSecond)) * 2;
        // Cartas de Sorte/Risco/Sinérgicas têm custo mínimo de 10
        if (c.type == Card.CardType.LUCK || c.type == Card.CardType.RISK || c.type == Card.CardType.SYNERGY) {
            base = Math.max(base, 10);
        }
        return base;
    }

    // =========================================================================
    // Clique
    // =========================================================================

    public void handleClick() {
        int baseGold = player.getClickValue();

        // A Torre risco: +30 ouro por clique com 15% de chance de spawn de cobrador
        for (Card c : activeCards) {
            if (c == null) continue;
            if (hasSpecialEffect(c, "CLICK_SPAWN_COLLECTOR_15")) {
                baseGold += 30;
                if (Math.random() < 0.15) {
                    // TODO: spawnCollector() — drenar sorte
                    ui.showMessage("👿 Um cobrador apareceu!", Color.RED);
                }
            } else if (hasSpecialEffect(c, "CLICK_REMOVE_COLLECTOR_15")) {
                baseGold -= 15;
                // TODO: 15% chance de remover cobrador ativo
            }

            // Roda da Fortuna clique: 20% de chance de bônus/penalidade
            if (hasSpecialEffect(c, "CLICK_CHANCE_BONUS") && Math.random() < 0.20) {
                baseGold += 10;
                player.changeLuck(1);
                ui.showMessage("🎲 Roda da Fortuna! +10 ouro +1% sorte", new Color(255, 215, 0));
            } else if (hasSpecialEffect(c, "CLICK_CHANCE_PENALTY") && Math.random() < 0.20) {
                baseGold -= 5;
                player.changeLuck(-1);
            }
        }

        player.addCoins(baseGold);
        Sound.CLICK.play();
        clicksThisSecond++;

        updateCounter();
        updateLuckLabel();
        clampLuck();
        checkGameOver();
    }

    private String getRarityColor(Card.Rarity rarity) {
        if (rarity == Card.Rarity.MYTHIC)   return "#FF44FF";
        if (rarity == Card.Rarity.RARE)     return "#44AAFF";
        if (rarity == Card.Rarity.UNCOMMON) return "#44FF88";
        return "#FFFFFF";
    }

    // =========================================================================
    // Eventos de ouro ganho/perdido — aciona A Torre sorte
    // =========================================================================

    /**
     * Chamar sempre que o jogador PERDE ouro (cobradores, descartes, eventos).
     * A Torre (normal): +2% sorte ao perder ouro.
     * A Torre (invertida): -2% sorte ao ganhar ouro — tratada em onGoldGained().
     */
    public void onGoldLost(int amount) {
        for (Card c : activeCards) {
            if (hasSpecialEffect(c, "LUCK_ON_GOLD_LOSS")) {
                player.changeLuck(2);
                updateLuckLabel();
                clampLuck();
                break;
            }
        }
    }

    /** Chamar sempre que o jogador GANHA ouro em eventos/cobradores (não no clique normal). */
    public void onGoldGained(int amount) {
        for (Card c : activeCards) {
            if (hasSpecialEffect(c, "LUCK_LOSS_ON_GOLD_GAIN")) {
                player.changeLuck(-2);
                updateLuckLabel();
                clampLuck();
                break;
            }
        }
    }

    // =========================================================================
    // Limite de sorte (piso/teto das cartas)
    // =========================================================================

    private void clampLuck() {
        int luck = player.getLuck();
        if (luck < luckFloor) player.setLuck(luckFloor);
        if (luck > luckCeil)  player.setLuck(luckCeil);
    }

    // =========================================================================
    // UI das cartas
    // =========================================================================

    public void updateCardUI(int index) {
        ui.cardSlots[index].removeAll();
        Card c = activeCards[index];
        ui.cardSlots[index].setLayout(new BorderLayout());

        if (c == null) {
            JLabel empty = new JLabel("Vazio", SwingConstants.CENTER);
            ui.cardSlots[index].add(empty, BorderLayout.CENTER);
            ui.cardSlots[index].setToolTipText(null);

        } else {
            int discardCost = calcDiscardCost(c);
            String rarityColor = getRarityColor(c.rarity);
            String invertedTag = c.inverted ? " ⟲" : "";

            JLabel name = new JLabel(
                    "<html><font color='" + rarityColor + "'>" + c.name + invertedTag + "</font></html>",
                    SwingConstants.CENTER
            );

            JButton discard = new JButton("X");
            discard.setFocusPainted(false);
            discard.setMargin(new Insets(2, 6, 2, 6));
            discard.setActionCommand("discard_" + index);
            discard.setToolTipText("Descartar carta pagando uma taxa.");
            discard.addActionListener(cHandler);

            ui.cardSlots[index].add(name, BorderLayout.CENTER);
            ui.cardSlots[index].add(discard, BorderLayout.EAST);

            // Tooltip completo
            String tooltipText = "<html>"
                    + "<b><font color='" + rarityColor + "'>" + c.name + "</font></b>"
                    + (c.inverted ? " <i>(Invertida)</i>" : "") + "<br><br>"
                    + "<i>" + c.type + " · " + c.rarity + "</i><br><br>"
                    + c.desc + "<br><br>"
                    + (c.clickValue    != 0 ? "Clique: " + (c.clickValue > 0 ? "+" : "") + c.clickValue + "<br>" : "")
                    + (c.coinsPerSecond!= 0 ? "CPS: "   + (c.coinsPerSecond > 0 ? "+" : "") + c.coinsPerSecond + "<br>" : "")
                    + (c.luckBonus     != 0 ? "Sorte ao equipar: " + (c.luckBonus > 0 ? "+" : "") + c.luckBonus + "%<br>" : "")
                    + (c.luckPerSecond != 0 ? "Sorte/s: " + (c.luckPerSecond > 0 ? "+" : "") + c.luckPerSecond + "%<br>" : "")
                    + "<br>Descartar custa: " + discardCost + " moedas"
                    + "</html>";

            ui.cardSlots[index].setToolTipText(tooltipText);
        }

        ui.cardSlots[index].revalidate();
        ui.cardSlots[index].repaint();
    }

    // =========================================================================
    // Utilitários de carta
    // =========================================================================

    /** Conta cartas ativas de um tipo específico. */
    public int countCardsOfType(Card.CardType type) {
        int count = 0;
        for (Card c : activeCards) {
            if (c != null && c.type == type) count++;
        }
        return count;
    }

    /** Conta total de cartas ativas. */
    public int countActiveCards() {
        int count = 0;
        for (Card c : activeCards) { if (c != null) count++; }
        return count;
    }

    /** Verifica se qualquer carta ativa tem um specialEffect específico. */
    public boolean hasCardWithEffect(String effect) {
        for (Card c : activeCards) {
            if (hasSpecialEffect(c, effect)) return true;
        }
        return false;
    }

    /** Null-safe: verifica se uma carta específica tem um specialEffect. */
    private boolean hasSpecialEffect(Card c, String effect) {
        return c != null && effect.equals(c.specialEffect);
    }

    // =========================================================================
    // Controle de tempo
    // =========================================================================

    public String formatTime(int totalSeconds) {
        int hours   = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void stopGameTimers() {
        if (timer != null)        { timer.stop();        timer = null; }
        if (messageTimer != null) { messageTimer.stop(); messageTimer = null; }
    }

    // =========================================================================
    // Game Over
    // =========================================================================

    private void checkGameOver() {
        if (player.getLuck() <= 0 || player.getCoins() < 0) {
            state = GameState.GAME_OVER;
            vm.showGameOverScreen();
            System.out.println("Game Over!");
        }
    }

    // =========================================================================
    // Modo de jogo
    // =========================================================================

    public void configureGameMode(int mode) {
        currentGameMode = mode;
    }

    // =========================================================================
    // Reset
    // =========================================================================

    public void resetGame() {
        stopGameTimers();
        player.reset();

        for (int i = 0; i < activeCards.length; i++) {
            activeCards[i] = null;
            updateCardUI(i);
        }

        secondsElapsed     = 0;
        rollsMade          = 0;
        rollCost           = 20;
        firstTimePlaying   = false;
        julgamentoTimer    = 0;

        // Reseta todos os modificadores
        totalOscMultiplier = 1f;
        totalTendencyReduc = 0f;
        totalLuckPerSecond = 0f;
        luckFloor          = 0;
        luckCeil           = 100;
        doubleTendency     = false;
        doubleAllLuck      = false;
        invertAllLuck      = false;
        amplifyAll         = false;
        reduceAll          = false;

        updateCounter();
        updateCPSLabel();
        updateLuckLabel();
        updateRollCost();
    }

    // =========================================================================
    // Shutdown
    // =========================================================================

    public void shutdown() {
        if (timer != null)        timer.stop();
        if (messageTimer != null) messageTimer.stop();
        Sound.closeAll();
    }
}