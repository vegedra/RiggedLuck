/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    Cartas — instância ativa no jogo.
    CardData (do JSON) é o template; Card é o objeto vivo com estado.
*/

package com.github.vegedra.cards;

public class Card {

    // --- Enums ---

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

    // --- Dados base (vindos do CardData/JSON) ---

    public final String id;
    public final String name;
    public final String desc;
    public final CardType type;
    public final Rarity rarity;
    public final boolean inverted;      // Carta normal ou invertida

    // Efeitos numéricos básicos (0 se não aplicável)
    public final int clickValue;            // Ouro por clique
    public final int coinsPerSecond;        // Ouro por segundo (passivo)
    public final float luckBonus;           // Sorte imediata ao equipar (%)
    public final float luckPerClick;        // Sorte por clique (%)
    public final float luckPerSecond;       // Sorte por segundo (%)
    public final float tendencyReduction;   // Reduz tendência de queda da sorte (%/s)
    public final float oscMultiplier;       // Multiplica a oscilação da sorte (ex: 0.6 = -40%)

    // Efeito especial — identificado por string, tratado no GameManager
    // Ex: "LUCK_FLOOR_25", "COLLECTOR_DRAIN_LESS_25", "ACTIVE_SWAP_LUCK_GOLD"
    public final String specialEffect;

    // --- Estado em jogo ---
    public boolean active = true;           // Para cartas de uso único que já foram ativadas

    // --- Construtor completo ---
    public Card(String id, String name, String desc,
                CardType type, Rarity rarity, boolean inverted,
                int clickValue, int coinsPerSecond,
                float luckBonus, float luckPerClick, float luckPerSecond,
                float tendencyReduction, float oscMultiplier,
                String specialEffect) {

        this.id = id;
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.rarity = rarity;
        this.inverted = inverted;
        this.clickValue = clickValue;
        this.coinsPerSecond = coinsPerSecond;
        this.luckBonus = luckBonus;
        this.luckPerClick = luckPerClick;
        this.luckPerSecond = luckPerSecond;
        this.tendencyReduction = tendencyReduction;
        this.oscMultiplier = oscMultiplier;
        this.specialEffect = specialEffect;
    }

    // --- Construtor simples (para cartas sem efeitos especiais) ---
    public Card(String id, String name, String desc,
                CardType type, Rarity rarity, boolean inverted,
                int clickValue, int coinsPerSecond) {
        this(id, name, desc, type, rarity, inverted,
                clickValue, coinsPerSecond,
                0f, 0f, 0f, 0f, 1f, null);
    }

    @Override
    public String toString() {
        return name + (inverted ? " [INVERTIDA]" : "") + " (" + rarity + ")";
    }
}