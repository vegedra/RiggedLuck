/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Gerencia spawn, ciclo de vida, UI e interações dos cobradores.
*/

package com.github.vegedra.collectors;

import com.github.vegedra.collectors.types.CobradorTenaz;
import com.github.vegedra.core.GameManager;
import com.github.vegedra.core.Player;
import com.github.vegedra.core.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CobradorManager {

    // Constantes
    private static final int MAX_COBRADORES = 5;
    private static final int SPAWN_INTERVAL_SEC = 15;       // Verifica spawn a cada 15s
    private static final int SPAWN_CHANCE_BASE = 30;        // 25% base de chance de spawn
    private static final int COINS_TRIGGER = 100;           // Mínimo de moedas para ativar sistema (100)

    // Dimensões do card dos cobradores
    private static final int CARD_W = 200;
    private static final int CARD_H = 280;
    private static final int STACK_OFFSET = 6;              // deslocamento px entre cada carta no deck
    // Layout interno
    private static final int NAME_Y    = 5;
    private static final int NAME_H    = 22;
    private static final int SEP_Y     = NAME_Y + NAME_H + 2;       // 29
    private static final int SPRITE_Y  = SEP_Y + 4;                 // 33
    private static final int SPRITE_H  = 162;                       // ~80% do espaço útil
    private static final int STATS_Y   = SPRITE_Y + SPRITE_H + 4;   // 199
    private static final int STATS_H   = 16;
    private static final int DEBUFF_Y  = STATS_Y + STATS_H + 2;     // 217
    private static final int DEBUFF_H  = 14;
    private static final int BTN_PAY_Y = 230;
    private static final int BTN_ATK_Y = 254;
    private static final int BTN_H     = 22;
    private static final int BTN_X     = 8;
    private static final int BTN_W     = CARD_W - BTN_X * 2;        // 184

    // Referências
    private final UI ui;
    private final Player player;
    private final GameManager gm;
    private final ActionListener cHandler;

    // Estados
    private final Cobrador[] cobradores = new Cobrador[MAX_COBRADORES];

    private boolean spawnEnabled = false;
    private int ticksUntilSpawnCheck = SPAWN_INTERVAL_SEC;

    // Painel principal
    private JPanel deckContainer;

    // Construtor
    public CobradorManager(UI ui, Player player, GameManager gm, ActionListener cHandler) {
        this.ui       = ui;
        this.player   = player;
        this.gm       = gm;
        this.cHandler = cHandler;
    }

    // Inicializa o painel dos cobradores
    public void initUI() {
        // Área total que acomoda o deck + sombra de empilhamento
        int containerW = CARD_W + STACK_OFFSET * (MAX_COBRADORES - 1);
        int containerH = CARD_H + STACK_OFFSET * (MAX_COBRADORES - 1);

        deckContainer = new JPanel(null);
        deckContainer.setBounds(500, 165, containerW, containerH);
        deckContainer.setOpaque(false);
        deckContainer.setVisible(false);

        ui.gamePanel.add(deckContainer);
        ui.gamePanel.revalidate();
    }

    // Processa a drenagem, debuffs e spawn
    public void tick(int secondsElapsed) {
        // Debug
        System.out.println("[CM] tick s=" + secondsElapsed
                + " coins=" + player.getCoins()
                + " enabled=" + spawnEnabled
                + " countdown=" + ticksUntilSpawnCheck);

        // Ativa o sistema de spawn quando o jogador acumula moedas suficientes
        if (!spawnEnabled && player.getCoins() >= COINS_TRIGGER) {
            spawnEnabled = true;
        }
        if (!spawnEnabled) return;

        // Drenagem de moedas e debuffs passivos de cada cobrador ativo
        for (Cobrador c : cobradores) {
            if (c != null && c.isActive()) {
                player.changeCoins(-c.getDrainPerSecond());
                c.tick();
                // O Eremita: bloqueia debuffs que afetam sorte diretamente
                if (c.getDebuff() == Cobrador.Debuff.LUCK_DRAIN && gm.hasCard("o_ermitao")) {
                // sorte protegida
                } else {
                    c.aplicarDebuff(player);
                }
            }
        }

        // Contagem regressiva para verificação de spawn
        ticksUntilSpawnCheck--;
        if (ticksUntilSpawnCheck <= 0) {
            // O Enforcado: cobradores demoram 20% mais para aparecer
            int interval = gm.hasCard("o_enforcado")
                    ? (int)(SPAWN_INTERVAL_SEC * 1.2)
                    : SPAWN_INTERVAL_SEC;
            ticksUntilSpawnCheck = interval;
            trySpawn(secondsElapsed);
        }

        // Atualiza UI
        gm.updateCounter();
        gm.updateLuckLabel();
    }


    // Verifica spawn
    private void trySpawn(int secondsElapsed) {
        if (countActive() >= MAX_COBRADORES) return;

        // Chance de spawn cresce com o tempo de jogo (máx 80%)
        int minutes = secondsElapsed / 60;
        int effectiveBase = player.getLuck() > 90 ? SPAWN_CHANCE_BASE * 2 : SPAWN_CHANCE_BASE;
        int chance = Math.min(80, effectiveBase + minutes * 3);

        if ((int)(Math.random() * 100) < chance) {
            spawn(secondsElapsed);
        }
    }
    private void spawn(int secondsElapsed) {
        // Procura um slot vazio
        int emptyIndex = findEmptySlot();
        if (emptyIndex == -1) return;

        // Cria o cobrador
        Cobrador novo = CobradorFactory.createRandom(secondsElapsed);
        cobradores[emptyIndex] = novo;
        System.out.println("[CM] Spawnou: " + novo.getName() + " no slot " + emptyIndex);

        updateStackUI();
        System.out.println("[CM] updateSlotUI ok");

        ui.showMessage("Um <b>" + novo.getName() + "</b> aparece!", Color.RED);
        // Sound.COBRADOR_SPAWN.play(); // TODO: adicionar SFX
    }


    // Pagar o cobrador
    public void handlePay(int index) {
        Cobrador c = cobradores[index];
        if (c == null || !c.isActive()) return;

        int baseCost = c.getPaymentCost();
        // A Imperatriz: pagar cobradores custa 30% menos
        int cost = gm.hasCard("a_imperatriz") ? (int)(baseCost * 0.7f) : baseCost;
        if (player.getCoins() < cost) {
            ui.showMessage("Moedas insuficientes para pagar!", Color.RED);
            return;
        }

        //boolean paid = c.receberPagamento(cost);
        if (c.receberPagamento(cost)) {
            player.changeCoins(-cost);
            String nome = c.getName();
            removeCobrador(index);
            gm.updateCounter();
            ui.showMessage(nome + " foi pago e foi embora.", Color.GREEN);
        }
    }

    // Ataque
    public void handleAttack(int index) {
        Cobrador c = cobradores[index];
        if (c == null || !c.isActive()) return;

        int luckPenalty = c.receberAtaque();
        // O Eremita: cobradores não afetam sorte diretamente
        if (!gm.hasCard("o_ermitao")) {
            player.changeLuck(-luckPenalty);
            gm.updateLuckLabel();
        }

        // Cobrador foi derrotado
        if (!c.isActive()) {
            String nome = c.getName();
            removeCobrador(index);
            ui.showMessage(nome + " foi expulso! (-" + luckPenalty + "% sorte)", Color.ORANGE);
            // Cobrador Tenaz sobreviveu ao primeiro ataque
        } else {
            updateStackUI();
            ui.showMessage(c.getName() + " resistiu! (-" + luckPenalty + "% sorte)", Color.YELLOW);
        }

        // Verifica game over
        gm.checkGameOver();
    }


    // UI dos cards dos cobradores
    /**
     * Reconstrói o deck inteiro.
     * A carta da frente (slot ativo de menor índice) fica por cima com UI completa.
     * As cartas atrás são deslocadas e mostram apenas o verso do card.
     */
    private void updateStackUI() {
        deckContainer.removeAll();

        // Coleta apenas slots ocupados
        int[] activeIndexes = new int[MAX_COBRADORES];
        int activeCount = 0;
        for (int i = 0; i < MAX_COBRADORES; i++) {
            if (cobradores[i] != null && cobradores[i].isActive()) {
                activeIndexes[activeCount++] = i;
            }
        }

        if (activeCount == 0) {
            deckContainer.setVisible(false);
            deckContainer.revalidate();
            deckContainer.repaint();
            ui.gamePanel.repaint();
            return;
        }

        // Z-order em Swing (null layout)
        for (int layer = 0; layer < activeCount; layer++) {
            int cobradorIndex = activeIndexes[layer];
            Cobrador c = cobradores[cobradorIndex];

            int offsetX = layer * STACK_OFFSET;
            int offsetY = layer * STACK_OFFSET;

            JPanel card = buildCardPanel(c, cobradorIndex, layer == 0);
            card.setBounds(offsetX, offsetY, CARD_W, CARD_H);
            deckContainer.add(card);
        }

        deckContainer.setVisible(true);
        deckContainer.revalidate();
        deckContainer.repaint();
        ui.gamePanel.repaint();
    }

    // Constrói o painel visual de uma carta de cobrador
    private JPanel buildCardPanel(Cobrador c, int cobradorIndex, boolean isFront) {
        JPanel card = new JPanel(null);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // Verso da carta (cartas empilhadas atrás)
        if (!isFront) {
            JLabel back = new JLabel("☽", SwingConstants.CENTER);
            back.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            back.setForeground(new Color(180, 180, 180));
            back.setBounds(0, 0, CARD_W, CARD_H);
            card.add(back);
            return card;
        }

        // Frente da carta (carta interativa da frente)
        // Nome no topo
        JLabel nameLabel = new JLabel(c.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Cambria", Font.BOLD, 12));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(4, NAME_Y, CARD_W - 8, NAME_H);
        card.add(nameLabel);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(180, 180, 180));
        sep.setBounds(8, SEP_Y, CARD_W - 16, 2);
        card.add(sep);

        // Sprite / ícone central
        JLabel spriteLabel = new JLabel("", SwingConstants.CENTER);
        spriteLabel.setBounds(0, SPRITE_Y, CARD_W, SPRITE_H);
        try {
            java.net.URL imgUrl = getClass().getResource(c.getIconPath());
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image scaled = icon.getImage().getScaledInstance(CARD_W - 20, SPRITE_H - 8, Image.SCALE_DEFAULT);
                spriteLabel.setIcon(new ImageIcon(scaled));
            } else {
                spriteLabel.setText("👹");
                spriteLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
            }
        } catch (Exception e) {
            spriteLabel.setText("👹");
            spriteLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        }
        card.add(spriteLabel);

        // Drenagem e HP (se do tipo tenaz)
        String hpStr = (c instanceof CobradorTenaz)
                ? "  [" + ((CobradorTenaz) c).getRemainingHP() + " HP]"
                : "";
        JLabel statsLabel = new JLabel(
                "<html><center>-" + c.getDrainPerSecond() + " moedas/s" + hpStr + "</center></html>",
                SwingConstants.CENTER
        );
        statsLabel.setFont(new Font("Cambria", Font.PLAIN, 11));
        statsLabel.setForeground(new Color(180, 0, 0));
        statsLabel.setBounds(4, STATS_Y, CARD_W - 8, STATS_H);
        card.add(statsLabel);

        // Debuff (se houver)
        if (c.getDebuff() != Cobrador.Debuff.NONE) {
            JLabel debuffLabel = new JLabel(
                    "<html><center><font color='#FF2E2E'>" + getDebuffLabel(c.getDebuff())
                            + "</font></center></html>",
                    SwingConstants.CENTER
            );
            debuffLabel.setFont(new Font("Cambria", Font.PLAIN, 11));
            debuffLabel.setForeground(new Color(160, 60, 0));
            debuffLabel.setBounds(4, DEBUFF_Y-1, CARD_W - 8, DEBUFF_H);
            card.add(debuffLabel);
        }

        // Botão Pagar
        int displayCost = gm.hasCard("a_imperatriz") ? (int)(c.getPaymentCost() * 0.7f) : c.getPaymentCost();
        JButton payBtn = new JButton("Pagar (" + displayCost + " moedas)");
        payBtn.setFont(new Font("Cambria", Font.PLAIN, 11));
        payBtn.setFocusPainted(false);
        payBtn.setBackground(new Color(60, 140, 60));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBorderPainted(false);
        payBtn.setBounds(BTN_X, BTN_PAY_Y, BTN_W, BTN_H);
        payBtn.setActionCommand("pay_cobrador_" + cobradorIndex);
        payBtn.addActionListener(cHandler);
        card.add(payBtn);

        // Botão Atacar
        JButton attackBtn = new JButton("Atacar");
        attackBtn.setFont(new Font("Cambria", Font.BOLD, 11));
        attackBtn.setFocusPainted(false);
        attackBtn.setBackground(new Color(160, 40, 40));
        attackBtn.setForeground(Color.WHITE);
        attackBtn.setBorderPainted(false);
        attackBtn.setBounds(BTN_X, BTN_ATK_Y, BTN_W, BTN_H);
        attackBtn.setActionCommand("attack_cobrador_" + cobradorIndex);
        attackBtn.addActionListener(cHandler);
        card.add(attackBtn);

        // Tooltip
        card.setToolTipText("<html><b>" + c.getName() + "</b><br>"
                + c.getDescription() + "<br><br>"
                + "Drenagem: -" + c.getDrainPerSecond() + " moedas/s<br>"
                + "Custo para pagar: " + c.getPaymentCost() + " moedas<br>"
                + "Penalidade ao atacar: -" + c.getLuckPenaltyOnAttack() + "% sorte<br>"
                + "Efeito: " + getDebuffLabel(c.getDebuff()) + "</html>");

        return card;
    }

    // Utilitários
    private void removeCobrador(int index) {
        cobradores[index] = null;
        updateStackUI();
        gm.updateClickValueDisplay();
    }

    private int findEmptySlot() {
        for (int i = 0; i < MAX_COBRADORES; i++) {
            if (cobradores[i] == null) return i;
        }
        return -1;
    }

    public int countActive() {
        int count = 0;
        for (Cobrador c : cobradores) {
            if (c != null && c.isActive()) count++;
        }
        return count;
    }

    // Retorna a penalidade total de clique causada por CobradorTenaz ativos
    public int getTotalClickPenalty() {
        int penalty = 0;
        for (Cobrador c : cobradores) {
            if (c != null && c.isActive() && c.getDebuff() == Cobrador.Debuff.CLICK_WEAKEN) {
                penalty += 2; // Cada Tenaz ativo penaliza -2 por clique
            }
        }
        return penalty;
    }

    // Total de moedas drenadas por segundo por todos os cobradores ativos
    public int getTotalDrainPerSecond() {
        int total = 0;
        for (Cobrador c : cobradores) {
            if (c != null && c.isActive()) total += c.getDrainPerSecond();
        }
        return total;
    }

    private String getDebuffLabel(Cobrador.Debuff debuff) {
        switch (debuff) {
            case LUCK_DRAIN: return "Drena sorte/s";
            case CLICK_WEAKEN: return "Enfraquece cliques (-2/clique)";
            case DOUBLE_DRAIN: return "Drenagem em dobro";
            default: return "Nenhum";
        }
    }

    // Reseta o manager para um novo jogo
    public void reset() {
        for (int i = 0; i < MAX_COBRADORES; i++) {
            cobradores[i] = null;
        }
        spawnEnabled = false;
        ticksUntilSpawnCheck = SPAWN_INTERVAL_SEC;
        if (deckContainer != null) {
            deckContainer.removeAll();
            deckContainer.setVisible(false);
            deckContainer.revalidate();
            deckContainer.repaint();
        }
        if (ui != null && ui.gamePanel != null) ui.gamePanel.repaint();
    }
}