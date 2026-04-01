/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Cobrador violento, drena mais moedas e corrói a sorte passivamente.
*/

package com.github.vegedra.collectors.types;

import com.github.vegedra.collectors.Cobrador;
import com.github.vegedra.core.Player;

public class CobradorAgressor extends Cobrador {

    // Construtor
    public CobradorAgressor() {
        super(
                "cobrador_agressor",
                "Cobrador Agressor",
                "Um verdadeiro bully. Drena moedas rapidamente e corrói a sorte enquanto presente.",
                8,      // drainPerSecond
                100,                  // paymentCost
                15,                   // luckPenaltyOnAttack
                "/images/enemies/cobrador_agressor.gif",
                Debuff.LUCK_DRAIN
        );
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
        active = false;
        return luckPenaltyOnAttack;
    }

    @Override
    public void aplicarDebuff(Player player) {
        // Drena 1% de sorte adicional por segundo além da drenagem normal de moedas
        player.changeLuck(-1);
    }
}