/*
    Rigged Luck Copyright (C) Pedro Ivo Rocha de Deus / Digital Cake Studio - All Rights Reserved

    This source code is protected under international copyright law.  
    All rights reserved and protected by the copyright holders.
    This file is confidential and only available to authorized individuals with the
    permission of the copyright holders.  If you encounter this file and do not have
    permission, please contact the copyright holders and delete this file.

    Player File
*/

package com.github.vegedra.core;

import com.github.vegedra.cards.Card;

public class Player {

    // Atributos
    private int coins = 0;
    private int clickValue = 1;    // Quantas moedas gera por clique
    private int luck = 50;        // Sorte
    private int totalCPS = 0;

    // Getters
    public int getCoins() {
        return coins;
    }
    public int getClickValue() {
        return clickValue;
    }
    public int getLuck() {
        return luck;
    }
    public int getTotalCPS() { return totalCPS; }
    
    // Setters
    public void addCoins(int amount) {
        coins += amount;
    }
    public void addClickValue(int amount) {
        clickValue += amount;
    }
    public void changeLuck(int amount) {
        luck += amount;

        if (luck > 100) luck = 100;
        if (luck < 0) luck = 0;
    }
    public void applyCard(Card card) {
        this.clickValue += card.value;
        this.addCPS(card.coinsPerSecond);
    }
    public void addCPS(int amount) { this.totalCPS += amount; }
    public void removeCPS(int amount) { this.totalCPS -= amount; }
}
