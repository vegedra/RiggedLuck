/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Cartas
*/

package com.github.vegedra.cards;

public class Card {

    // Atributos
    public String id;
    public String name;
    //public int cost;              // Valor de compra (não sei o que fazer ainda)
    public int value;               // Aumento de moedas por clique
    public int coinsPerSecond;      // Aumento de moedas por segundo
    public String desc;
    //public boolean purchased;
    public String rarity;           // TODO: Implementar depois
    // TODO -> public Tradeoff tradeoff;

    // Construtor
    public Card(String id, String name, String desc, int value, int coinsPerSecond) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.value = value;
        this.coinsPerSecond = coinsPerSecond;
    }
}
