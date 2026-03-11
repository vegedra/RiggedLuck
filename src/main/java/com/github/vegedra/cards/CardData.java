/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    CardData — template de carta carregado do JSON via Gson.
    Campos com nomes em snake_case para bater com o JSON.
    Gson preenche os campos automaticamente; valores ausentes ficam 0/null/false.
*/

package com.github.vegedra.cards;

public class CardData {

    // --- Identidade ---
    public String id;                   // Ex: "o_mago"
    public String name;                 // Ex: "O Mago"
    public String desc_normal;          // Descrição quando normal
    public String desc_inverted;        // Descrição quando invertida
    public String type;                 // "CLICK" | "PASSIVE" | "LUCK" | "DEFENSE" | "RISK" | "SYNERGY"
    public String rarity;               // "COMMON" | "UNCOMMON" | "RARE" | "MYTHIC"

    // --- Efeitos normais ---
    public int click_value;
    public int coins_per_second;
    public float luck_bonus;
    public float luck_per_click;
    public float luck_per_second;
    public float tendency_reduction;
    public float osc_multiplier = 1f;   // Padrão 1.0 (sem alteração)
    public String special_effect;       // Ex: "LUCK_FLOOR_25"

    // --- Efeitos invertidos (prefixo inv_) ---
    public int inv_click_value;
    public int inv_coins_per_second;
    public float inv_luck_bonus;
    public float inv_luck_per_click;
    public float inv_luck_per_second;
    public float inv_tendency_reduction;
    public float inv_osc_multiplier = 1f;
    public String inv_special_effect;

    /**
     * Instancia um Card ativo a partir deste template.
     * @param inverted true se a carta sair invertida
     */
    public Card instantiate(boolean inverted) {
        Card.CardType cardType = Card.CardType.valueOf(type);
        Card.Rarity cardRarity = Card.Rarity.valueOf(rarity);

        if (!inverted) {
            return new Card(
                    id, name, desc_normal,
                    cardType, cardRarity, false,
                    click_value, coins_per_second,
                    luck_bonus, luck_per_click, luck_per_second,
                    tendency_reduction, osc_multiplier,
                    special_effect
            );
        } else {
            return new Card(
                    id + "_inv", name, desc_inverted,
                    cardType, cardRarity, true,
                    inv_click_value, inv_coins_per_second,
                    inv_luck_bonus, inv_luck_per_click, inv_luck_per_second,
                    inv_tendency_reduction, inv_osc_multiplier,
                    inv_special_effect
            );
        }
    }
}