/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Cartas — instância ativa no jogo.
    CardData (json) é o template; Card é o objeto com estado.
*/

package com.github.vegedra.cards;

public class Card {

    // Enums para as cartas
    public enum CardType {
        CLICK,      // Afeta ganho por clique
        PASSIVE,    // Ouro por segundo
        LUCK,       // Manipula a sorte
        DEFENSE,    // Contra cobradores
        RISK,       // Alto risco / alta recompensa
        SYNERGY     // Escala com outras cartas
    }
    public enum Rarity {
        COMMON,     // 50%
        UNCOMMON,   // 30%
        RARE,       // 15%
        MYTHIC      // 5%
    }

    // Dados base Json
    public final String id;
    public final String name;
    public final String desc;
    public final CardType type;
    public final Rarity rarity;
    public final boolean active;
    public boolean used;  // controle de uso único
    public final ActiveEffect activeEffect;

    // Efeitos para cartas de uso único
    public enum ActiveEffect {
        NONE, SECOND_CHANCE, DOUBLE_CLICK, DOUBLE_ALL, DEATH_RESET
    }

    // Efeitos
    public final int clickValue;            // Ouro por clique
    public final int coinsPerSecond;        // Ouro por segundo (passivo)
    public final float luckPerClick;        // Sorte por clique (%)
    public final float luckPerSecond;       // Sorte por segundo (%)

    // Construtor
    public Card(String id, String name, String desc,
                CardType type, Rarity rarity,
                int clickValue, int coinsPerSecond,
                float luckPerClick, float luckPerSecond,
                boolean active, ActiveEffect activeEffect) {

        this.id = id;
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.rarity = rarity;
        this.clickValue = clickValue;
        this.coinsPerSecond = coinsPerSecond;
        this.luckPerClick = luckPerClick;
        this.luckPerSecond = luckPerSecond;
        this.active = active;
        this.activeEffect = activeEffect;
        this.used = false;
    }

    // Representação textual do objeto
    @Override
    public String toString() {
        return name + (" (" + rarity + ")");
    }
}
