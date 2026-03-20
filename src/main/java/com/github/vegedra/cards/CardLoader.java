/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Lê cards.json e devolve a lista de CardData.
*/

package com.github.vegedra.cards;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardLoader {

    private static List<CardData> allCards = null;

    // Carrega e cacheia a lista de cartas do JSON.
    public static List<CardData> loadAll() {
        if (allCards != null) return allCards;

        try (InputStream is = CardLoader.class.getResourceAsStream("/cards.json")) {
            // Erro
            if (is == null) {
                System.err.println("[CardLoader] ERRO: cards.json não encontrado em resources");
                return new ArrayList<>();
            }

            // Carrega os dados
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, List<CardData>>>() {}.getType();
            Map<String, List<CardData>> root = gson.fromJson(new InputStreamReader(is), mapType);

            allCards = root.getOrDefault("cards", new ArrayList<>());
            System.out.println("[CardLoader] " + allCards.size() + " cartas carregadas.");
            if (!allCards.isEmpty()) {
                CardData first = allCards.get(0);
                System.out.println("Primeira carta: " + first.name + " - " + first.desc);
            }
            return allCards;

        // Erro
        } catch (Exception e) {
            System.err.println("[CardLoader] Erro: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Retorna todas as cartas de um tipo específico.
    public static List<CardData> getByType(String type) {
        List<CardData> result = new ArrayList<>();
        for (CardData cd : loadAll()) {
            if (type.equalsIgnoreCase(cd.type)) result.add(cd);
        }
        return result;
    }

    // Retorna todas as cartas de uma raridade específica.
    public static List<CardData> getByRarity(String rarity) {
        List<CardData> result = new ArrayList<>();
        for (CardData cd : loadAll()) {
            if (rarity.equalsIgnoreCase(cd.rarity)) result.add(cd);
        }
        return result;
    }
}
