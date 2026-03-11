/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    CardGenerator — sorteia uma carta do pool carregado do JSON.

    Fluxo:
      1. Determina a raridade com base em chances + bônus de Sorte
      2. Filtra cartas daquela raridade
      3. Sorteia uma aleatoriamente
      4. Decide se sai normal ou invertida (chance = Sorte / 100)
      5. Instancia e retorna o Card
*/

package com.github.vegedra.cards;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CardGenerator {

    private final Random random = new Random();

    /**
     * Gera uma carta aleatória baseada na Sorte atual do jogador (0–100).
     * Sorte alta → mais chance de raridade maior e carta normal.
     */
    public Card generateCard(int luck) {
        List<CardData> pool = CardLoader.loadAll();

        if (pool.isEmpty()) {
            System.err.println("[CardGenerator] Pool vazio! Verifique cards.json.");
            return fallbackCard();
        }

        // 1. Sorteia raridade
        Card.Rarity rarity = rollRarity(luck);

        // 2. Filtra pool por raridade
        final String rarityName = rarity.name();
        List<CardData> filtered = pool.stream()
                .filter(cd -> rarityName.equalsIgnoreCase(cd.rarity))
                .collect(Collectors.toList());

        // Fallback: se não tiver cartas dessa raridade, usa todo o pool
        if (filtered.isEmpty()) {
            System.err.println("[CardGenerator] Nenhuma carta " + rarityName + " no pool. Usando pool completo.");
            filtered = pool;
        }

        // 3. Sorteia uma carta do pool filtrado
        CardData chosen = filtered.get(random.nextInt(filtered.size()));

        // 4. Decide normal vs invertida
        // Chance de sair normal = Sorte / 100  (sorte 70 → 70% normal)
        float normalChance = Math.max(0.05f, Math.min(0.95f, luck / 100f));
        boolean inverted = random.nextFloat() > normalChance;

        // 5. Instancia e retorna
        return chosen.instantiate(inverted);
    }

    // -------------------------------------------------------------------------
    // Raridade
    // -------------------------------------------------------------------------

    /**
     * Tabela base:
     *   COMMON   50%  → reduz com sorte alta
     *   UNCOMMON 30%
     *   RARE     15%
     *   MYTHIC    5%  → aumenta com sorte alta
     *
     * Bônus de Sorte: cada 10 pontos acima de 50 transfere 1% de COMMON para MYTHIC/RARE
     */
    private Card.Rarity rollRarity(int luck) {
        // Bônus por sorte acima de 50
        int bonus = Math.max(0, (luck - 50) / 10);  // 0 a 5

        int common   = 50 - (bonus * 2);   // 50% → 40% no máximo
        int uncommon = 30;
        int rare     = 15 + bonus;          // 15% → 20%
        int mythic   = 5  + bonus;          // 5%  → 10%

        int roll = random.nextInt(100);

        if (roll < mythic)                        return Card.Rarity.MYTHIC;
        if (roll < mythic + rare)                 return Card.Rarity.RARE;
        if (roll < mythic + rare + uncommon)      return Card.Rarity.UNCOMMON;
        return Card.Rarity.COMMON;
    }

    // -------------------------------------------------------------------------
    // Fallback de emergência (não depende do JSON)
    // -------------------------------------------------------------------------
    private Card fallbackCard() {
        return new Card(
                "fallback", "Carta Perdida",
                "O destino falhou em te entregar uma carta.",
                Card.CardType.CLICK, Card.Rarity.COMMON, false,
                1, 0
        );
    }
}