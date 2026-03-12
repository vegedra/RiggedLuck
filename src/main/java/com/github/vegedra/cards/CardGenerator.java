/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    CardGenerator — sorteia uma carta do pool carregado do json.
*/

package com.github.vegedra.cards;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CardGenerator {

    private final Random random = new Random();

    // Gera uma carta aleatória baseada na Sorte atual do jogador (0–100)
    public Card generateCard(int luck) {
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

        // Se não tiver cartas dessa raridade, usa tudo no pool
        if (filtered.isEmpty()) {
            System.err.println("[CardGenerator] Nenhuma carta " + rarity.name() + " no pool. Usando pool completo.");
            filtered = pool;
        }

        // 3. Sorteia uma carta do pool filtrado
        CardData chosen = filtered.get(random.nextInt(filtered.size()));

        // 4. Instancia e retorna
        return chosen.instantiate();
    }

    // Raridade
    private Card.Rarity rollRarity(int luck) {
        // Bônus por sorte acima de 80
        int bonus = Math.max(0, (luck - 80) / 10);  // 0 a 5

        int common   = 50 - (bonus * 2);
        int uncommon = 30;
        int rare     = 15 + bonus;
        int mythic   = 5  + bonus;

        int roll = random.nextInt(100);

        if (roll < mythic)                        return Card.Rarity.MYTHIC;
        if (roll < mythic + rare)                 return Card.Rarity.RARE;
        if (roll < mythic + rare + uncommon)      return Card.Rarity.UNCOMMON;
        return Card.Rarity.COMMON;
    }

    // Fallback
    private Card fallbackCard() {
        return new Card(
                "fallback", "Carta Perdida",
                "O destino falhou em te entregar uma carta. Os demônios riem de você...",
                Card.CardType.CLICK, Card.Rarity.COMMON,
                1, 0, 0, 0);
    }
}