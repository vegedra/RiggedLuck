/*
    Rigged Luck Copyright (C) Pedro Ivo Rocha de Deus / Digital Cake Studio - All Rights Reserved

    This source code is protected under international copyright law.  
    All rights reserved and protected by the copyright holders.
    This file is confidential and only available to authorized individuals with the
    permission of the copyright holders.  If you encounter this file and do not have
    permission, please contact the copyright holders and delete this file.

    Cards File
*/

package com.github.vegedra.cards;

import com.github.vegedra.core.Main;

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
