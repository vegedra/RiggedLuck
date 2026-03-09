/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Player
*/

package com.github.vegedra.core;

import com.github.vegedra.cards.Card;

public class Player {

    // Atributos
    private long coins = 0;        // Melhor usar globalmente ou criar um novo por modo?
    private int clickValue = 1;    // Quantas moedas gera por clique
    private int luck = 50;        // Sorte
    private int totalCPS = 0;

    // Resetar
    public void reset() {
        coins = 0;
        luck = 50;
        clickValue = 1;
    }

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
