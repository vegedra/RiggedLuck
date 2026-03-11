/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    Player
*/

package com.github.vegedra.core;

public class Player {

    // =========================================================================
    // Atributos
    // =========================================================================

    private long coins      = 0;
    private int  clickValue = 1;
    private double luck     = 50.0;

    // =========================================================================
    // Reset
    // =========================================================================

    public void reset() {
        coins      = 0;
        luck       = 50.0;
        clickValue = 1;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public long getCoins()      { return coins; }
    public int  getClickValue() { return clickValue; }
    public int  getLuck()       { return (int) luck; }

    // =========================================================================
    // Moedas
    // =========================================================================

    public void addCoins(int amount)  { coins += amount; }
    public void addCoins(long amount) { coins += amount; }

    // =========================================================================
    // Clique
    // =========================================================================

    public void addClickValue(int amount) {
        clickValue += amount;
        // Clique mínimo de 0 — carta invertida não faz o clique virar negativo permanentemente
        // (A Torre risco com -15 é tratada pontualmente no GameManager)
        if (clickValue < 0) clickValue = 0;
    }

    // =========================================================================
    // Sorte
    // =========================================================================

    /** Altera a sorte pelo valor informado. Mantém entre 0 e 100. */
    public void changeLuck(int amount) {
        luck += amount;
        clampLuck();
    }

    /** Define a sorte diretamente (usado pelo GameManager para piso/teto de cartas). */
    public void setLuck(int value) {
        luck = value;
        clampLuck();
    }

    private void clampLuck() {
        if (luck > 100) luck = 100;
        if (luck <   0) luck = 0;
    }
}