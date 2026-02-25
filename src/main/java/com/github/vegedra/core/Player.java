package com.github.vegedra.core;

import com.github.vegedra.cards.Card;

public class Player {

    private int coins = 0;
    private int clickValue = 1;
    private int luck = 50;

    public void addCoins(int amount) {
        coins += amount;
    }

    public int getCoins() {
        return coins;
    }

    public void addClickValue(int amount) {
        clickValue += amount;
    }

    public int getClickValue() {
        return clickValue;
    }

    public void changeLuck(int amount) {
        luck += amount;

        if (luck > 100) luck = 100;
        if (luck < 0) luck = 0;
    }

    public int getLuck() {
        return luck;
    }

    public void applyCard(Card c) {
        clickValue += c.value;
    }
}
