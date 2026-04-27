/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Cobrador comum. Drena poucas moedas, sem debuff extra.
*/

package com.github.vegedra.collectors.types;

import com.github.vegedra.collectors.Cobrador;
import com.github.vegedra.core.Player;

public class CobradorBasico extends Cobrador {

    // Construtor
    public CobradorBasico() {
        super(
                "cobrador_basico",
                "Cobrador",
                "Um cobrador comum. Algumas moedas são suficientes para afastá-lo.",
                3,        // drainPerSecond
                50,                     // paymentCost
                5,                      // luckPenaltyOnAttack
                "/images/enemies/cobrador_basico.gif",
                Debuff.NONE
        );
        this.luckOnPay = 5;             // Pagar ganha +5% sorte
        this.coinRewardOnDefeat = 30;   // Derrotar ganha 30 moedas
        this.rareCardChance = 0;        // Chance de carta rara ao derrotar
    }

    @Override
    public boolean receberPagamento(int moedasOferecidas) {
        if (moedasOferecidas >= paymentCost) {
            active = false;
            return true;
        }
        return false;
    }

    @Override
    public int receberAtaque() {
        active = false;         // Derrota com 1 ataque
        return luckPenaltyOnAttack;
    }

    @Override
    public void aplicarDebuff(Player player) {
        // Sem debuff extra
    }

    @Override
    public double getDrainPercent() { return 0.01; }
}