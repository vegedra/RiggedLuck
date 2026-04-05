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

import com.github.vegedra.cards.Card;
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
    public static Cobrador createRandom(int secondsElapsed, Card[] activeCards) {
        int minutes = secondsElapsed / 60;

        // Pesos base -> ajustados progressivamente
        int basicWeight = Math.max(10, 60 - minutes * 4);   // diminui com o tempo
        int agressorWeight = Math.min(45, 20 + minutes * 3);   // cresce rapidamente
        int tenazWeight = Math.min(25, 5  + minutes);       // cresce devagar
        int amaldicoWeight = Math.min(20, Math.max(0, (minutes - 3) * 2)); // zero < 3 min

        int total = basicWeight + agressorWeight + tenazWeight + amaldicoWeight;
        int roll  = random.nextInt(total);

        CobradorType type;
        if (roll < basicWeight)
            type = CobradorType.BASICO;
        else if (roll < basicWeight + agressorWeight)
            type = CobradorType.AGRESSOR;
        else if (roll < basicWeight + agressorWeight + tenazWeight)
            type = CobradorType.TENAZ;
        else
            type = CobradorType.AMALDICOADO;

        Cobrador c = create(type);

        // Define modo adaptativo com base na build do jogador
        if (activeCards != null) {
            Cobrador.AdaptiveMode mode = determineAdaptiveMode(activeCards);
            c.setAdaptiveMode(mode);
        }

        return c;
    }

    /** Mantém compatibilidade com chamadas sem cartas. */
    public static Cobrador createRandom(int secondsElapsed) {
        return createRandom(secondsElapsed, null);
    }

    /**
     * Analisa as cartas ativas e determina o modo adaptativo do cobrador.
     * Requer pelo menos 2 cartas do mesmo tipo para ativar a adaptação.
     * Tipos de carte ignorados: DEFENSE e SYNERGY (não geram adaptação direta).
     */
    private static Cobrador.AdaptiveMode determineAdaptiveMode(Card[] activeCards) {
        int click   = 0;
        int passive = 0;
        int luck    = 0;
        int risk    = 0;

        for (Card c : activeCards) {
            if (c == null) continue;
            switch (c.type) {
                case CLICK:   click++;   break;
                case PASSIVE: passive++; break;
                case LUCK:    luck++;    break;
                case RISK:    risk++;    break;
                default: break;
            }
        }

        // Encontra o tipo dominante (mínimo 2 cartas para ativar)
        int max = Math.max(Math.max(click, passive), Math.max(luck, risk));
        if (max < 2) return Cobrador.AdaptiveMode.NONE;

        // Em caso de empate, prioridade: Risco > Sorte > Passiva > Clique
        if (risk    == max) return Cobrador.AdaptiveMode.GLASS_CANNON;
        if (luck    == max) return Cobrador.AdaptiveMode.LUCK_DRAIN_ADAPTIVE;
        if (passive == max) return Cobrador.AdaptiveMode.AMPLIFIED_DRAIN;
        if (click   == max) return Cobrador.AdaptiveMode.CLICK_DRAIN;

        return Cobrador.AdaptiveMode.NONE;
    }
}