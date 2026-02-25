package com.github.vegedra.cards;

import com.github.vegedra.core.Main;

public class Card {

    // Atributos
    public String id;
    public String name;
    //public int cost;
    public int value;           // quanto aumenta moedas por clique
    public int coinsPerSecond;
    public String desc;
    //public boolean purchased;

    // Construtor
    public Card(String id, String name, String desc, int value, int coinsPerSecond) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.value = value;
        this.coinsPerSecond = coinsPerSecond;
    }
}