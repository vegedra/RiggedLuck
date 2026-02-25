package com.github.vegedra.cards;

import java.util.Random;

public class CardGenerator {

    Random random = new Random();

    public Card generateCard(int luck) {

        int roll = random.nextInt(100);

        // Sorte influencia
        roll += luck / 5;

        if (roll < 30) {
            return createWeakCard();
        }
        else if (roll < 70) {
            return createNormalCard();
        }
        else {
            return createStrongCard();
        }
    }

    private Card createWeakCard() {
        return new Card(
                "weak_click",
                "Carta Fraca",
                "Gera 1 moeda por clique.",
                1,
                0
        );
    }

    private Card createNormalCard() {
        return new Card(
                "normal_generator",
                "Moeda Viva",
                "Gera 2 moedas passivamente.",
                0,
                2
        );
    }

    private Card createStrongCard() {
        return new Card(
                "strong_click",
                "Bênção Dourada",
                "Gera 5 moedas por clique.",
                5,
                0
        );
    }
}