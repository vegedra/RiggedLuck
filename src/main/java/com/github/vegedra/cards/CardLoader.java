/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    CardLoader — lê cards.json e devolve a lista de CardData.
    Requer Gson no classpath (ex: gson-2.10.1.jar).

    Coloque cards.json em: src/main/resources/cards.json
    (ou no mesmo diretório do .jar ao rodar)
*/

package com.github.vegedra.cards;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CardLoader {

    private static List<CardData> allCards = null;

    /**
     * Carrega e cacheia a lista de cartas do JSON.
     * Chamar uma vez na inicialização do jogo.
     * @return lista imutável de CardData
     */
    public static List<CardData> loadAll() {
        if (allCards != null) return allCards;

        try (InputStream is = CardLoader.class.getResourceAsStream("/cards.json")) {
            if (is == null) {
                System.err.println("[CardLoader] ERRO: cards.json não encontrado em resources!");
                return Collections.emptyList();
            }

            Gson gson = new Gson();
            // O JSON é um objeto com chaves por tipo: { "cards": [...] }
            Type mapType = new TypeToken<Map<String, List<CardData>>>() {}.getType();
            Map<String, List<CardData>> root = gson.fromJson(new InputStreamReader(is), mapType);

            allCards = root.getOrDefault("cards", new ArrayList<>());
            System.out.println("[CardLoader] " + allCards.size() + " cartas carregadas.");
            return allCards;

        } catch (Exception e) {
            System.err.println("[CardLoader] Falha ao carregar cards.json: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retorna todas as cartas de um tipo específico.
     */
    public static List<CardData> getByType(String type) {
        List<CardData> result = new ArrayList<>();
        for (CardData cd : loadAll()) {
            if (type.equalsIgnoreCase(cd.type)) result.add(cd);
        }
        return result;
    }

    /**
     * Retorna todas as cartas de uma raridade específica.
     */
    public static List<CardData> getByRarity(String rarity) {
        List<CardData> result = new ArrayList<>();
        for (CardData cd : loadAll()) {
            if (rarity.equalsIgnoreCase(cd.rarity)) result.add(cd);
        }
        return result;
    }
}