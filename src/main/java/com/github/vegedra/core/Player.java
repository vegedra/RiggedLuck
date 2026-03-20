/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Player
*/

package com.github.vegedra.core;

public class Player {

    // Atributos
    private long coins = 0;
    private long totalCoinsEarned = 0;
    private int  clickValue = 1;        // base + bônus das cartas
    private double luck = 50.0;

    // Reset - new game
    public void reset() {
        coins = 0;
        luck = 50;
        clickValue = 1;
        totalCoinsEarned = 0;
    }

    // Getters
    public long getCoins() { return coins; }
    public int  getClickValue() { return clickValue; }
    public int  getLuck() { return (int) luck; }
    public long getTotalCoinsEarned() { return totalCoinsEarned; }

    // Setters
    public void changeCoins(int amount)  { coins += amount; if (amount > 0) totalCoinsEarned += amount; }
    public void addClickValue(int amount) { clickValue = Math.max(0, clickValue + amount); }
    public void setLuck(int value) { luck = Math.min(100, Math.max(0, value)); }
    public void changeLuck(int delta) {
        luck += delta;
        if (luck > 100) luck = 100;
        if (luck < 0) luck = 0;
    }
}