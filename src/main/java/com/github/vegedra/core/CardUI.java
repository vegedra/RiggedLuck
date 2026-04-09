/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Cuida da renderização visual das cartas no jogo
*/

package com.github.vegedra.core;

import com.github.vegedra.cards.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CardUI {

    // Objeto das classes
    private final UI ui;
    private final GameManager gm;
    private final ActionListener cHandler;

    // Construtor
    public CardUI(UI ui, GameManager gm, ActionListener cHandler) {
        this.ui = ui;
        this.gm = gm;
        this.cHandler = cHandler;
    }

    // Cor das cartas de acordo com sua raridade
    private String getRarityColor(Card.Rarity rarity) {
        switch (rarity) {
            case MYTHIC:   return "#FF44FF";
            case RARE:     return "#44AAFF";
            case UNCOMMON: return "#44FF88";
            default:       return "#000000";
        }
    }

    // Usa emojis como sprite
    private String getTypeEmoji(Card.CardType type) {
        switch (type) {
            case CLICK:   return "👆";
            case PASSIVE: return "⏳";
            case LUCK:    return "🍀";
            case DEFENSE: return "🛡️";
            case RISK:    return "🎲";
            case SYNERGY: return "⭐";
            default:      return "🃏";
        }
    }

    // Atualiza UI das cartas
    public void updateCardUI(int index, Card c) {
        ui.cardSlots[index].removeAll();
        ui.cardSlots[index].setLayout(new BorderLayout());

        // Se nao houver carta no slot
        if (c == null) {
            JLabel empty = new JLabel("Vazio", SwingConstants.CENTER);
            empty.setFont(new Font("Cambria", Font.PLAIN, 12));
            ui.cardSlots[index].add(empty, BorderLayout.CENTER);
            ui.cardSlots[index].setToolTipText(null);
        } else {
            String cardName = (c.name != null) ? c.name : "Carta sem nome";
            String cardDesc = (c.desc != null) ? c.desc : "";

            // Constrói tooltip antes para usar no botão também
            String activeTag = "";
            if (c.active) {
                activeTag = c.used
                        ? "<font color='gray'><i>Já utilizada.</i></font><br><br>"
                        : "<font color='orange'><b>[ USO ÚNICO — clique para ativar ]</b></font><br><br>";
            }
            int discardCost = gm.calcDiscardCost(c);
            String tooltip = "<html><b>" + cardName + " (" + c.rarity + ")</b><br><br>" +
                    activeTag +
                    cardDesc + "<br><br>" +
                    "Clique: +" + c.clickValue + "<br>" +
                    "Moedas/s: +" + c.coinsPerSecond + "<br>" +
                    "Sorte/clique: +" + c.luckPerClick + "%<br>" +
                    "Sorte/s: +" + c.luckPerSecond + "%<br><br>" +
                    "Descartar custa: " + discardCost + " moedas</html>";

            // Cartas ativadas pelo jogador (não A Estrela, que dispara sozinha)
            boolean playerActivated = c.active
                    && c.activeEffect != Card.ActiveEffect.SECOND_CHANCE;

            // Carta ativa disponível: vira botão
            if (playerActivated && !c.used) {
                JButton activateBtn = new JButton(
                        "<html><center><font color='" + getRarityColor(c.rarity) + "'>"
                                + getTypeEmoji(c.type) + " " + cardName + "</font><br><font size='1'>[ USAR ]</font></center></html>"
                );
                activateBtn.setFocusPainted(false);
                activateBtn.setFont(new Font("Cambria", Font.BOLD, 10));
                activateBtn.setActionCommand("activate_" + index);
                activateBtn.addActionListener(cHandler);
                activateBtn.setToolTipText(tooltip);
                ui.cardSlots[index].add(activateBtn, BorderLayout.CENTER);

            // Carta já usada: nome riscado em cinza
            } else if (c.active && c.used) {
                JLabel usedLabel = new JLabel(
                        "<html><center><font color='gray'><s>" + getTypeEmoji(c.type) + "</s></font>"
                                + "<br><font size='1' color='gray'>usado</font></center></html>",
                        SwingConstants.CENTER
                );
                usedLabel.setFont(new Font("Cambria", Font.PLAIN, 10));
                usedLabel.setToolTipText(tooltip); // ← tooltip no label
                ui.cardSlots[index].add(usedLabel, BorderLayout.CENTER);

            // Carta passiva
            } else {
                String emoji = getTypeEmoji(c.type);
                JLabel passiveLabel = new JLabel(
                        "<html><center><font color='" + getRarityColor(c.rarity) + "' size='5'>" + emoji + "</font></center></html>",
                        SwingConstants.CENTER
                );
                passiveLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                passiveLabel.setToolTipText(tooltip);
                ui.cardSlots[index].add(passiveLabel, BorderLayout.CENTER);
            }

            // Botão de descarte — sempre presente
            JButton discard = new JButton("X");
            discard.setFocusPainted(false);
            discard.setMargin(new Insets(2, 4, 2, 4));
            discard.setFont(new Font("Cambria", Font.PLAIN, 10));
            discard.setActionCommand("discard_" + index);
            discard.addActionListener(cHandler);
            ui.cardSlots[index].add(discard, BorderLayout.EAST);

            // Tooltip no painel para cobrir o botão de descarte e áreas restantes
            ui.cardSlots[index].setToolTipText(tooltip);
        }

        // Botão direito: descarta a carta diretamente (com confirmação)
        final int slotIndex = index;
        ui.cardSlots[index].addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                    Card card = gm.getActiveCards()[slotIndex];
                    if (card == null) return;
                    int sellValue = gm.calcSellValue(card);
                    int confirm = javax.swing.JOptionPane.showConfirmDialog(
                            ui.cardSlots[slotIndex],
                            "<html>Vender <b>" + card.name + "</b>?<br>Você receberá <b>+" + sellValue + " moedas</b>.</html>",
                            "Vender carta",
                            javax.swing.JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                        gm.handleDiscard(slotIndex);
                    }
                }
            }
        });

        ui.cardSlots[index].revalidate();
        ui.cardSlots[index].repaint();
    }
}
