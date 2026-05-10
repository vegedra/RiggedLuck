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
        int roll = random.nextInt(100);

        if (luck >= 80) {
            // Todas as raridades disponíveis
            if (roll < 3)  return Card.Rarity.MYTHIC;    //  3%
            if (roll < 16) return Card.Rarity.RARE;       // 13%
            if (roll < 54) return Card.Rarity.UNCOMMON;   // 38%
            return Card.Rarity.COMMON;                     // 46%
        }
        if (luck >= 45) {
            // RARE disponível; MYTHIC ainda bloqueado
            // Chance de raro sobe suavemente de 2 % (sorte 45) a 13 % (sorte 79)
            int rareChance = 2 + (luck - 45) / 3;
            if (roll < rareChance)       return Card.Rarity.RARE;
            if (roll < rareChance + 37)  return Card.Rarity.UNCOMMON;
            return Card.Rarity.COMMON;
        }
        // Só COMMON e UNCOMMON
        // Uncommon sobe de 20 % (sorte 0) a 37 % (sorte 44)
        int uncommonChance = 20 + luck * 17 / 44;
        if (roll < uncommonChance) return Card.Rarity.UNCOMMON;
        return Card.Rarity.COMMON;
    }

    // Gera N cartas distintas para o picker (não repete ID entre elas nem com activeCards)
    public Card[] generateOptions(int luck, Card[] activeCards, int count) {
        Card[] options = new Card[count];
        for (int i = 0; i < count; i++) {
            // Passa activeCards + opções já geradas para evitar duplicatas
            Card[] extended = new Card[activeCards.length + i];
            System.arraycopy(activeCards, 0, extended, 0, activeCards.length);
            for (int j = 0; j < i; j++) extended[activeCards.length + j] = options[j];
            options[i] = generateCard(luck, extended);
        }
        return options;
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