/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Janela de seleção de carta estilo roguelikes
*/

package com.github.vegedra.core;

import com.github.vegedra.cards.Card;

import javax.swing.*;
import java.awt.*;

public class CardPickerDialog extends JDialog {

    // Guarda a carta que o jogador escolheu e retorna null se fechou sem escolher
    private Card chosenCard = null;

    // Construtor (monta a janela)
    public CardPickerDialog(JFrame parent, Card[] options, int cost) {
        super(parent, "Escolha uma Carta", true); // true = modal (bloqueia o jogo)

        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 0));

        // Cabeçalho: título com o custo da carta
        JLabel title = new JLabel("Escolha uma carta (" + cost + " moedas)", SwingConstants.CENTER);
        title.setFont(new Font("Cambria", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        // Centro: as 3 cartas lado a lado
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 10, 0)); // 1 linha, 3 colunas, 10px de espaço
        cardsRow.setBackground(Color.WHITE);
        cardsRow.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        for (Card card : options) cardsRow.add(buildCardPanel(card)); // cria um painel pra cada carta
        add(cardsRow, BorderLayout.CENTER);

        // Rodapé: botão para fechar sem escolher
        JButton cancelBtn = new JButton("Fechar");
        cancelBtn.setFont(new Font("Cambria", Font.PLAIN, 12));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose()); // dispose() fecha a janela
        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 14, 0));
        footer.add(cancelBtn);
        add(footer, BorderLayout.SOUTH);

        // Tamanho e posição: centralizado na janela do jogo
        setSize(680, 340);
        setLocationRelativeTo(parent);
    }

    /*
        Constrói o painel visual de uma carta individual.
        Cada carta tem: nome, raridade, separador, descrição, stats e botão "Escolher".
        Clicar em qualquer lugar do painel ou no botão seleciona a carta e fecha o dialog.
    */
    private JPanel buildCardPanel(Card card) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // empilha os elementos verticalmente
        panel.setBackground(Color.WHITE);
        // Borda colorida pela raridade da carta (2px), com padding interno
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getRarityColor(card.rarity), 2),
                BorderFactory.createEmptyBorder(10, 10, 8, 10)
        ));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // cursor de mão ao passar

        // Nome da carta (cor da raridade)
        JLabel name = new JLabel("<html><center>" + card.name + "</center></html>", SwingConstants.CENTER);
        name.setFont(new Font("Cambria", Font.BOLD, 14));
        name.setForeground(getRarityColor(card.rarity));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(name);
        panel.add(Box.createVerticalStrut(4)); // espaço vertical fixo

        // Raridade em cinza e pequeno abaixo do nome
        JLabel rarity = new JLabel(card.rarity.name(), SwingConstants.CENTER);
        rarity.setFont(new Font("Cambria", Font.PLAIN, 12));
        rarity.setForeground(Color.GRAY);
        rarity.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(rarity);
        panel.add(Box.createVerticalStrut(6));

        // Linha separadora horizontal
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(6));

        // Descrição da carta
        JLabel desc = new JLabel("<html><center>" + card.desc + "</center></html>", SwingConstants.CENTER);
        desc.setFont(new Font("Cambria", Font.PLAIN, 14));
        desc.setForeground(Color.DARK_GRAY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(desc);
        panel.add(Box.createVerticalStrut(6));

        // Stats numéricos (só aparece se houver algum valor)
        String stats = buildStats(card);
        if (!stats.isEmpty()) {
            JLabel statsLabel = new JLabel("<html><center>" + stats + "</center></html>", SwingConstants.CENTER);
            statsLabel.setFont(new Font("Cambria", Font.PLAIN, 13));
            statsLabel.setForeground(new Color(0, 140, 0)); // verde
            statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(statsLabel);
            panel.add(Box.createVerticalStrut(6));
        }

        // Empurra o botão para o fundo do painel
        panel.add(Box.createVerticalGlue());

        // Botão "Escolher" -> salva a carta e fecha a janela
        JButton btn = new JButton("Escolher");
        btn.setFont(new Font("Cambria", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btn.addActionListener(e -> { chosenCard = card; dispose(); });
        panel.add(btn);

        // Clicar em qualquer lugar do painel também escolhe a carta
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    chosenCard = card; dispose();
                }
            }
        });

        return panel;
    }

    // Retorna a cor correspondente à raridade da carta
    private Color getRarityColor(Card.Rarity rarity) {
        switch (rarity) {
            case MYTHIC:   return new Color(180, 0, 220);   // roxo
            case RARE:     return new Color(0, 100, 220);   // azul
            case UNCOMMON: return new Color(0, 160, 80);    // verde
            default: return Color.BLACK;                              // comum = preto
        }
    }

    /*
        Monta a linha de stats da carta com apenas os valores não-zero.
        Exemplo: "+4 clique  +10/s  [ USO ÚNICO ]"
    */
    private String buildStats(Card card) {
        StringBuilder sb = new StringBuilder();
        if (card.clickValue != 0) sb.append("+").append(card.clickValue).append(" clique  ");
        if (card.coinsPerSecond != 0) sb.append("+").append(card.coinsPerSecond).append("/s  ");
        if (card.luckPerSecond != 0) sb.append("+").append(card.luckPerSecond).append("% sorte/s  ");
        if (card.luckPerClick != 0) sb.append("+").append(card.luckPerClick).append("% sorte/clique  ");
        if (card.active)    sb.append("[ USO ÚNICO ]");
        return sb.toString().trim();
    }

    // Chamado pelo GameManager após fechar o dialog para saber o que foi escolhido
    public Card getChosenCard() { return chosenCard; }
}