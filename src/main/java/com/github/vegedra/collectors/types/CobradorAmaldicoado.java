/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Drenagem mais pesada e sorte diminui mais. Só aparece em partidas mais longas.
*/

package com.github.vegedra.collectors.types;

import com.github.vegedra.collectors.Cobrador;
import com.github.vegedra.core.Player;

public class CobradorAmaldicoado extends Cobrador {

    // Construtor
    public CobradorAmaldicoado() {
        super(
                "cobrador_amaldicoado",
                "Cobrador Amaldiçoado",
                "Uma presença das sombras. Drena moedas e sorte agressivamente. Cuidado!",
                15,       // drainPerSecond
                400,                    // paymentCost
                25,                     // luckPenaltyOnAttack
                "/images/enemies/cobrador_amaldicoado.gif",
                Debuff.LUCK_DRAIN
        );
        this.luckOnPay = 20;   // Pagar ganha +20% sorte
        this.coinRewardOnDefeat = 150;  // Derrotar ganha 150 moedas
        this.rareCardChance = 40;   // 40% de chance de carta rara ao derrotar
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
        // Drena 2% de sorte por segundo — mais agressivo que o Agressor
        player.changeLuck(-2);
    }
}