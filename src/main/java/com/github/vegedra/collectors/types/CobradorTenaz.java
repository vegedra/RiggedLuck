/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Cobrador mais resistente, precisa de 2 ataques para ser derrotado.
    Seu debuff penaliza o valor de clique (calculado via CobradorManager.getTotalClickPenalty).
*/

package com.github.vegedra.collectors.types;

import com.github.vegedra.collectors.Cobrador;
import com.github.vegedra.core.Player;

public class CobradorTenaz extends Cobrador {

    // Variaveis
    private static final int ATTACKS_TO_DEFEAT = 2;
    private int attacksReceived = 0;

    // Construtor
    public CobradorTenaz() {
        super(
                "cobrador_tenaz",
                "Cobrador Tenaz",
                "Resistente à ataques. Mais caro para pagar e enfraquece seus cliques enquanto presente.",
                5,      // drainPerSecond
                200,                  // paymentCost
                8,                    // luckPenaltyOnAttack (aplicado por ATAQUE, não por derrota)
                "/images/enemies/cobrador_tenaz.gif",
                Debuff.CLICK_WEAKEN
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
        attacksReceived++;
        if (attacksReceived >= ATTACKS_TO_DEFEAT) {
            active = false; // Segundo ataque derrota
        }
        return luckPenaltyOnAttack; // Perde sorte a cada ataque
    }

    @Override
    public void aplicarDebuff(Player player) {
        // O debuff CLICK_WEAKEN é tratado diretamente por CobradorManager.getTotalClickPenalty().
        // Não modifica player.clickValue aqui para evitar necessidade de restauração manual.
    }

    // ── Getters específicos ────────────────────────────────────────────────
    public int getAttacksReceived()  { return attacksReceived; }
    public int getAttacksToDefeat()  { return ATTACKS_TO_DEFEAT; }
    public int getRemainingHP() { return ATTACKS_TO_DEFEAT - attacksReceived; }
}