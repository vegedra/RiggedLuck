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

        // Drenagem e debuffs de cada cobrador ativo
        for (Cobrador c : cobradores) {
            if (c == null || !c.isActive()) continue;

            // CLICK_DRAIN não drena por segundo, é descontado em cada clique
            if (!c.drainsPerClick()) {
                int baseDrain = c.getDrainPerSecond();
                if (c.getAdaptiveMode() == Cobrador.AdaptiveMode.AMPLIFIED_DRAIN ||
                        c.getAdaptiveMode() == Cobrador.AdaptiveMode.GLASS_CANNON) {
                    baseDrain *= 2;
                }
                int percentDrain = (int)(player.getCoins() * c.getDrainPercent());
                player.changeCoins(-Math.max(baseDrain, percentDrain));
            }

            c.tick();

            // Debuffs de sorte: modo adaptativo LUCK_DRAIN_ADAPTIVE tem prioridade
            if (c.getAdaptiveMode() == Cobrador.AdaptiveMode.LUCK_DRAIN_ADAPTIVE) {
                if (!gm.hasCard("o_ermitao")) player.changeLuck(-3);
            } else if (c.getDebuff() == Cobrador.Debuff.LUCK_DRAIN && gm.hasCard("o_ermitao")) {
                // O Eremita bloqueia o debuff de sorte normal
            } else {
                c.aplicarDebuff(player);
            }
        }

        // Contagem regressiva para o spawn
        ticksUntilSpawnCheck--;
        if (ticksUntilSpawnCheck <= 0) {
            // O Enforcado: cobradores demoram 20% mais para aparecer
            int interval = gm.hasCard("o_enforcado")
                    ? (int)(SPAWN_INTERVAL_SEC * 1.2)
                    : SPAWN_INTERVAL_SEC;
            ticksUntilSpawnCheck = interval;
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
        Cobrador novo = CobradorFactory.createRandom(secondsElapsed, gm.getActiveCards());
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
        if (c.receberPagamento(baseCost)) {
            player.changeCoins(-cost);

            // Pagar ganha Sorte
            int luckGain = c.getLuckOnPay();
            player.changeLuck(luckGain);

            String nome = c.getName();
            removeCobrador(index);
            gm.updateCounter();
            gm.updateLuckLabel();
            ui.showMessage(nome + " foi pago e foi embora. (+" + luckGain + "% sorte)", Color.GREEN);
        }
    }

    // Ataque
    public void handleAttack(int index) {
        Cobrador c = cobradores[index];
        if (c == null || !c.isActive()) return;

        int luckPenalty = c.receberAtaque();

        if (!gm.hasCard("o_ermitao")) {
            player.changeLuck(-luckPenalty);
            gm.updateLuckLabel();
        }

        if (!c.isActive()) {
            // Cobrador derrotado
            int reward = c.getCoinRewardOnDefeat();
            player.changeCoins(reward);
            gm.updateCounter();

            String nome    = c.getName();
            removeCobrador(index);

            boolean gotCard = tryGrantFightCard(c);
            String cardMsg  = gotCard ? " Uma carta foi concedida!" : "";
            String luckMsg  = gm.hasCard("o_ermitao") ? "" : " (-" + luckPenalty + "% sorte)";

            ui.showMessage(nome + " expulso! (+" + reward + " moedas)" + luckMsg + cardMsg, Color.ORANGE);
        } else {
            // Cobrador resistiu (ex: Tenaz no primeiro ataque)
            updateStackUI();
            String luckMsg = gm.hasCard("o_ermitao") ? "" : " (-" + luckPenalty + "% sorte)";
            ui.showMessage(c.getName() + " resistiu!" + luckMsg, Color.YELLOW);
        }

        gm.checkGameOver();
    }

    // Tenta conceder carta de recompensa ao derrotar um cobrador
    private boolean tryGrantFightCard(Cobrador c) {
        int roll = (int)(Math.random() * 100);
        if (roll < c.getRareCardChance()) {
            return gm.grantFightRewardCard();
        }
        return false;
    }

    // Retorna o total de moedas a drenar por clique (AdaptiveMode.CLICK_DRAIN)
    public int getClickDrainPerHit() {
        int drain = 0;
        for (Cobrador c : cobradores) {
            if (c != null && c.isActive() && c.drainsPerClick()) {
                drain += c.getDrainPerSecond();
            }
        }
        return drain;
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
        //String adaptiveBadge = getAdaptiveBadge(c.getAdaptiveMode());     badge do modo adaptativo
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

        // Drenagem efetiva
        String drainStr;
        switch (c.getAdaptiveMode()) {
            case CLICK_DRAIN:
                drainStr = "-" + c.getDrainPerSecond() + " moedas/clique";
                break;
            case AMPLIFIED_DRAIN:
            case GLASS_CANNON:
                drainStr = "-" + (c.getDrainPerSecond() * 2) + " moedas/s";
                break;
            default:
                drainStr = "-" + c.getDrainPerSecond() + " moedas/s";
        }
        String hpStr = (c instanceof CobradorTenaz)
                ? "  [" + ((CobradorTenaz) c).getRemainingHP() + " HP]"
                : "";
        JLabel statsLabel = new JLabel(
                "<html><center>" + drainStr + hpStr + "</center></html>",
                SwingConstants.CENTER
        );
        statsLabel.setFont(new Font("Cambria", Font.PLAIN, 11));
        statsLabel.setForeground(new Color(180, 0, 0));
        statsLabel.setBounds(4, STATS_Y, CARD_W - 8, STATS_H);
        card.add(statsLabel);

        // Debuff (se houver)
        // Debuff / modo adaptativo
        String debuffText = getDebuffDisplayText(c);
        if (!debuffText.isEmpty()) {
            JLabel debuffLabel = new JLabel(
                    "<html><center><font color='#FF2E2E'>" + debuffText + "</font></center></html>",
                    SwingConstants.CENTER
            );
            debuffLabel.setFont(new Font("Cambria", Font.PLAIN, 11));
            debuffLabel.setBounds(4, DEBUFF_Y - 1, CARD_W - 8, DEBUFF_H);
            card.add(debuffLabel);
        }

        // Botão Pagar — custo + sorte a ganhar
        int displayCost = gm.hasCard("a_imperatriz") ? (int)(c.getPaymentCost() * 0.7f) : c.getPaymentCost();
        JButton payBtn = new JButton(
                "<html><center>Pagar (" + displayCost + " moedas)"
                        + "<br><font color='#00CC00'>+" + c.getLuckOnPay() + "% sorte</font></center></html>"
        );
        payBtn.setFont(new Font("Cambria", Font.PLAIN, 10));
        payBtn.setFocusPainted(false);
        payBtn.setBackground(new Color(60, 140, 60));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBorderPainted(false);
        payBtn.setBounds(BTN_X, BTN_PAY_Y, BTN_W, BTN_H);
        payBtn.setActionCommand("pay_cobrador_" + cobradorIndex);
        payBtn.addActionListener(cHandler);
        card.add(payBtn);

        // Botão Atacar — mostra recompensa ao vencer
        JButton attackBtn = new JButton(
                "<html><center>Atacar"
                        + " <font size='1'>(+" + c.getCoinRewardOnDefeat() + " moedas)</font></center></html>"
        );
        attackBtn.setFont(new Font("Cambria", Font.BOLD, 11));
        attackBtn.setFocusPainted(false);
        attackBtn.setBackground(new Color(160, 40, 40));
        attackBtn.setForeground(Color.WHITE);
        attackBtn.setBorderPainted(false);
        attackBtn.setBounds(BTN_X, BTN_ATK_Y, BTN_W, BTN_H);
        attackBtn.setActionCommand("attack_cobrador_" + cobradorIndex);
        attackBtn.addActionListener(cHandler);
        card.add(attackBtn);

        // Tooltip completo
        String adaptiveDesc = c.getAdaptiveMode() != Cobrador.AdaptiveMode.NONE
                ? "<br><b>Adaptativo:</b> " + getAdaptiveDescription(c.getAdaptiveMode())
                : "";
        card.setToolTipText("<html><b>" + c.getName() + "</b><br>"
                + c.getDescription()
                + "<br><br>Drenagem: " + drainStr
                + "<br>Custo para pagar: " + displayCost + " moedas (+" + c.getLuckOnPay() + "% sorte)"
                + "<br>Penalidade ao atacar: -" + c.getLuckPenaltyOnAttack() + "% sorte"
                + "<br>Recompensa ao derrotar: +" + c.getCoinRewardOnDefeat() + " moedas"
                + "<br>Chance de carta: " + c.getRareCardChance() + "%"
                + adaptiveDesc + "</html>");

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

    private String getDebuffDisplayText(Cobrador c) {
        switch (c.getAdaptiveMode()) {
            case CLICK_DRAIN:         return "Drena por clique";
            case AMPLIFIED_DRAIN:     return "Drenagem 2x";
            case LUCK_DRAIN_ADAPTIVE: return "Drena sorte -3%/s";
            case GLASS_CANNON:        return "Dano 2x / HP reduzido";
            default:
                switch (c.getDebuff()) {
                    case LUCK_DRAIN:   return "Drena sorte/s";
                    case CLICK_WEAKEN: return "Enfraquece cliques (-2/clique)";
                    case DOUBLE_DRAIN: return "Drenagem em dobro";
                    default:           return "";
                }
        }
    }

    private String getAdaptiveBadge(Cobrador.AdaptiveMode mode) {
        switch (mode) {
            case CLICK_DRAIN:         return " [⚡]";
            case AMPLIFIED_DRAIN:     return " [⬆]";
            case LUCK_DRAIN_ADAPTIVE: return " [☽]";
            case GLASS_CANNON:        return " [💀]";
            default:                  return "";
        }
    }

    private String getAdaptiveSpawnMsg(Cobrador.AdaptiveMode mode) {
        switch (mode) {
            case CLICK_DRAIN:         return " Drena a cada clique!";
            case AMPLIFIED_DRAIN:     return " Explora sua geração passiva!";
            case LUCK_DRAIN_ADAPTIVE: return " Mira na sua sorte!";
            case GLASS_CANNON:        return " Perigoso, mas pode cair rápido!";
            default:                  return "";
        }
    }

    private String getAdaptiveDescription(Cobrador.AdaptiveMode mode) {
        switch (mode) {
            case CLICK_DRAIN:         return "Drena moedas por clique (build Clique)";
            case AMPLIFIED_DRAIN:     return "Drenagem dobrada/s (build Passiva)";
            case LUCK_DRAIN_ADAPTIVE: return "Drena -3% sorte/s (build Sorte)";
            case GLASS_CANNON:        return "Drenagem dobrada, HP reduzido (build Risco)";
            default:                  return "";
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