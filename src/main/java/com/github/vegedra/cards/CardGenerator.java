/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    CardGenerator — sorteia uma carta do pool carregado do json.
*/

package com.github.vegedra.cards;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CardGenerator {

    private final Random random = new Random();

    // Gera uma carta aleatória baseada na Sorte atual do jogador (0–100)
    public Card generateCard(int luck, Card[] activeCards) {
        List<CardData> pool = CardLoader.loadAll();

        if (pool.isEmpty()) {
            System.err.println("[CardGenerator] Cartas não carregadas, verificar cards.json.");
            return fallbackCard();
        }

        // 1. Sorteia raridade
        Card.Rarity rarity = rollRarity(luck);

        // 2. Filtra pool por raridade
        List<CardData> filtered = pool.stream()
                .filter(cd -> rarity.name().equalsIgnoreCase(cd.rarity))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            System.err.println("[CardGenerator] Nenhuma carta " + rarity.name() + " no pool. Usando pool completo.");
            filtered = pool;
        }

        // 3. Filtra cartas já ativas (evita duplicatas)
        Set<String> activeIds = new HashSet<>();
        for (Card c : activeCards) {
            if (c != null) activeIds.add(c.id);
        }
        List<CardData> available = filtered.stream()
                .filter(cd -> !activeIds.contains(cd.id))
                .collect(Collectors.toList());

        // Fallback: se todas da raridade já estiverem ativas, usa pool completo filtrado
        if (available.isEmpty()) available = filtered;

        // 4. Sorteia e retorna
        CardData chosen = available.get(random.nextInt(available.size()));
        return chosen.instantiate();
    }

    // Raridade
    private Card.Rarity rollRarity(int luck) {
        // Pesos base (quanto maior o peso, maior a chance)
        int commonWeight   = 60;
        int uncommonWeight = 30;
        int rareWeight     = 9;
        int mythicWeight   = 1;  // mítico só aparece com sorte >= 70

        // Bônus por sorte a partir de 60
        final int minForBonus = 60;
        if (luck > minForBonus) {
            // A cada 10 pontos acima de 50, ganha 1 de peso para raro
            // e 0.5 para mítico (arredondado para baixo, mínimo 1 quando aplicável)
            int bonus = (luck - minForBonus) / 10;  // 0 a 5

            rareWeight += bonus;
            // Mítico só ganha peso se luck > 70 (bonus >=2)
            if (bonus >= 2) {
                mythicWeight += Math.max(1, bonus / 2);  // para bonus=2 -> +1, bonus=4 -> +2, etc.
            }

            // Reduz o peso de comum, mas mantém um mínimo para não sumirem
            commonWeight -= (bonus + (bonus / 2));
            if (commonWeight < 30) commonWeight = 30;
        }

        // Garante que mítico não ultrapasse um limite razoável (máximo 5)
        if (mythicWeight > 5) mythicWeight = 5;

        // Sorteio baseado nos pesos acumulados
        int totalWeight = commonWeight + uncommonWeight + rareWeight + mythicWeight;
        int roll = random.nextInt(totalWeight);

        if (roll < mythicWeight)               return Card.Rarity.MYTHIC;
        if (roll < mythicWeight + rareWeight)  return Card.Rarity.RARE;
        if (roll < mythicWeight + rareWeight + uncommonWeight)
            return Card.Rarity.UNCOMMON;
        return Card.Rarity.COMMON;
    }

    // Fallback
    private Card fallbackCard() {
        return new Card(
                "fallback", "Carta Perdida",
                "O destino falhou em te entregar uma carta. Os demônios riem de você...",
                Card.CardType.CLICK, Card.Rarity.COMMON,
                1, 0, 0, 0,
                false, Card.ActiveEffect.NONE);
    }
}