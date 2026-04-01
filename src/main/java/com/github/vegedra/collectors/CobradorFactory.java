/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Factory que instancia cobradores por tipo (Factory Pattern),
    Tipos mais difíceis surgem com o tempo.
*/

package com.github.vegedra.collectors;

import com.github.vegedra.collectors.types.CobradorAgressor;
import com.github.vegedra.collectors.types.CobradorAmaldicoado;
import com.github.vegedra.collectors.types.CobradorBasico;
import com.github.vegedra.collectors.types.CobradorTenaz;

import java.util.Random;

public class CobradorFactory {

    private static final Random random = new Random();

    // Instancia um cobrador do tipo especificado
    public static Cobrador create(CobradorType type) {
        switch (type) {
            case AGRESSOR: return new CobradorAgressor();
            case TENAZ: return new CobradorTenaz();
            case AMALDICOADO: return new CobradorAmaldicoado();
            default: return new CobradorBasico();
        }
    }

    /**
     * Sorteia um cobrador aleatório com pesos que escalam com o tempo de jogo.
     * Nos primeiros minutos só aparecem cobradores básicos; com o tempo,
     * tipos mais difíceis ganham peso crescente.
     */
    public static Cobrador createRandom(int secondsElapsed) {
        int minutes = secondsElapsed / 60;

        // Pesos base -> ajustados progressivamente
        int basicWeight = Math.max(10, 60 - minutes * 4);   // diminui com o tempo
        int agressorWeight = Math.min(45, 20 + minutes * 3);   // cresce rapidamente
        int tenazWeight = Math.min(25, 5  + minutes);       // cresce devagar
        int amaldicoWeight = Math.min(20, Math.max(0, (minutes - 3) * 2)); // zero < 3 min

        int total = basicWeight + agressorWeight + tenazWeight + amaldicoWeight;
        int roll  = random.nextInt(total);

        if (roll < basicWeight)
            return create(CobradorType.BASICO);
        if (roll < basicWeight + agressorWeight)
            return create(CobradorType.AGRESSOR);
        if (roll < basicWeight + agressorWeight + tenazWeight)
            return create(CobradorType.TENAZ);
        return create(CobradorType.AMALDICOADO);
    }
}