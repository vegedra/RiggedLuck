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
    private static final int SPAWN_INTERVAL_SEC = 5;     // Verifica spawn a cada 15s
    private static final int SPAWN_CHANCE_BASE = 100;   // 25% base de chance de spawn
    private static final int COINS_TRIGGER = 10;       // Mínimo de moedas para ativar sistema (100)

    // Dimensões do card dos cobradores
    private static final int CARD_W = 200;
    private static final int CARD_H = 280;
    private static final int STACK_OFFSET = 5;          // deslocamento px entre cada carta no deck

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

        deckContainer = new JPanel(null); // null layout para posicionamento absoluto das cartas
        deckContainer.setBounds(500, 165, containerW, containerH);  // TODO: MEXE AQUI
        deckContainer.setOpaque(false);
        deckContainer.setVisible(false);

        ui.gamePanel.add(deckContainer);
        ui.gamePanel.revalidate();
    }

    // Processa a drenagem, debuffs e spawn
    public void tick(int secondsElapsed) {

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
                c.aplicarDebuff(player);
            }
        }

        // Contagem regressiva para verificação de spawn
        ticksUntilSpawnCheck--;
        if (ticksUntilSpawnCheck <= 0) {
            ticksUntilSpawnCheck = SPAWN_INTERVAL_SEC;
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
        int chance = Math.min(80, SPAWN_CHANCE_BASE + minutes * 3);

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

        int cost = c.getPaymentCost();
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
        player.changeLuck(-luckPenalty);
        gm.updateLuckLabel();

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

        // Renderiza de trás para frente: as cartas de trás ficam no fundo
        // A carta da frente (activeIndexes[0]) é a última adicionada = fica no topo
        for (int layer = activeCount - 1; layer >= 0; layer--) {
            int cobradorIndex = activeIndexes[layer];
            Cobrador c        = cobradores[cobradorIndex];

            // Offset: quanto mais para trás, mais deslocado para baixo e para a direita
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
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 2));

        if (!isFront) {
            // Verso da carta (cartas empilhadas atrás)
            JLabel back = new JLabel("☽", SwingConstants.CENTER);
            back.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
            back.setForeground(new Color(140, 140, 140));
            back.setBounds(0, 0, CARD_W, CARD_H);
            card.add(back);
            return card;
        }

        // Frente da carta (carta interativa da frente)
        // Nome no topo
        JLabel nameLabel = new JLabel(c.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Cambria", Font.BOLD, 11));
        nameLabel.setForeground(new Color(0, 0, 0));
        nameLabel.setBounds(4, 6, CARD_W - 8, 16);
        card.add(nameLabel);

        // Separador decorativo
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255));
        sep.setBounds(8, 25, CARD_W - 16, 1);
        card.add(sep);

        // Sprite / ícone central
        JLabel spriteLabel = new JLabel("", SwingConstants.CENTER);
        spriteLabel.setBounds(0, 30, CARD_W, 80);
        try {
            java.net.URL imgUrl = getClass().getResource(c.getIconPath());
            if (imgUrl != null) {
                ImageIcon icon   = new ImageIcon(imgUrl);
                Image     scaled = icon.getImage().getScaledInstance(64, 72, Image.SCALE_DEFAULT);
                spriteLabel.setIcon(new ImageIcon(scaled));
            } else {
                spriteLabel.setText("👻");
                spriteLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
            }
        } catch (Exception e) {
            spriteLabel.setText("👻");
            spriteLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        }
        card.add(spriteLabel);

        // Drenagem e HP (se Tenaz)
        String hpStr = "";
        if (c instanceof CobradorTenaz) {
            hpStr = "  [" + ((CobradorTenaz) c).getRemainingHP() + " HP]";
        }
        JLabel statsLabel = new JLabel(
                "<html><center><font color='#FF9999'>-" + c.getDrainPerSecond()
                        + " moedas/s" + hpStr + "</font></center></html>",
                SwingConstants.CENTER
        );
        statsLabel.setFont(new Font("Cambria", Font.PLAIN, 10));
        statsLabel.setBounds(4, 112, CARD_W - 8, 16);
        card.add(statsLabel);

        // Debuff (se houver)
        if (c.getDebuff() != Cobrador.Debuff.NONE) {
            JLabel debuffLabel = new JLabel(
                    "<html><center><font color='#FF6666'>✦ " + getDebuffLabel(c.getDebuff())
                            + "</font></center></html>",
                    SwingConstants.CENTER
            );
            debuffLabel.setFont(new Font("Cambria", Font.PLAIN, 9));
            debuffLabel.setBounds(4, 129, CARD_W - 8, 14);
            card.add(debuffLabel);
        }

        // Botão Pagar
        JButton payBtn = new JButton("Pagar (" + c.getPaymentCost() + ")");
        payBtn.setFont(new Font("Cambria", Font.PLAIN, 9));
        payBtn.setFocusPainted(false);
        payBtn.setBackground(new Color(60, 140, 60));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBorderPainted(false);
        payBtn.setBounds(6, 148, CARD_W - 12, 22);
        payBtn.setActionCommand("pay_cobrador_" + cobradorIndex);
        payBtn.addActionListener(cHandler);
        card.add(payBtn);

        // Botão Atacar
        JButton attackBtn = new JButton("Atacar");
        attackBtn.setFont(new Font("Cambria", Font.BOLD, 9));
        attackBtn.setFocusPainted(false);
        attackBtn.setBackground(new Color(160, 40, 40));
        attackBtn.setForeground(Color.WHITE);
        attackBtn.setBorderPainted(false);
        attackBtn.setBounds(6, 173, CARD_W - 12, 22);
        attackBtn.setActionCommand("attack_cobrador_" + cobradorIndex);
        attackBtn.addActionListener(cHandler);
        card.add(attackBtn);

        // Tooltip
        card.setToolTipText("<html><b>" + c.getName() + "</b><br>"
                + c.getDescription() + "<br><br>"
                + "Drenagem: -" + c.getDrainPerSecond() + " moedas/s<br>"
                + "Custo pagar: " + c.getPaymentCost() + " moedas<br>"
                + "Penalidade ataque: -" + c.getLuckPenaltyOnAttack() + "% sorte<br>"
                + "Debuff: " + getDebuffLabel(c.getDebuff()) + "</html>");

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
        spawnEnabled         = false;
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