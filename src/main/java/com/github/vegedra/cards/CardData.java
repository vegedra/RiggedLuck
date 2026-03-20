/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    CardData — template de carta carregado do json
    Gson preenche os campos automaticamente, valores ausentes ficam 0/null/false.
*/

package com.github.vegedra.cards;

public class CardData {

    // Atributos
    public String id;                   // Ex: "o_mago"
    public String name;                 // Ex: "O Mago"
    public String desc;                 // Descrição
    public String type;                 // CLICK, PASSIVE, LUCK, DEFENSE, RISK, SYNERGY
    public String rarity;               // COMMON, UNCOMMON, RARE, MYTHIC
    public boolean active;              // Se é carta de uso ativo/único
    public String active_effect;

    // Efeitos
    public int click_value;             // bônus de ouro por clique
    public int coins_per_second;        // ouro passivo por segundo
    public float luck_per_click;        // sorte ganha por clique
    public float luck_per_second;       // sorte passiva por segundo

    public Card instantiate() {
        return new Card(
                id, name, desc,
                Card.CardType.valueOf(type),
                Card.Rarity.valueOf(rarity),
                click_value,
                coins_per_second,
                luck_per_click,
                luck_per_second,
                active,
                Card.ActiveEffect.valueOf(active_effect)
        );
    }
}