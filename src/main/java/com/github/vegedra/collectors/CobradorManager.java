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
    private static final int SPAWN_INTERVAL_SEC = 15;   // Verifica spawn a cada 15 s
    private static final int SPAWN_CHANCE_BASE = 20;    // 20% base de chance de spawn
    private static final int COINS_TRIGGER = 100;  // Mínimo de moedas para ativar sistema

    // Referências
    private final UI ui;
    private final Player player;
    private final GameManager gm;
    private final ActionListener cHandler;

    // Estados
    private final Cobrador[] cobradores = new Cobrador[MAX_COBRADORES];
    private final JPanel[] cobradorSlots = new JPanel[MAX_COBRADORES];

    private boolean spawnEnabled = false;
    private int ticksUntilSpawnCheck = SPAWN_INTERVAL_SEC;

    // Painel principal
    private JPanel cobradorPanel;

    // Construtor
    public CobradorManager(UI ui, Player player, GameManager gm, ActionListener cHandler) {
        this.ui       = ui;
        this.player   = player;
        this.gm       = gm;
        this.cHandler = cHandler;
    }

    // Inicializa o painel dos cobradores
    public void initUI() {
        cobradorPanel = new JPanel();
        cobradorPanel.setBounds(440, 130, 315, 335);
        cobradorPanel.setLayout(new BoxLayout(cobradorPanel, BoxLayout.Y_AXIS));
        cobradorPanel.setBackground(Color.WHITE);
        cobradorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 0, 0), 1),
                "⚠ Cobradores"
        ));
        cobradorPanel.setVisible(false); // Aparece só quando há cobradores ativos

        // Slots para os stacks de cobradores
        for (int i = 0; i < MAX_COBRADORES; i++) {
            cobradorSlots[i] = new JPanel();
            cobradorSlots[i].setMaximumSize(new Dimension(305, 58));
            cobradorSlots[i].setMinimumSize(new Dimension(305, 58));
            cobradorSlots[i].setPreferredSize(new Dimension(305, 58));
            cobradorSlots[i].setVisible(false);
            cobradorPanel.add(cobradorSlots[i]);
            cobradorPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        // Adiciona à UI
        ui.gamePanel.add(cobradorPanel);
        ui.gamePanel.revalidate();
    }

    // Processa a drenagem, debuffs e spawn
    public void tick(int secondsElapsed) {
        /*
        System.out.println("[CM] tick s=" + secondsElapsed
                + " coins=" + player.getCoins()
                + " enabled=" + spawnEnabled
                + " countdown=" + ticksUntilSpawnCheck);
        */
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

        gm.updateCounter();
        gm.updateLuckLabel();
    }


    // Verifica spawn
    private void trySpawn(int secondsElapsed) {
        if (countActive() >= MAX_COBRADORES) return;

        // Chance de spawn cresce com o tempo de jogo (máx 80%)
        int minutes = secondsElapsed / 60;
        int chance  = Math.min(80, SPAWN_CHANCE_BASE + minutes * 3);

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

        updateSlotUI(emptyIndex);
        System.out.println("[CM] updateSlotUI ok");
        refreshPanelVisibility();
        System.out.println("[CM] panel visible=" + cobradorPanel.isVisible());

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

        boolean paid = c.receberPagamento(cost);
        if (paid) {
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
            updateSlotUI(index);
            ui.showMessage(c.getName() + " resistiu! (-" + luckPenalty + "% sorte)", Color.YELLOW);
        }

        // Verifica game over
        gm.checkGameOver();
    }


    // UI dos slots
    private void updateSlotUI(int index) {
        Cobrador c = cobradores[index];
        JPanel slot = cobradorSlots[index];
        slot.removeAll();

        if (c == null) {
            slot.setVisible(false);
            return;
        }

        slot.setLayout(new BorderLayout(4, 0));
        slot.setBackground(new Color(255, 238, 238));
        slot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(200, 50, 50)),
                BorderFactory.createEmptyBorder(4, 6, 4, 4)
        ));

        // Ícone GIF
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(38, 38));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            java.net.URL imgUrl = getClass().getResource(c.getIconPath());
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image scaled = icon.getImage().getScaledInstance(36, 36, Image.SCALE_DEFAULT);
                iconLabel.setIcon(new ImageIcon(scaled));
            } else {
                throw new Exception("Asset não encontrado: " + c.getIconPath());
            }
        } catch (Exception e) {
            iconLabel.setText("👻");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        }
        slot.add(iconLabel, BorderLayout.WEST);

        // Info central
        String hpBar = "";
        if (c instanceof CobradorTenaz) {
            CobradorTenaz tenaz = (CobradorTenaz) c;
            hpBar = " [" + tenaz.getRemainingHP() + " HP]";
        }
        String debuffTag = c.getDebuff() != Cobrador.Debuff.NONE
                ? "<br><font color='#CC0000' size='1'>✦ " + getDebuffLabel(c.getDebuff()) + "</font>"
                : "";

        JLabel infoLabel = new JLabel(
                "<html><b>" + c.getName() + "</b>" +
                        "<br><font color='gray' size='1'>-" + c.getDrainPerSecond() + " moedas/s" + hpBar + "</font>" +
                        debuffTag + "</html>"
        );
        infoLabel.setFont(new Font("Cambria", Font.PLAIN, 10));
        slot.add(infoLabel, BorderLayout.CENTER);

        // Botões (direita)
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        actionPanel.setBackground(new Color(255, 238, 238));
        actionPanel.setPreferredSize(new Dimension(88, 50));

        JButton payBtn = new JButton("Pagar (" + c.getPaymentCost() + ")");
        payBtn.setFont(new Font("Cambria", Font.PLAIN, 9));
        payBtn.setFocusPainted(false);
        payBtn.setBackground(new Color(180, 255, 180));
        payBtn.setActionCommand("pay_cobrador_" + index);
        payBtn.addActionListener(cHandler);

        JButton attackBtn = new JButton("Atacar");
        attackBtn.setFont(new Font("Cambria", Font.BOLD, 9));
        attackBtn.setFocusPainted(false);
        attackBtn.setBackground(new Color(255, 160, 160));
        attackBtn.setActionCommand("attack_cobrador_" + index);
        attackBtn.addActionListener(cHandler);

        actionPanel.add(payBtn);
        actionPanel.add(attackBtn);
        slot.add(actionPanel, BorderLayout.EAST);

        // Tooltip completo no painel
        slot.setToolTipText("<html><b>" + c.getName() + "</b><br>" +
                c.getDescription() + "<br><br>" +
                "Drenagem: -" + c.getDrainPerSecond() + " moedas/s<br>" +
                "Custo pagar: "  + c.getPaymentCost()         + " moedas<br>" +
                "Penalidade ataque: -" + c.getLuckPenaltyOnAttack() + "% sorte<br>" +
                "Debuff: " + getDebuffLabel(c.getDebuff()) + "</html>");

        slot.setVisible(true);
        slot.revalidate();
        slot.repaint();
    }

    // Utilitários
    private void removeCobrador(int index) {
        cobradores[index] = null;
        cobradorSlots[index].setVisible(false);
        cobradorSlots[index].removeAll();
        cobradorSlots[index].revalidate();
        cobradorSlots[index].repaint();
        refreshPanelVisibility();
        // Atualiza clickValueLabel para refletir fim do debuff CLICK_WEAKEN
        gm.updateClickValueDisplay();
    }

    private void refreshPanelVisibility() {
        if (cobradorPanel == null) return;
        boolean hasAny = countActive() > 0;
        cobradorPanel.setVisible(hasAny);
        cobradorPanel.revalidate();
        cobradorPanel.repaint();
        ui.gamePanel.repaint();
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
            if (cobradorSlots[i] != null) {
                cobradorSlots[i].setVisible(false);
                cobradorSlots[i].removeAll();
            }
        }
        spawnEnabled = false;
        ticksUntilSpawnCheck = SPAWN_INTERVAL_SEC;
        refreshPanelVisibility();
    }
}